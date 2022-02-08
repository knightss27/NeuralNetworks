import java.io.FileWriter;
import java.io.IOException;

public class Main {

	final static Example[] handwrittenTraining = ExampleLoader.handwrittenDigitText("digits-train.txt");
	final static Example[] handwrittenTesting = ExampleLoader.handwrittenDigitText("digits-test.txt");

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

		NeuralNetwork network = new NeuralNetwork(64, 10, 138, 0.03);
		NetworkManager.trainToAccuracy(network, handwrittenTraining, handwrittenTesting, (int) (.9 * handwrittenTraining.length), 99.8F, 100, 20);

//		NeuralNetwork network2 = new NeuralNetwork(64, 10, 138, 0.01);
//		network2.useCyclicalLearning = true;
//		NetworkManager.trainToAccuracy(network2, handwrittenTraining, handwrittenTesting, 97, 1000, 25, true);

		int numThreads = 4;

		int categoryMax = 140;
		int categoryMin = 60;

		double learningRateMin = 0.01;
		double learningRateMax = 0.13;
		double learningRateIncrease = 0.01;
		int numIncreasesToRun = (int) ((learningRateMax-learningRateMin)/learningRateIncrease);

		NeuralNetwork[][] networksToRun = new NeuralNetwork[numThreads][(categoryMax-categoryMin)/4];
		NeuralNetwork[][] ratesToRun = new NeuralNetwork[numThreads][numIncreasesToRun/numThreads];

		for (int i = categoryMin; i < categoryMax; i += numThreads) {
			for (int j = i; j < i+4; j++) {
				networksToRun[j%4][(i-categoryMin)/4] = new NeuralNetwork(64, 10, j, 0.05);
			}
		}

//		for (int i = 0; i < numIncreasesToRun; i += numThreads) {
//			for (int j = i; j < i + numThreads; j++) {
//				ratesToRun[j%4][i/4] = new NeuralNetwork(64, 10, 138, learningRateMin + j * learningRateIncrease);
////				System.out.println((int) ((i-learningRateMin)/learningRateIncrease));
//			}
//		}
//
//		for (int i = 0; i < networksToRun.length; i++) {
//			TestingThread thread = new TestingThread(networksToRun[i], i);
//			thread.start();
//		}
    }

    static class TestingThread extends Thread {

    	NeuralNetwork[] networks;
    	int id;

    	TestingThread(NeuralNetwork[] networks, int id) {
    		this.networks = networks;
    		this.id = id;
		}

    	@Override
		public void run() {
			try {
				FileWriter myWriter = new FileWriter("nn_testingA_" + id + ".txt");
//				myWriter.write("Learn Rate, 15 epoch accuracy\n");
				myWriter.write("# Hidden,300 Epoch Acc,Testing Acc\n");

				for (int i = 0; i < networks.length; i++) {
					NeuralNetwork network = networks[i];
					System.out.println("testing: " + id + ": " + i + "/" + networks.length);
					float acc = NetworkManager.trainToAccuracy(network, handwrittenTraining, handwrittenTesting, 2500, 99, 300, 301);

//					myWriter.write(String.valueOf(0.01 + ((i*4)+id) * 0.01) + "," + acc + "\n");
					myWriter.write(network.numHidden + "," + acc + "," + NetworkManager.test(network, handwrittenTesting) + "\n");
					myWriter.flush();
				}

				myWriter.close();
				System.out.println("Thread " + id + " finished.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
