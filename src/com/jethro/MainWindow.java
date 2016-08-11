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
    private JPanel circleImageTab;
    private JPanel edgeImageTab;
    private JPanel imageTab;
    private JPanel tabPanel;
    private JPanel mainPanel;

    private JLabel imageLabel;
    private JLabel edgeImageLabel;
    private JLabel circleImageLabel;
    private JPanel grayscaleImageTab;
    private JLabel grayscaleImageLabel;
    private JPanel blurredImageTab;
    private JLabel blurredImageLabel;
    private JPanel xGradientTab;
    private JLabel xGradientImageLabel;
    private JPanel yGradientTab;
    private JLabel yGradientImageLabel;

    private BufferedImage baseImg;
    private BufferedImage grayscaleImg;
    private BufferedImage blurredImg;
    private BufferedImage binaryImg;
    private BufferedImage edgeImg;
    private BufferedImage circleImg;

    /**
     * Loads and sets the base image.
     */
    private void ReadAndSetBaseImage() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, PNG, BMP & GIF Images",
                "jpg", "png", "bmp", "gif");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(mainPanel);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                baseImg = ImageIO.read(chooser.getSelectedFile());
                imageLabel.setIcon(new ImageIcon(baseImg));
            } catch (IOException ioe) {
                String msg = "Failed to open imageLabel: " + chooser.getSelectedFile().getName() + "\n";
                msg += "Error: " + ioe.getMessage();
                JOptionPane.showMessageDialog(mainPanel, msg, "Image IO Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Saves the image in the currently selected tab to the directory specified by the user.
     * @param tab JPanel to get the image from.
     */
    private void SaveTabImage(JPanel tab) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, PNG, BMP & GIF Images",
                "jpg", "png", "bmp", "gif");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(mainPanel);
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
                JOptionPane.showMessageDialog(mainPanel, msg, "Image IO Error", JOptionPane.ERROR_MESSAGE);
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
     * Do all required steps to get the hough transform to the image.
     * Generates multiple images that are to be displayed on the screen when they are generated.
     */
    private void ProcessImage() {
        grayscaleImg = ImageToGrayscale(baseImg);
        grayscaleImageLabel.setIcon(new ImageIcon(grayscaleImg));

        blurredImg = GaussianBlur(grayscaleImg, 5);
        blurredImageLabel.setIcon(new ImageIcon(blurredImg));

        BufferedImage[] gradients = SobelFilter(blurredImg);
        xGradientImageLabel.setIcon(new ImageIcon(gradients[0]));
        yGradientImageLabel.setIcon(new ImageIcon(gradients[1]));

        edgeImg = gradients[2];
        edgeImageLabel.setIcon(new ImageIcon(edgeImg));
    }

    /**
     * Convolves in a single direction. Uses relfection at edges.
     * @param img BufferedImage matrix the convolution is to be run on.
     * @param filter double[] filter to be used in convolution.
     * @param horizontal boolean flag determining which direction to OneWayConvolve.
     * @return BufferedImage convolved matrix
     */
    private static BufferedImage OneWayConvolve(BufferedImage img, double[] filter, boolean horizontal) {
        BufferedImage convolvedImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int index = ((int)-(filter.length / 2));
                int filteredC = 0;
                for (int f = 0; f < filter.length; f++) {
                    int sampleIndex;
                    Color c;
                    if (horizontal) {
                        sampleIndex = Math.max(0, Math.min(x + f + index, img.getWidth() - 1)); // bound the filter
                        c = new Color(img.getRGB(sampleIndex, y));
                    } else {
                        sampleIndex = Math.max(0, Math.min(y + f + index, img.getHeight() - 1)); // bound the filter
                        c = new Color(img.getRGB(x, sampleIndex));
                    }
                    filteredC += (int)(c.getRed() * filter[f]); // Only works for grayscale images
                }
                Color newC = new Color(filteredC, filteredC, filteredC);
                convolvedImg.setRGB(x, y, newC.getRGB());
            }
        }
        return convolvedImg;
    }

    /**
     * Converts an image from RGB to grayscale
     * @param img Source image to convert.
     * @return BufferedImage grayscale version of img.
     */
    private static BufferedImage ImageToGrayscale(BufferedImage img) {
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

    /**
     * Use a square gaussian kernel of size kernelSize to blur the image.
     * @param img BufferedImage to blur.
     * @param kernelSize Size of the width and height of the gaussian kernel.
     * @return BufferedImage of the blurred image.
     */
    private static BufferedImage GaussianBlur(BufferedImage img, int kernelSize) {
        int minX = -(kernelSize / 2);
        double[] kernel = GaussianRange(minX, minX + kernelSize - 1);

        BufferedImage horizontal = OneWayConvolve(img, kernel, true);
        return OneWayConvolve(horizontal, kernel, false);
    }

    private static BufferedImage[] SobelFilter(BufferedImage img) {
        int[][] filterX = new int[][]{{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] filterY = new int[][]{{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        BufferedImage xGradient = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        BufferedImage yGradient = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        BufferedImage edgeGradients = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        for (int y = 1; y < img.getHeight() - 1; y++) {
            for (int x = 1; x < img.getWidth() - 1; x++) {
                int xVal = 0;
                int yVal = 0;
                for (int fi = 0; fi < 3; fi++) {
                    for (int fj = 0; fj < 3; fj++) {
                        int c = new Color(img.getRGB(x + fj - 1, y + fi - 1)).getRed();
                        xVal += c * filterX[fi][fj];
                        yVal += c * filterY[fi][fj];
                    }
                }
                xVal = Math.min(255, Math.abs(xVal));
                yVal = Math.min(255, Math.abs(yVal));
                xGradient.setRGB(x, y, new Color(xVal, xVal, xVal).getRGB());
                yGradient.setRGB(x, y, new Color(yVal, yVal, yVal).getRGB());

                int cG = (int) Math.ceil(Math.hypot(xVal, yVal));
                cG = Math.min(255, Math.abs(cG));
                edgeGradients.setRGB(x, y, new Color(cG, cG, cG).getRGB());
            }
        }

        return new BufferedImage[]{xGradient, yGradient, edgeGradients};
    }

    public MainWindow() {
        // When clicked the button opens the file selector dialog and if the imageLabel is valid it sets it
        importImageButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ReadAndSetBaseImage();
                ProcessImage();
            }
        });

        // When clicked the button opens a dialog to enable saving of the currently viewed image
        saveTabImageButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SaveTabImage((JPanel) imageTabs.getSelectedComponent());
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Hough Detector");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }
}
