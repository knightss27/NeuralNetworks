import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class ExcelOutput {

    public static void generateCSV(String inputFile, String outputFile, int numNodeIncreases, int numRateIncreases) {
        try {
            FileWriter writer = new FileWriter(outputFile);

            Path filepath = FileSystems.getDefault().getPath(inputFile);
            List<String> lines = Files.readAllLines(filepath);
            lines = lines.subList(1, lines.size());

            PriorityQueue<String> queue = new PriorityQueue<>(lines.size()-2, new TestComparator());
            queue.addAll(lines);


            String[][] outputData = new String[numRateIncreases][numNodeIncreases];
            String[] outputNodeLabels = new String[numNodeIncreases];
            int currentRate = 0;
            int currentNode = 0;


            while (!queue.isEmpty()) {
                String val = queue.poll();

                outputData[currentRate][currentNode] = val.split(",")[4];

                if (currentRate == 0) {
                    outputNodeLabels[currentNode] = val.split(",")[1] + "N";
                }

                currentNode++;

                if (currentNode == numNodeIncreases) {
                    if (currentRate == 0) {
                        String nodeLabels = Arrays.toString(outputNodeLabels);
                        writer.write(nodeLabels.substring(1, nodeLabels.length() - 1) + "\n");
                    }

                    writer.write(val.split(",")[2] + "R, " + Arrays.toString(outputData[currentRate]).substring(1, Arrays.toString(outputData[currentRate]).length() - 1) + "\n");
                    writer.flush();

                    currentRate++;
                    currentNode = 0;
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Ack!  Problem generating CSV: " + e.getMessage());
        }
    }

    static class TestComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {

            String[] o1Items = o1.split(",");
            String[] o2Items = o2.split(",");

            int n1 = Integer.parseInt(o1Items[1]);
            int n2 = Integer.parseInt(o2Items[1]);
            double r1 = Double.parseDouble(o1Items[2]);
            double r2 = Double.parseDouble(o2Items[2]);

            if (Math.abs(r1-r2) < 1e-6) {
                if (n1 > n2) {
                    return 1;
                } else if (n1 < n2) {
                    return -1;
                } else {
                    return 0;
                }
            } else if (r1 > r2) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
