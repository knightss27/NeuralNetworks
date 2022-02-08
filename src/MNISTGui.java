import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

class MNISTGui {

    final int imageSize = 28;  // don't change this - must match the MNIST data
    final int canvasSize = imageSize * 15;
    final int brushSize = 30;

    NeuralNetwork net;
    PaintCanvas paintCanvas;
    JLabel digitLabel;

    MNISTGui(NeuralNetwork net) {
        this.net = net;

        paintCanvas = new PaintCanvas();

        Box controls = Box.createVerticalBox();

        JButton learnButton = new JButton("Learn");
        learnButton.addActionListener(new LearnListener());

        JButton clearButton = new JButton(("Clear"));
        clearButton.addActionListener(new ClearListener());

        JButton classifyButton = new JButton("Classify");
        classifyButton.addActionListener(new ClassifyListener());

        digitLabel = new JLabel();
        digitLabel.setFont(new Font("Sans Serif", Font.BOLD, 48));
        digitLabel.setText("  ");
        Box digitBox = Box.createHorizontalBox();
        digitBox.add(Box.createGlue());
        digitBox.add(digitLabel);
        digitBox.add(Box.createGlue());

        controls.add(learnButton);
        controls.add(Box.createVerticalStrut(10));
        controls.add(clearButton);
        controls.add(Box.createVerticalStrut(10));
        controls.add(classifyButton);
        controls.add(Box.createVerticalStrut(20));
        controls.add(Box.createGlue());
        controls.add(digitBox);


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(paintCanvas, BorderLayout.CENTER);
        mainPanel.add(controls, BorderLayout.WEST);

        // Create the framed window //////////////////////////////////

        JFrame frame = new JFrame("Digit Recognition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.toFront();
        paintCanvas.clear();
    }

    class LearnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Please pre-train your data");
        }
    }

    class ClearListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            paintCanvas.clear();
            digitLabel.setText("  ");
        }
    }

    class ClassifyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Image sampledImage = paintCanvas.bufferedImage.getScaledInstance(imageSize,imageSize,Image.SCALE_AREA_AVERAGING);
            BufferedImage finalImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_BYTE_GRAY);
            Graphics pen = finalImage.getGraphics();
            pen.drawImage(sampledImage, 0, 0, null);


            paintCanvas.bufferedImagePen.drawImage(finalImage,canvasSize-imageSize,canvasSize-imageSize, paintCanvas);
            paintCanvas.display();

            double[] inputs = new double[imageSize*imageSize];
            for (int y=0; y<imageSize; y++) {
                for (int x=0; x<imageSize; x++) {
                    inputs[y*imageSize+x] = (finalImage.getRGB(x,y) & 0xFF) / 255.0;
                }
            }
            Example example = new Example(ExampleLoader.MNISTChunkImage(inputs), 0);

            int classifiedCategory = net.classify(example);
            System.out.println("Recognized a " + classifiedCategory);

            digitLabel.setText("" + classifiedCategory);
        }
    }

    class PaintCanvas extends Canvas {
        private BufferStrategy strategy = null;
        private final Color backgroundColor = Color.WHITE;
        private final Color foregroundColor = Color.BLACK;
        private Graphics2D bufferedImagePen;
        BufferedImage bufferedImage;

        PaintCanvas() {
            setPreferredSize(new Dimension(canvasSize, canvasSize));
            setIgnoreRepaint(true);
            Scribbler scribbler = new Scribbler();
            addMouseListener(scribbler);
            addMouseMotionListener(scribbler);
            bufferedImage = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_RGB);
            bufferedImagePen = bufferedImage.createGraphics();
            bufferedImagePen.setColor(foregroundColor);
            bufferedImagePen.setStroke(new BasicStroke(brushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        public void addNotify() {
            super.addNotify();
            createBufferStrategy(2);
            strategy = getBufferStrategy();
        }

        Graphics2D getCanvasPen() {
            return (Graphics2D) strategy.getDrawGraphics();
        }

        void display() {
            getCanvasPen().drawImage(bufferedImage, 0, 0, this);
            strategy.show();
        }

        void clear() {
            bufferedImagePen.setColor(backgroundColor);
            bufferedImagePen.fillRect(0, 0, getWidth(), getHeight());
            bufferedImagePen.setColor(foregroundColor);
            display();
        }

        class Scribbler extends MouseInputAdapter {
            int prevX, prevY;

            @Override
            public void mousePressed(MouseEvent event) {
                prevX = event.getX();
                prevY = event.getY();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                int x = event.getX();
                int y = event.getY();
                bufferedImagePen.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
                display();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

        }

    }
}