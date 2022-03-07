// Seth Knights

import java.util.Arrays;

public class Example {

    public int category;
    public double[] attributes;

    Example() {

    }

    Example(int[] attributes, int category) {
        this.category = category;
        this.attributes = Arrays.stream(attributes).asDoubleStream().toArray();
    }

    Example(double[] attributes, int category) {
        this.category = category;
        this.attributes = attributes;
    }

}
