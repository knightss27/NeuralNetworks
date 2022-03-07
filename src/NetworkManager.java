// Seth Knights

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class NetworkManager {

    public static void train(NeuralNetwork network, Example[] examples, boolean shuffleData) {
        Example[] newArr = shuffleData ? shuffleArray(examples) : examples;
        for (int i = 0; i < examples.length; i++) {
            network.learnExample(newArr[i]);
        }
    }

    // Implementing Fisher–Yates shuffle
    private static Example[] shuffleArray(Example[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Example a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        return ar;
    }

    public static float test(NeuralNetwork network, Example[] examples) {
        int totalCorrect = 0;
        for (Example example : examples) {
            int category = network.classify(example);

            if (category == example.category) {
                totalCorrect++;
            }
        }
        return (float) totalCorrect / examples.length * 100;
    }

    public static float trainToAccuracy(NeuralNetwork network, Example[] trainingExamples, Example[] testingExamples, int validationSplit, float desiredAccuracy, int epochCutoff, int debugEpoch, boolean restartAtCutoff) {
        return trainToAccuracy(network, trainingExamples, testingExamples, validationSplit, desiredAccuracy, epochCutoff, debugEpoch, false, restartAtCutoff, new float[2][2]);
    }

    public static float trainToAccuracy(NeuralNetwork network, Example[] trainingExamples, Example[] testingExamples, int validationSplit, float desiredAccuracy, int epochCutoff, int debugEpoch, boolean restartAtCutoff, float[][] highestAccuracies) {
        return trainToAccuracy(network, trainingExamples, testingExamples, validationSplit, desiredAccuracy, epochCutoff, debugEpoch, false, restartAtCutoff, highestAccuracies);
    }

    public static float trainToAccuracy(NeuralNetwork network,
                                        Example[] trainingExamples,
                                        Example[] testingExamples,
                                        int validationSplit,
                                        float desiredAccuracy,
                                        int epochCutoff,
                                        int debugEpoch,
                                        boolean shuffleExamples,
                                        boolean restartAtCutoff,
                                        float[][] highestAccuracies) {
        int epoch = 0;
        float accuracy = 0;

        Example[] actualTraining;
        Example[] actualValidation;

        if (validationSplit == 0) {
            actualTraining = trainingExamples;
            actualValidation = trainingExamples;
        } else {
            actualTraining = Arrays.copyOfRange(trainingExamples, 0, validationSplit);
            actualValidation = Arrays.copyOfRange(trainingExamples, validationSplit, trainingExamples.length);
        }

        for (int i = 0; i < epochCutoff; i++) {
            train(network, actualTraining, shuffleExamples);
            accuracy = test(network, actualValidation);
            float testingAccuracy = test(network, testingExamples);
            epoch++;

            if (epoch % debugEpoch == 0) {
                System.out.println("Epoch " + epoch + ": done with validation accuracy " + accuracy + "%");
                System.out.println("--------- testing accuracy is: " + testingAccuracy + "% ");
            }

            // Updates for the highest stuff
            if (accuracy > highestAccuracies[0][0]) {
                highestAccuracies[0][0] = accuracy;
                highestAccuracies[0][1] = epoch;
            }
            if (testingAccuracy > highestAccuracies[1][0]) {
                highestAccuracies[1][0] = accuracy;
                highestAccuracies[1][1] = epoch;
            }

            if (accuracy > desiredAccuracy) {
                break;
            }

            if (network.useCyclicalLearning) {
//                double cycle = Math.floor(1.0 + epoch/(2.0*network.stepSize));
//                double x = Math.abs(((double) epoch)/network.stepSize - 2.0*cycle + 1.0);

                // This equation describes a triangle that decreases by half its height each full cycle, hitting its apex mid-cycle.
                // base_lr + (max_lr-base_lr)*np.maximum(0, (1-x))/float(2**(cycle-1))
                //network.learningRate = network.learningRateMin + (network.learningRateMax - network.learningRateMin)*Math.max(0, (1.0-x))/Math.pow(2, cycle-1.0);

                network.learningRate = Math.min((desiredAccuracy - accuracy) / accuracy * 5.0, 0.05);
            }


            if (restartAtCutoff && i == epochCutoff - 1) {
                System.out.println("Testing accuracy is: " + testingAccuracy + "% before restarting. \n");
                network.initializeNeurons();
                i = 0;
                epoch = 0;
            }
        }
        float testingAccuracy = test(network, testingExamples);
        float trainingAccuracy = test(network, trainingExamples);

        System.out.println("Training finished. Epoch: " + epoch + ", Testing: " + testingAccuracy + ", Validation: " + accuracy + ", Training: " + trainingAccuracy);
        return accuracy;
    }

    public static void writeNetworkToFile(NeuralNetwork network) {
        try {
            FileWriter myWriter = new FileWriter("nn_data.txt");
            // numAttributes, numCategories, numHidden, learningRate, useCyclicalLearning
            // hiddenLayer
            // answerLayer

            myWriter.write(network.numAttributes + "," + network.numCategories + "," + network.numHidden + "," + network.learningRate + "," + (network.useCyclicalLearning ? 1 : 0) + "\n");
            myWriter.write(Arrays.toString(network.hiddenLayer) + "\n");
            myWriter.write(Arrays.toString(network.answerLayer));

            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
