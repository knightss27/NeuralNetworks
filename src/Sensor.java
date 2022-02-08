public class Sensor extends Neuron {
    Sensor() {
        super(0);
    }

    public void setValue(double value) {
        this.output = value;
    }
}
