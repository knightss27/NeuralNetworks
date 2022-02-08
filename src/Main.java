import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Main {

	final static Example[] handwrittenTraining = ExampleLoader.handwrittenDigitText("digits-train.txt");
	final static Example[] handwrittenTesting = ExampleLoader.handwrittenDigitText("digits-test.txt");
	final static ThreadWriter threadWriter = new ThreadWriter("test-2d");

    public static void main(String[] args) {
	    Example[] ANDexamples = new Example[]{
	      new Example(new int[]{0,0}, 0),
	      new Example(new int[]{0,1}, 0),
	      new Example(new int[]{1,0}, 0),
	      new Example(new int[]{1,1}, 1),
        };

		Example[] XORexamples = new Example[]{
				new Example(new int[]{0,0}, 0),
				new Example(new int[]{0,1}, 1),
				new Example(new int[]{1,0}, 1),
				new Example(new int[]{1,1}, 0),
		};

//		Example[] mnistDataTrain = ExampleLoader.MNISTDigitSet("train-labels-idx1-ubyte", "train-images-idx3-ubyte");
//		Example[] mnistDataTest = ExampleLoader.MNISTDigitSet("t10k-labels-idx1-ubyte", "t10k-images-idx3-ubyte");

//		NeuralNetwork network = new NeuralNetwork(49, 10, 138, 0.05);
//		NetworkManager.trainToAccuracy(network, mnistDataTrain, mnistDataTest, 3000, 95, 200, 10);

//		NetworkManager.writeNetworkToFile(network);

//		System.out.println("Desired accuracy reached, launching GUI");
//
//		new MNISTGui(network);

//		NeuralNetwork network = new NeuralNetwork(2, 2, 2, 1.0);
//	    NetworkManager.trainToAccuracy(network, ANDexamples, ANDexamples, 0, 99, 500, 10);

//		NeuralNetwork network = new NeuralNetwork(2, 2, 2, 0.05);
//		NetworkManager.trainToAccuracy(network, XORexamples, XORexamples, 0, 99, 100000, 1000);

		// Step size should = (2-8) * (training iterations in epoch = 3823)

//		NeuralNetwork network = new NeuralNetwork(64, 10, 138, 0.03);
//		NetworkManager.trainToAccuracy(network, handwrittenTraining, handwrittenTesting, (int) (.9 * handwrittenTraining.length), 99.8F, 100, 20);

//		NeuralNetwork network2 = new NeuralNetwork(64, 10, 138, 0.01);
//		network2.useCyclicalLearning = true;
//		NetworkManager.trainToAccuracy(network2, handwrittenTraining, handwrittenTesting, 97, 1000, 25, true);

		int numThreads = 4;

		int categoryMax = 150;
		int categoryMin = 130;

		double learningRateMin = 0.01;
		double learningRateMax = 0.13;
		double learningRateIncrease = 0.01;
		int numIncreasesToRun = (int) ((learningRateMax-learningRateMin)/learningRateIncrease);

		NeuralNetwork[][] networksToRun = new NeuralNetwork[numThreads][(categoryMax-categoryMin)/4];
		NeuralNetwork[][] ratesToRun = new NeuralNetwork[numThreads][numIncreasesToRun/numThreads];

//		for (int i = categoryMin; i < categoryMax; i += numThreads) {
//			for (int j = i; j < i+4; j++) {
//				networksToRun[j%4][(i-categoryMin)/4] = new NeuralNetwork(64, 10, j, 0.03);
//			}
//		}

//		for (int i = 0; i < numIncreasesToRun; i += numThreads) {
//			for (int j = i; j < i + numThreads; j++) {
//				ratesToRun[j%4][i/4] = new NeuralNetwork(64, 10, 138, learningRateMin + j * learningRateIncrease);
//			System.out.println((int) ((i-learningRateMin)/learningRateIncrease));
//			}
//		}

		// Each thread runs 1/4 the hidden neuron options, going through each of the learning rates
		NeuralNetwork[][] ratesVsNetworksToRun = new NeuralNetwork[numThreads][(categoryMax-categoryMin)/4 * (numIncreasesToRun)];


		// for all the numbers of hidden neurons
		// for each group of four
		// for each one loop over all rates
		// for each rate create a new neural network at index [thread][

		for (int i = categoryMin; i < categoryMax; i += numThreads) {
			for (int thread = 0; thread < numThreads; thread++) {
				int currentRate = 0;
				int numHidden = i + thread;
				for (int rateInterval = 0; rateInterval < numIncreasesToRun; rateInterval++) {
//					System.out.println("Thread: " + thread + ", numHidden: " + numHidden + ", rate: " + rateInterval + ", at: [" + thread + "][" + ((numHidden-categoryMin)/4 * numIncreasesToRun + rateInterval) + "]");
					ratesVsNetworksToRun[thread][((numHidden - categoryMin) / 4 * numIncreasesToRun + rateInterval)] = new NeuralNetwork(64, 10, numHidden, learningRateMin + learningRateIncrease * rateInterval);
				}
			}
		}

		System.out.println("Successfully generated " + numThreads + " threads with " + ratesVsNetworksToRun[0].length + " networks each.");
		System.out.println("Now attempting to initialize and run threads.");
		System.out.println("Writing to file: " + threadWriter.fileName);

		threadWriter.write("Epoch Cutoff,# Hidden,Learning Rate,Validation Accuracy,Testing Accuracy\n");

		CountDownLatch latch = new CountDownLatch(4);
		System.out.println("starting threads");
		threadWriter.start();
		for (int i = 0; i < ratesVsNetworksToRun.length; i++) {
			TestingThread thread = new TestingThread(ratesVsNetworksToRun[i], i, latch);
			thread.start();
		}

		try {
			latch.await();
			threadWriter.interrupt();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    static class TestingThread extends Thread {

    	NeuralNetwork[] networks;
    	int id;
    	CountDownLatch latch;

    	TestingThread(NeuralNetwork[] networks, int id, CountDownLatch latch) {
    		this.networks = networks;
    		this.id = id;
    		this.latch = latch;
		}

    	@Override
		public void run() {
			for (int i = 0; i < networks.length; i++) {
				NeuralNetwork network = networks[i];
				System.out.println("testing: " + id + ": " + i + "/" + networks.length);
				int epochCutoff = 100;
				float acc = NetworkManager.trainToAccuracy(network, handwrittenTraining, handwrittenTesting, (int) (handwrittenTraining.length * .9), 99.9F, epochCutoff, 301, false);

				threadWriter.write(epochCutoff + "," + network.numHidden + "," + network.learningRate + "," + acc + "," + NetworkManager.test(network, handwrittenTesting) + "\n");
			}

			latch.countDown();
		}
	}
}
