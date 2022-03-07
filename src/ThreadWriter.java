// Seth Knights

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadWriter extends Thread {

    BlockingQueue<String> writeBuffer;
    String fileName;
    FileWriter writer;
    boolean running;

    ThreadWriter(String filename) {
        this.fileName = filename;
        this.writeBuffer = new LinkedBlockingQueue<>();
        try {
            this.writer = new FileWriter(fileName + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
    }

    public void write(String toWrite) {
        try {
            writeBuffer.put(toWrite);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String toWrite = writeBuffer.take();
                writer.write(toWrite);
                writer.flush();

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
