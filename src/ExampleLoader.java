
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExampleLoader {

    public static Example[] handwrittenDigitText(String fileName) {
        try {
            Path filepath = FileSystems.getDefault().getPath(fileName);
            List<String> lines = Files.readAllLines(filepath);
            Example[] examples = new Example[lines.size()];

            for (int i = 0; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");
                int category = Integer.parseInt(values[values.length-1]);
                int[] attributes = new int[values.length-1];
                for (int j = 0; j < values.length-1; j++) {
                    attributes[j] = Integer.parseInt(values[j]);
                }

                examples[i] = new Example(attributes, category);
            }

            return examples;
        } catch (IOException ioexception) {
            System.out.println("Ack!  We had a problem: " + ioexception.getMessage());
        }
        return new Example[]{};
    }

    public static Example[] MNISTDigitSet(String labelFileName, String imageFileName) {
        DataInputStream labelStream = openFile(labelFileName, 2049);
        DataInputStream imageStream = openFile(imageFileName, 2051);

        List<Example> examples = new ArrayList<>();

        try {
            int numLabels = labelStream.readInt();
            int numImages = imageStream.readInt();
            assert(numImages == numLabels) : "lengths of label file and image file do not match";

            int rows = imageStream.readInt();
            int cols = imageStream.readInt();
            assert(rows == cols) : "images in file are not square";
            assert(rows == 28) : "images in file are wrong size";

            for (int i = 0; i < numImages; i++) {
                int categoryLabel = Byte.toUnsignedInt(labelStream.readByte());
                double[] inputs = new double[rows * cols];
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        int pixel = 255 - Byte.toUnsignedInt(imageStream.readByte());
                        inputs[r * rows + c] = pixel / 255.0;
                    }
                }

                int[] newChunkedInputs = MNISTChunkImage(inputs);
                examples.add(new Example(newChunkedInputs, categoryLabel));
//                examples.add(new Example(inputs, categoryLabel));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return examples.toArray(new Example[]{});
    }

    static int[] MNISTChunkImage(double[] inputs) {
        int[] newChunkedInputs = new int[49];
        int currentChunk = 0;

        for (int j = 0; j < 28; j += 4) {
            for (int k = 0; k < 28; k += 4) {

                int numColoredPixels = 0;

                for (int x = j; x < j+4; x++) {
                    for (int y = k; y < k+4; y++) {
                        int pixelIndex = x * 28 + y;
                        if (inputs[pixelIndex] < 0.9) {
                            numColoredPixels++;
                        }
                    }
                }

                newChunkedInputs[currentChunk] = numColoredPixels;
                currentChunk++;
            }
        }
        return newChunkedInputs;
    }

    static DataInputStream openFile(String fileName, int expectedMagicNumber) {
        DataInputStream stream = null;
        try {
            stream = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            int magic = stream.readInt();
            if (magic != expectedMagicNumber) {
                throw new RuntimeException("file " + fileName + " contains invalid magic number");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("file " + fileName + " was not found");
        } catch (IOException e) {
            throw new RuntimeException("file " + fileName + " had exception: " + e);
        }
        return stream;
    }
}
