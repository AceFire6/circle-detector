package com.jethro;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class MainWindow {
    private JButton importImageButton;
    private JButton saveTabImageButton;
    private JButton detectCirclesButton;
    private JButton runEdgeDetectionButton;

    private JTabbedPane imageTabs;
    private JPanel CircleImageTab;
    private JPanel EdgeImageTab;
    private JPanel ImageTab;
    private JPanel TabPanel;
    private JPanel MainPanel;

    private JLabel imageLabel;
    private JLabel EdgeImageLabel;
    private JLabel CircleImageLabel;
    private JPanel GrayscaleImageTab;
    private JLabel grayscaleImageLabel;

    private Image baseImg;
    private Image binaryImg;
    private Image edgeImg;
    private Image circleImg;

    /**
     * Loads and sets the base image.
     */
    private void readAndSetBaseImage() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, PNG, BMP & GIF Images",
                "jpg", "png", "bmp", "gif");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(MainPanel);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                baseImg = ImageIO.read(chooser.getSelectedFile());
                imageLabel.setIcon(new ImageIcon(baseImg));
            } catch (IOException ioe) {
                String msg = "Failed to open imageLabel: " + chooser.getSelectedFile().getName() + "\n";
                msg += "Error: " + ioe.getMessage();
                JOptionPane.showMessageDialog(MainPanel, msg, "Image IO Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Saves the image in the currently selected tab to the directory specified by the user.
     * @param tab JPanel to get the image from.
     */
    private void saveTabImage(JPanel tab) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, PNG, BMP & GIF Images",
                "jpg", "png", "bmp", "gif");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(MainPanel);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = (BufferedImage) ((ImageIcon)((JLabel)tab.getComponent(0)).getIcon()).getImage();
                File imgOut = chooser.getSelectedFile();
                String[] fileParts = imgOut.getName().split("\\.");
                String extension = fileParts[fileParts.length - 1];
                ImageIO.write(img, extension, imgOut);
            } catch (IOException ioe) {
                String msg = "Failed to open imageLabel: " + chooser.getSelectedFile().getName() + "\n";
                msg += "Error: " + ioe.getMessage();
                JOptionPane.showMessageDialog(MainPanel, msg, "Image IO Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Samples the gaussian distribution at (x, 0).
     * @param x int x coordinate to sample the gaussian at.
     * @return Double of the gaussian at point (x, 0).
     */
    private static double Gaussian(int x) {
        double sigmaSq = 1.4 * 1.4;
        return ((1 / (2 * Math.PI * sigmaSq)) * Math.exp(-(x * x) / sigmaSq));
    }

    /**
     * Returns a range of values from minX to maxX.
     * @param minX int lower bound of the range.
     * @param maxX int upper bound of the range.
     * @return Double array containing normalized results.
     */
    private static double[] GaussianRange(int minX, int maxX) {
        double total = 0;
        double[] gaussRange = new double[(maxX - minX) + 1];

        for (int i = minX; i < maxX + 1; i++) {
            gaussRange[i - minX] = Gaussian(i);
            total += gaussRange[i - minX];
        }

        for (int i = 0; i < gaussRange.length; i++) {
            gaussRange[i] = gaussRange[i] / total;
        }

        return gaussRange;
    }

    /**
     * Runs convolution on the input matrix using the filter horizontally and then vertically.
     * @param matrix double[][] the convolution is to be run on.
     * @param filter double[] to use for the convolution.
     * @return double[][] convolved matrix.
     */
    private static double[][] oneWayConvolve(double[][] matrix, double[] filter) {
        double[][] horizontal = oneWayConvolve(matrix, filter, true);
        return oneWayConvolve(horizontal, filter, false);
    }

    /**
     * Do all required steps to get the hough transform to the image.
     * Generates multiple images that are to be displayed on the screen when they are generated.
     */
    private void processImage() {
        grayscaleImageLabel.setIcon(new ImageIcon(imageToGrayscale((BufferedImage) baseImg)));
    }

    /**
     * Convolves in a single direction. Uses relfection at edges.
     * @param matrix double[][] matrix the convolution is to be run on.
     * @param filter double[] filter to be used in convolution.
     * @param horizontal boolean flag determining which direction to oneWayConvolve.
     * @return double[][] convolved matrix
     */
    private static double[][] oneWayConvolve(double[][] matrix, double[] filter, boolean horizontal) {
        double[][] newMatrix = new double[matrix.length][matrix[0].length];

        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[y].length; x++) {
                int index = ((int)-(filter.length / 2));
                for (int f = 0; f < filter.length; f++) {
                    int sampleIndex;
                    if (horizontal) {
                        sampleIndex = Math.max(0, Math.min(x + f + index, matrix[y].length - 1)); // bound the filter
                        newMatrix[y][x] += filter[f] * matrix[y][sampleIndex];
                    } else {
                        sampleIndex = Math.max(0, Math.min(y + f + index, matrix.length - 1)); // bound the filter
                        newMatrix[y][x] += filter[f] * matrix[sampleIndex][x];
                    }
                }
            }
        }
        return newMatrix;
    }

    /**
     * Converts an image from RGB to grayscale
     * @param img Source image to convert.
     * @return BufferedImage grayscale version of img.
     */
    private static BufferedImage imageToGrayscale(BufferedImage img) {
        BufferedImage gImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x, y));
                int cGray = (int) ((0.2989 * c.getRed()) + (0.5870 * c.getGreen()) + (0.1140 * c.getBlue()));
                gImg.setRGB(x, y, new Color(cGray, cGray, cGray).getRGB());
            }
        }
        return gImg;
    }

    public MainWindow() {
        // When clicked the button opens the file selector dialog and if the imageLabel is valid it sets it
        importImageButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                readAndSetBaseImage();
                processImage();
            }
        });

        // When clicked the button opens a dialog to enable saving of the currently viewed image
        saveTabImageButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                saveTabImage((JPanel) imageTabs.getSelectedComponent());
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Hough Detector");
        frame.setContentPane(new MainWindow().MainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }
}
