import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class NeuralNetwork {

    int numAttributes, numCategories, numHidden;
    double learningRate, learningRateMax, learningRateMin;
    int stepSize;
    boolean useCyclicalLearning;


    Sensor[] inputLayer;
    Neuron[] hiddenLayer;
    Neuron[] answerLayer;

//    NeuralNetwork(String filename) {
//        try {
//            Path filepath = FileSystems.getDefault().getPath(fileName);
//            List<String> lines = Files.readAllLines(filepath);
//            Example[] examples = new Example[lines.size()];
//
//            for (int i = 0; i < lines.size(); i++) {
//                String[] values = lines.get(i).split(",");
//                switch (i) {
//                    case 0: {
//                        numAttributes = Integer.parseInt(values[0]);
//                        numCategories = Integer.parseInt(values[1]);
//                        numHidden = Integer.parseInt(values[2]);
//                        learningRate = Double.parseDouble(values[3]);
//                        useCyclicalLearning = values[4].equals("1");
//                        break;
//                    }
//                    case 1: {
//
//                    }
//
//                }
//            }
//        } catch (IOException ioexception) {
//            System.out.println("Ack!  We had a problem loading netowrk file: " + ioexception.getMessage());
//        }
//    }

    /**
     *
     * @param numAttributes # of inputs neurons
     * @param numCategories # of output neurons
     * @param numHidden # of hidden neurons
     * @param learningRate learning rate
     */
    NeuralNetwork(int numAttributes, int numCategories, int numHidden, double learningRate) {
        this.numAttributes = numAttributes;
        this.numCategories = numCategories;
        this.numHidden = numHidden;
        this.learningRate = learningRate;

        inputLayer = new Sensor[numAttributes];
        hiddenLayer = new Neuron[numHidden];
        answerLayer = new Neuron[numCategories];

        initializeNeurons();
    }

    // Cyclical learning rate decreasing
    NeuralNetwork(int numAttributes, int numCategories, int numHidden, double learningRateMin, double learningRateMax, int stepSize) {
        this.numAttributes = numAttributes;
        this.numCategories = numCategories;
        this.numHidden = numHidden;
        this.learningRate = learningRateMin;
        this.learningRateMin = learningRateMin;
        this.learningRateMax = learningRateMax;
        this.stepSize = stepSize;
        this.useCyclicalLearning = true;

        inputLayer = new Sensor[numAttributes];
        hiddenLayer = new Neuron[numHidden];
        answerLayer = new Neuron[numCategories];

        initializeNeurons();
    }

    public void initializeNeurons() {
        for (int i = 0; i < inputLayer.length; i++) {
            inputLayer[i] = new Sensor();
        }

        for (int i = 0; i < hiddenLayer.length; i++) {
            hiddenLayer[i] = new Neuron(Math.random() * .1 - 0.05);
            hiddenLayer[i].inputs = inputLayer;

            double[] weights = new double[inputLayer.length];
            for (int j = 0; j < weights.length; j++) {
                weights[j] = Math.random() * .1 - 0.05;
            }
            hiddenLayer[i].weights = weights;

        }

        for (int i = 0; i < answerLayer.length; i++) {
            answerLayer[i] = new Neuron(Math.random() * .1 - 0.05);
            answerLayer[i].inputs = hiddenLayer;

            double[] weights = new double[hiddenLayer.length];
            for (int j = 0; j < weights.length; j++) {
                weights[j] = Math.random() * .1 - 0.05;
            }
            answerLayer[i].weights = weights;
        }
    }

    public int classify(Example example) {
        for (int i = 0; i < example.attributes.length; i++) {
            inputLayer[i].setValue(example.attributes[i]);
        }

        for (Neuron neuron : hiddenLayer) {
            neuron.compute();
        }

        int greatestIndex = 0;
        for (int i = 0; i < answerLayer.length; i++) {
            answerLayer[i].compute();

            if (answerLayer[i].output > answerLayer[greatestIndex].output) {
                greatestIndex = i;
            }
        }

        return greatestIndex;
    }

    public void learnExample(Example example) {
        classify(example);

        double[] answerLayerErrors = new double[answerLayer.length];
        for (int i = 0; i < answerLayer.length; i++) {
            double correctAnswer = i == example.category ? 1.0 : 0.0;
            answerLayerErrors[i] = (correctAnswer - answerLayer[i].output) * answerLayer[i].output * (1.0 - answerLayer[i].output);
//            System.out.println("Output amount: " + answerLayer[i].output);
        }

        double[] hiddenLayerErrors = new double[hiddenLayer.length];
        for (int i = 0; i < hiddenLayer.length; i++) {
            double hiddenError = 0;
            for (int j = 0; j < answerLayer.length; j++) {
                // look at each answer layer node and what weight is assigned to the input
                hiddenError += answerLayerErrors[j] * answerLayer[j].weights[i];
            }
            hiddenLayerErrors[i] = hiddenError * hiddenLayer[i].output * (1.0 - hiddenLayer[i].output);
        }

        for (int i = 0; i < answerLayer.length; i++) {
            for (int j = 0; j < answerLayer[i].weights.length; j++) {
                // NewWeight = OldWeight + AnswerErrorSignal * HiddenResult * LearningRate
                answerLayer[i].weights[j] = answerLayer[i].weights[j] + (answerLayerErrors[i]) * (hiddenLayer[j].output) * learningRate;
            }
            // Bias calculation sets input to 1.0
            answerLayer[i].biasWeight = answerLayer[i].biasWeight + (answerLayerErrors[i]) * (1.0) * learningRate;
        }

        for (int i = 0; i < hiddenLayer.length; i++) {
            for (int j = 0; j< inputLayer.length; j++) {
                // NewWeight = OldWeight + HiddenErrorSignal * SensorInputValue * LearningRate
                hiddenLayer[i].weights[j] = hiddenLayer[i].weights[j] + (hiddenLayerErrors[i]) * (inputLayer[j].output) * learningRate;
            }
            // Bias calculation sets input to 1.0
            hiddenLayer[i].biasWeight = hiddenLayer[i].biasWeight + (hiddenLayerErrors[i]) * (1.0) * learningRate;
        }

    }

}
