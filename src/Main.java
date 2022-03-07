// Seth Knights

import java.util.concurrent.CountDownLatch;

public class Main {

    public static void main(String[] args) {

        // Learn AND
        learnAND(1.0, false);

        // Learn XOR
        learnXOR(0.05, false);

        // Learn AK Digits
        learnAKDigits(138, 0.03, 0.9, false);

        // Learn MNIST Digits
        learnMNIST(138, 0.05, 0.9, false, false);

        // Learn MNIST with GUI
        learnMNIST(138, 0.05, 0.9, false, true);

        // Multithreaded testing
        ThreadWriter threadWriter = new ThreadWriter("test-100e-4");
        threadedLearn(2, new int[]{2,202}, 1, new double[]{0.01, 1.01}, .02, handwrittenTraining, handwrittenTesting, 0.9, threadWriter);
    }

    final static Example[] XORexamples = new Example[]{new Example(new int[]{0, 0}, 0), new Example(new int[]{0, 1}, 1), new Example(new int[]{1, 0}, 1), new Example(new int[]{1, 1}, 0),};
    final static Example[] ANDexamples = new Example[]{new Example(new int[]{0, 0}, 0), new Example(new int[]{0, 1}, 0), new Example(new int[]{1, 0}, 0), new Example(new int[]{1, 1}, 1),};

    final static Example[] handwrittenTraining = ExampleLoader.handwrittenDigitText("digits-train.txt");
    final static Example[] handwrittenTesting = ExampleLoader.handwrittenDigitText("digits-test.txt");

    static void learnAND(double learningRate, boolean restartAtCutoff) {
        NeuralNetwork network = new NeuralNetwork(2, 2, 2, learningRate);
        NetworkManager.trainToAccuracy(network, ANDexamples, ANDexamples, 0, 99, 500, 10, restartAtCutoff);
    }

    static void learnXOR(double learningRate, boolean restartAtCutoff) {
        NeuralNetwork network = new NeuralNetwork(2, 2, 2, learningRate);
        NetworkManager.trainToAccuracy(network, XORexamples, XORexamples, 0, 99, 40000, 1000, restartAtCutoff);
    }

    static void learnAKDigits(int numHidden, double learningRate, double validationProportion, boolean restartAtCutoff) {
        NeuralNetwork network = new NeuralNetwork(64, 10, numHidden, learningRate);
        NetworkManager.trainToAccuracy(network, handwrittenTraining, handwrittenTesting, (int) (validationProportion * handwrittenTraining.length), 99.8F, 100, 20, restartAtCutoff);
    }

    static void learnMNIST(int numHidden, double learningRate, double validationProportion, boolean restartAtCutoff, boolean useGUI) {
        Example[] mnistDataTrain = ExampleLoader.MNISTDigitSet("train-labels-idx1-ubyte", "train-images-idx3-ubyte");
		Example[] mnistDataTest = ExampleLoader.MNISTDigitSet("t10k-labels-idx1-ubyte", "t10k-images-idx3-ubyte");

        NeuralNetwork network = new NeuralNetwork(64, 10, numHidden, learningRate);
        NetworkManager.trainToAccuracy(network, mnistDataTrain, mnistDataTest, (int) (validationProportion * handwrittenTraining.length), 99.8F, 200, 2, restartAtCutoff);

        if (useGUI) {
            new MNISTGui(network);
        }
    }

    static void threadedLearn(int numThreads, int[] numHiddenBounds, int numHiddenStep, double[] learningRateBounds, double learningRateStep, Example[] trainingData, Example[] testingData, double validationProportion, ThreadWriter threadWriter) {
        if ((numHiddenBounds[1] - numHiddenBounds[0]) % numThreads != 0) {
            throw new RuntimeException("Number of hidden nodes to check is not evenly split by number of threads.");
        }

        int numHiddenMin = numHiddenBounds[0];
        int numHiddenMax = numHiddenBounds[1];
        int numHiddenIncreasesToRun = (numHiddenMax - numHiddenMin) / numThreads;
        double learningRateMin = learningRateBounds[0];
        double learningRateMax = learningRateBounds[1];
        int numRateIncreasesToRun = (int) ((learningRateMax - learningRateMin) / learningRateStep);

        // Each thread runs 1/4 the hidden neuron options, going through each of the learning rates
        NeuralNetwork[][] networksToRun = new NeuralNetwork[numThreads][numHiddenIncreasesToRun * numRateIncreasesToRun];

        for (int i = numHiddenMin; i < numHiddenMax; i += numThreads) {
            for (int thread = 0; thread < numThreads; thread++) {
                int numHidden = i + thread;
                for (int rateInterval = 0; rateInterval < numRateIncreasesToRun; rateInterval++) {
                    networksToRun[thread][numHiddenIncreasesToRun * numRateIncreasesToRun + rateInterval] = new NeuralNetwork(2, 2, numHidden, learningRateMin + learningRateStep * rateInterval);
                }
            }
        }

        System.out.println("Successfully generated " + numThreads + " threads with " + networksToRun[0].length + " networks each.");
        System.out.println("Now attempting to initialize and run threads.");
        System.out.println("Writing to file: " + threadWriter.fileName);

        threadWriter.write("Epoch Cutoff,# Hidden,Learning Rate,10 Run Avg. Validation Accuracy,2 Run Avg. Testing Accuracy, Highest Testing Acc.,Highest Test Epoch,Highest Validation Acc.,Highest Validation Epoch\n");

        CountDownLatch latch = new CountDownLatch(numThreads);
        System.out.println("starting threads");
        threadWriter.start();

        for (int i = 0; i < networksToRun.length; i++) {
            NetworkTestingThread thread = new NetworkTestingThread(i, networksToRun[i], trainingData, testingData, validationProportion, threadWriter, latch);
            thread.start();
        }

        try {
            latch.await();
            threadWriter.interrupt();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
