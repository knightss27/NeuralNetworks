// Seth Knights

import java.util.concurrent.CountDownLatch;

public class NetworkTestingThread extends Thread {
    NeuralNetwork[] networks;
    CountDownLatch latch;
    ThreadWriter writer;

    Example[] testingData, trainingData;
    double validationProportion;

    int id;


    NetworkTestingThread(int id, NeuralNetwork[] networks, Example[] trainingData, Example[] testingData, double validationProportion, ThreadWriter writer, CountDownLatch latch) {
        this.networks = networks;
        this.testingData = testingData;
        this.trainingData = trainingData;
        this.validationProportion = validationProportion;

        this.id = id;
        this.latch = latch;
        this.writer = writer;
    }

    @Override
    public void run() {
        for (int i = 0; i < networks.length; i++) {
            NeuralNetwork network = networks[i];
            System.out.println("testing: " + id + ": " + i + "/" + networks.length + " -- at: " + System.currentTimeMillis());
            int epochCutoff = 30000;
            float totalValidationAcc = 0;
            float totalTestingAcc = 0;
            float[][] totalHighs = new float[2][2];


            for (int j = 0; j < 2; j++) {
                float[][] highs = new float[2][2];
                float acc = NetworkManager.trainToAccuracy(network, trainingData, testingData, (int) (validationProportion * trainingData.length), 99, epochCutoff, 1000, false, highs);
                totalValidationAcc += acc;
                totalTestingAcc += NetworkManager.test(network, testingData);
                totalHighs[0][0] += highs[0][0];
                totalHighs[0][1] += highs[0][1];
                totalHighs[1][0] += highs[1][0];
                totalHighs[1][1] += highs[1][1];

                network.initializeNeurons();
            }

            float avgValidationAcc = totalValidationAcc / 2F;
            float avgTestingAcc = totalTestingAcc / 2F;
            for (int k = 0; k < totalHighs.length; k++) {
                for (int l = 0; l < totalHighs[k].length; l++) {
                    totalHighs[k][l] = totalHighs[k][l] / 2F;
                }
            }

            writer.write(epochCutoff + "," + network.numHidden + "," + network.learningRate + "," + avgValidationAcc + "," + avgTestingAcc + "," + totalHighs[0][0] + "," + totalHighs[0][1] + "," + totalHighs[1][0] + "," + totalHighs[1][1] + "\n");
        }

        latch.countDown();
    }
}
