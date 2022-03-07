// Seth Knights

public class Neuron {

    public Neuron[] inputs;
    public double[] weights;
    public double biasWeight;

    public double output;

    Neuron(double weight) {
        this.biasWeight = weight;
    }

    public void compute() {
        double totalInput = 0;
        for (int i = 0; i< inputs.length; i++) {
            totalInput += inputs[i].output * weights[i];
        }
        totalInput += biasWeight;

        this.output = activation(totalInput);
    }

    private double activation(double total) {
        return 1.0 / (1.0 + Math.pow(Math.E, -total));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (double weight : weights) {
            sb.append(weight).append(";");
        }
        sb.append(biasWeight);
        String weightString = sb.toString();

        return "(" + weightString + ")";
    }
}
