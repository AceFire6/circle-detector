package com.jethro;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;


public class MainWindow {
    private JMenuItem openMenuItem;
    private JMenuItem saveMenuItem;

    private JTabbedPane imageTabs;
    private JPanel houghImageTab;
    private JPanel edgeImageTab;
    private JPanel imageTab;
    private JPanel tabPanel;
    private JPanel mainPanel;

    private JLabel imageLabel;
    private JLabel edgeImageLabel;
    private JLabel houghImageLabel;
    private JPanel grayscaleImageTab;
    private JLabel grayscaleImageLabel;
    private JPanel blurredImageTab;
    private JLabel blurredImageLabel;
    private JPanel xGradientTab;
    private JLabel xGradientImageLabel;
    private JPanel yGradientTab;
    private JLabel yGradientImageLabel;
    private JPanel nonMaxImageTab;
    private JLabel nonMaxImageLabel;
    private JPanel filteredNMSTab;
    private JLabel filteredNMSImageLabel;
    private JPanel hysteresisImageTab;
    private JLabel hysteresisImageLabel;
    private JPanel foundCircleImageTab;
    private JLabel circleImageLabel;

    private BufferedImage baseImg;
    private String baseImgName;
    private boolean GUI = true;

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
                SetBaseImage(chooser.getSelectedFile());
            } catch (IOException ioe) {
                String msg = "Failed to openMenuItem imageLabel: " + chooser.getSelectedFile().getName() + "\n";
                msg += "Error: " + ioe.getMessage();
                JOptionPane.showMessageDialog(mainPanel, msg, "Image IO Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void SetBaseImage(File f) throws IOException {
        BufferedImage inImg = ImageIO.read(f);
        baseImgName = f.getName();
        baseImg = new BufferedImage(inImg.getWidth(), inImg.getHeight(), BufferedImage.TYPE_INT_RGB);
        baseImg.getGraphics().drawImage(inImg, 0, 0, null);
        baseImg.getGraphics().dispose();

        if (GUI) {
            imageLabel.setIcon(new ImageIcon(baseImg));
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
                SaveImage(img, chooser.getSelectedFile().getName());
            } catch (IOException ioe) {
                String msg = "Failed to openMenuItem imageLabel: " + chooser.getSelectedFile().getName() + "\n";
                msg += "Error: " + ioe.getMessage();
                JOptionPane.showMessageDialog(mainPanel, msg, "Image IO Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void SaveImage(BufferedImage img, String outName) throws IOException {
        File imgOut = new File(outName);
        String[] fileParts = imgOut.getName().split("\\.");
        String extension = fileParts[fileParts.length - 1];
        ImageIO.write(img, extension, imgOut);
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
    private void ProcessImage(boolean writeOut) {
        BufferedImage grayscaleImg = ImageToGrayscale(baseImg);
        BufferedImage blurredImg = GaussianBlur(grayscaleImg, 5);

        int[][] xGradValues = new int[baseImg.getHeight()][baseImg.getWidth()];
        int[][] yGradValues = new int[baseImg.getHeight()][baseImg.getWidth()];

        BufferedImage[] gradients = SobelFilter(blurredImg, xGradValues, yGradValues);

        BufferedImage edgeImg = gradients[2];

        BufferedImage[] NSMImages = NonMaximalFilter(edgeImg, xGradValues, yGradValues);
        BufferedImage nonMaxImage = NSMImages[0];
        BufferedImage filteredNMSImage = NSMImages[1];
        BufferedImage hysteresisImage = Hysteresis(filteredNMSImage);

        BufferedImage[] houghImages = HoughCircleDetection(hysteresisImage, xGradValues, yGradValues);
        BufferedImage houghImage = houghImages[0];
        BufferedImage foundCircleImage = houghImages[1];

        if (GUI) {
            grayscaleImageLabel.setIcon(new ImageIcon(grayscaleImg));
            blurredImageLabel.setIcon(new ImageIcon(blurredImg));
            xGradientImageLabel.setIcon(new ImageIcon(gradients[0]));
            yGradientImageLabel.setIcon(new ImageIcon(gradients[1]));
            edgeImageLabel.setIcon(new ImageIcon(edgeImg));
            nonMaxImageLabel.setIcon(new ImageIcon(nonMaxImage));
            filteredNMSImageLabel.setIcon(new ImageIcon(filteredNMSImage));
            hysteresisImageLabel.setIcon(new ImageIcon(hysteresisImage));
            houghImageLabel.setIcon(new ImageIcon(houghImage));
            circleImageLabel.setIcon(new ImageIcon(foundCircleImage));
        }

        if (writeOut) {
            try {
                SaveImage(grayscaleImg, ToPath("grayscale.png"));
                SaveImage(blurredImg, ToPath("blurred.png"));
                SaveImage(gradients[0], ToPath("xgradient.png"));
                SaveImage(gradients[1], ToPath("ygradient.png"));
                SaveImage(edgeImg, ToPath("sobel-edges.png"));
                SaveImage(nonMaxImage, ToPath("non-max-edges.png"));
                SaveImage(filteredNMSImage, ToPath("non-maxfiltered-edges.png"));
                SaveImage(hysteresisImage, ToPath("hysteresis.png"));
                SaveImage(houghImage, ToPath("hough-lines.png"));
                SaveImage(foundCircleImage, ToPath("foundCircles.png"));
            } catch (IOException e) {
                System.out.println("Failed to write out an image");
                System.exit(0);
            }
        }
    }

    private String ToPath(String file) {
        return baseImgName.split("\\.")[0] + "-" + file;
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

    /**
     * Sobel filters the input image
     * @param img BufferedImage img input image, must be grayscale.
     * @param xGradValues int[][] to be filled with the actual values of the x gradient.
     * @param yGradValues int[][] to be filled with the actual values of the y gradient.
     * @return BufferedImage[] array containing xGradient, yGradient and edgeGradient BufferedImages.
     */
    private static BufferedImage[] SobelFilter(BufferedImage img, int[][] xGradValues, int[][] yGradValues) {
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

                xGradValues[y][x] = xVal;
                yGradValues[y][x] = yVal;

                xVal = Math.min(255, Math.abs(xVal));
                yVal = Math.min(255, Math.abs(yVal));
                xGradient.setRGB(x, y, new Color(xVal, xVal, xVal).getRGB());
                yGradient.setRGB(x, y, new Color(yVal, yVal, yVal).getRGB());

                int cG = (int) Math.ceil(Math.hypot(xVal, yVal));  // sqrt(xVal^2 + yVal^2)
                cG = Math.min(255, Math.abs(cG));
                edgeGradients.setRGB(x, y, new Color(cG, cG, cG).getRGB());
            }
        }

        return new BufferedImage[]{xGradient, yGradient, edgeGradients};
    }

    /**
     * Edge thinning technique. Filters edge based on local maxima. Also does threshold filtering after initial
     * operation to determine strong and weak edges.
     * @param grad BufferedImage grad is the combination of xGrad and yGrad.
     * @param xGrad int[][] xGrad is the edge gradient values for the x axis.
     * @param yGrad int[][] yGrad is the edge gradient values for the y axis.
     * @return BufferedImage[] after edge thinning and determining strong and weak edges (Strong = White, Weak = Red).
     */
    private static BufferedImage[] NonMaximalFilter(BufferedImage grad, int[][] xGrad, int[][] yGrad) {
        BufferedImage nonMax = new BufferedImage(grad.getWidth(), grad.getHeight(), grad.getType());
        BufferedImage filtered = new BufferedImage(grad.getWidth(), grad.getHeight(), grad.getType());
        int RGB_WHITE = Color.WHITE.getRGB();

        for (int y = 0; y < grad.getHeight(); y++) {
            for (int x = 0; x < grad.getWidth(); x++) {
                int xLum = xGrad[y][x];
                int yLum = yGrad[y][x];
                double theta = Math.toDegrees(Math.atan2(yLum, xLum));
                // Rounded to nearest 45 degrees
                theta = (Math.round(theta / 45) * 45) % 180;
                theta = (theta >= 0) ? theta : 180 + theta;

                int lum = new Color(grad.getRGB(x, y)).getRed();
                int aLum = 0;
                int bLum = 0;
                int extraLum = 0;

                if (theta == 0) {
                    if (x - 1 >= 0) {
                        extraLum = new Color(grad.getRGB(x-1, y)).getRed(); // West
                        aLum = new Color(nonMax.getRGB(x-1, y)).getRed(); // West
                    }

                    if (x + 1 < grad.getWidth()) {
                        bLum = new Color(grad.getRGB(x+1, y)).getRed(); // East
                    }
                } else if (theta == 45) { // 45 Degrees
                    if ((x-1 >= 0) && (y-1 >= 0)) {
                        extraLum = new Color(grad.getRGB(x-1, y-1)).getRed(); // NW
                        aLum = new Color(nonMax.getRGB(x-1, y-1)).getRed(); // NW
                    }

                    if ((x+1 < grad.getWidth()) && (y+1 < grad.getHeight())) {
                        bLum = new Color(grad.getRGB(x+1, y+1)).getRed(); // SE
                    }
                } else if (theta == 90) { // 90 Degrees
                    if (y - 1 >= 0) {
                        extraLum = new Color(grad.getRGB(x, y-1)).getRed(); // N
                        aLum = new Color(nonMax.getRGB(x, y-1)).getRed(); // N
                    }

                    if (y + 1 < grad.getHeight()) {
                        bLum = new Color(grad.getRGB(x, y+1)).getRed(); // S
                    }
                } else if (theta == 135) { // 135 degrees
                    if ((x+1 < grad.getWidth()) && (y-1 >= 0)) {
                        extraLum = new Color(grad.getRGB(x+1, y-1)).getRed(); // NE
                        aLum = new Color(nonMax.getRGB(x+1, y-1)).getRed(); // NE
                    }

                    if ((x-1 >= 0) && (y+1 < grad.getHeight())) {
                        bLum = new Color(grad.getRGB(x-1, y+1)).getRed(); // SW
                    }
                }

                int low = 20;
                int high = 120;

                if ((lum > aLum) && (lum > bLum) && (lum >= extraLum)) {
                    nonMax.setRGB(x, y, RGB_WHITE);
                    if (lum >= high) {
                        filtered.setRGB(x, y, RGB_WHITE); // Strong edges
                    } else if (lum >= low) {
                        filtered.setRGB(x, y, new Color(120, 0, 0).getRGB()); // Weak edges
                    }
                }
            }
        }

        return new BufferedImage[]{nonMax, filtered};
    }

    /**
     * Hysteresis function used to fill in line segments where weak lines are.
     * @param nmsImg BufferedImage after initial filtering, has strong and weak lines (strong = 255, weak = 120).
     * @return BufferedImage after performing the hysteresis step.
     */
    private static BufferedImage Hysteresis(BufferedImage nmsImg) {
        BufferedImage hysteresisImg = new BufferedImage(nmsImg.getWidth(), nmsImg.getHeight(), nmsImg.getType());
        boolean[][] checkedCells = new boolean[nmsImg.getHeight()][nmsImg.getWidth()];

        for (int y = 0; y < nmsImg.getHeight(); y++) {
            for (int x = 0; x < nmsImg.getWidth(); x++) {
                int lum = new Color(nmsImg.getRGB(x, y)).getRed();
                if (lum != 255 && !checkedCells[y][x]) {
                    if (lum == 120) {
                        ArrayList<int[]> edge = new ArrayList<>();
                        FollowLine(x, y, nmsImg, edge, checkedCells);
                        if (edge.size() > 1 && edge.get(0)[0] == -1) {
                            for (int i = 1; i < edge.size(); i++) {
                                int[] xy = edge.get(i);
                                if (xy[0] != -1) {
                                    hysteresisImg.setRGB(xy[0], xy[1], Color.WHITE.getRGB());
                                    nmsImg.setRGB(xy[0], xy[1], Color.CYAN.getRGB());
                                }
                            }
                        }
                    }
                } else if (lum == 255) {
                    hysteresisImg.setRGB(x, y, Color.WHITE.getRGB());
                    checkedCells[y][x] = true;
                }
            }
        }

        return hysteresisImg;
    }

    /**
     * Recursive grass-fire BLOB algorithm.
     * @param x int of current x coordinate.
     * @param y int of current y coordinate.
     * @param img BufferedImage that has already been filtered to have strong and weak lines.
     * @param edgeList ArrayList<int[]> of pixels in the extracted line. Value at index 0 determines if it should be considered.
     * @param checked boolean[][] used to determine if a pixel has already been checked.
     */
    private static void FollowLine(int x, int y, BufferedImage img, ArrayList<int[]> edgeList, boolean[][] checked) {
        int lum = new Color(img.getRGB(x, y)).getRed();
        if (!checked[y][x]) {
            checked[y][x] = true;
            if (lum == 255) {
                edgeList.add(0, new int[]{-1, 1});
            } else if (lum == 120) {
                edgeList.add(new int[]{x, y});
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int xi = Math.min(img.getWidth(), Math.max(0, x+j));
                        int yi = Math.min(img.getHeight(), Math.max(0, y+i));
                        FollowLine(xi, yi, img, edgeList, checked);
                    }
                }
            }
        } else if (edgeList.get(0)[0] != -1 && lum == 255) {
            edgeList.add(0, new int[]{-1, 1});
        }
    }

    /**
     * Bressenham's Circle Drawing Algorithm as defined here:
     * http://www.gamedev.net/page/resources/_/technical/graphics-programming-and-theory/bresenhams-line-and-circle-algorithms-r767
     * @param centerX int x pixel center of the circle.
     * @param centerY int y pixel center of the circle.
     * @param radius int radius of the circle.
     * @param img BufferedImage to draw the circle to.
     */
    private static int DrawCircle(int centerX, int centerY, int radius, boolean compare, BufferedImage img) {
        int d = 3 - (2 * radius);
        int x = 0;
        int y = radius;

        int rgb_col = new Color(21, 160, 255).getRGB();
        int matches = 0;

        while (x <= y) {
            if (centerX + x < img.getWidth()) {
                if (centerY + y < img.getHeight()) {
                    if (compare) {
                        int currLum = new Color(img.getRGB(centerX + x, centerY + y)).getRed();
                        if (currLum == 255) {
                            matches++;
                        }
                    } else {
                        img.setRGB(centerX + x, centerY + y, rgb_col);
                    }
                }

                if (centerY - y >= 0) {
                    if (compare) {
                        int currLum = new Color(img.getRGB(centerX + x, centerY - y)).getRed();
                        if (currLum == 255) {
                            matches++;
                        }
                    } else {
                        img.setRGB(centerX + x, centerY - y, rgb_col);
                    }
                }
            }

            if (centerX - x >= 0) {
                if (centerY + y < img.getHeight()) {
                    if (compare) {
                        int currLum = new Color(img.getRGB(centerX - x, centerY + y)).getRed();
                        if (currLum == 255) {
                            matches++;
                        }
                    } else {
                        img.setRGB(centerX - x, centerY + y, rgb_col);
                    }
                }

                if (centerY - y >= 0) {
                    if (compare) {
                        int currLum = new Color(img.getRGB(centerX - x, centerY - y)).getRed();
                        if (currLum == 255) {
                            matches++;
                        }
                    } else {
                        img.setRGB(centerX - x, centerY - y, rgb_col);
                    }
                }
            }

            if (centerX + y < img.getWidth()) {
                if (centerY + x < img.getHeight()) {
                    if (compare) {
                        int currLum = new Color(img.getRGB(centerX + y, centerY + x)).getRed();
                        if (currLum == 255) {
                            matches++;
                        }
                    } else {
                        img.setRGB(centerX + y, centerY + x, rgb_col);
                    }
                }

                if (centerY - x >= 0) {
                    if (compare) {
                        int currLum = new Color(img.getRGB(centerX + y, centerY - x)).getRed();
                        if (currLum == 255) {
                            matches++;
                        }
                    } else {
                        img.setRGB(centerX + y, centerY - x, rgb_col);
                    }
                }
            }

            if (centerX - y >= 0) {
                if (centerY + x < img.getHeight()) {
                    if (compare) {
                        int currLum = new Color(img.getRGB(centerX - y, centerY + x)).getRed();
                        if (currLum == 255) {
                            matches++;
                        }
                    } else {
                        img.setRGB(centerX - y, centerY + x, rgb_col);
                    }
                }

                if (centerY - x >= 0) {
                    if (compare) {
                        int currLum = new Color(img.getRGB(centerX - y, centerY - x)).getRed();
                        if (currLum == 255) {
                            matches++;
                        }
                    } else {
                        img.setRGB(centerX - y, centerY - x, rgb_col);
                    }
                }
            }

            if (d < 0) {
                d += (4 * x) + 6;
            } else {
                d += 4 * (x - y) + 10;
                y--;
            }
            x++;
        }
        return matches;
    }

    /**
     * Generalized Bresenham's Line Drawing Algorithm from:
     * https://www.cs.umd.edu/class/fall2003/cmsc427/bresenham.html
     * @param x1 int start x coord.
     * @param y1 int start y coord.
     * @param x2 int end x coord.
     * @param y2 int end y coord.
     * @param accum
     */
    private static void DrawLine(int x1, int y1, int x2, int y2, int[][] accum) {
        int deltaX = Math.abs(x2 - x1);
        int deltaY = Math.abs(y2 - y1);

        int s1 = ((x2 - x1) > 0) ? 1 : (deltaX > 0) ? -1 : 0;
        int s2 = ((y2 - y1) > 0) ? 1 : (deltaY > 0) ? -1 : 0;

        boolean swap = false;

        if (deltaY > deltaX) {
            deltaX = deltaY;
            deltaY = Math.abs(x2 - x1);
            swap = true;
        }

        int y = y1;
        int x = x1;
        int d = (2 * deltaY) - deltaX;

        for (int i = 0; i < deltaX; i++) {
            if ((x > 0 && x < accum[0].length) && (y > 0 && y < accum.length)) {
                accum[y][x] += 1;
            }

            while (d >= 0) {
                d -= 2 * deltaX;
                if (swap) {
                    x += s1;
                } else {
                    y += s2;
                }
            }
            d += 2 * deltaY;
            if (swap) {
                y += s2;
            } else {
                x += s1;
            }
        }
    }

    private static BufferedImage[] HoughCircleDetection(BufferedImage img, int[][] xGrad, int[][] yGrad) {
        int[][] accum = new int[img.getHeight()][img.getWidth()];
        ArrayList<int[]> centers = new ArrayList<>();
        BufferedImage lines = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        BufferedImage foundCircles = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        int length = (int) Math.round(Math.hypot(img.getWidth(), img.getHeight()));

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                foundCircles.setRGB(x, y, img.getRGB(x, y));

                int lum = new Color(img.getRGB(x, y)).getRed();
                if (lum == 255) {
                    int xLum = xGrad[y][x];
                    int yLum = yGrad[y][x];
                    double theta = Math.atan2(yLum, xLum);

                    int x1 = (int) Math.round(x + length * Math.cos(theta));
                    int y1 = (int) Math.round(y + length * Math.sin(theta));

                    int x2 = (int) Math.round(x + (-length * Math.cos(theta)));
                    int y2 = (int) Math.round(y + (-length * Math.sin(theta)));

                    DrawLine(x1, y1, x2, y2, accum);
                }
            }
        }

        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (minVal > accum[y][x]) {
                    minVal = accum[y][x];
                }
                if (maxVal < accum[y][x]) {
                    maxVal = accum[y][x];
                }
            }
        }

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int lum = Math.round((255 * (accum[y][x] - minVal)) / ((float) (maxVal - minVal)));
                lines.setRGB(x, y, new Color(lum, lum, lum).getRGB());

                boolean bigger = true;
                for (int i = -5; i <= 5; i++) {
                    for (int j = -5; j <= 5; j++) {
                        int xVal = x + j;
                        int yVal = y + i;

                        if ((xVal >= 0 && xVal < img.getWidth()) && (yVal >= 0 && yVal < img.getHeight())) {
                            if (accum[y][x] < accum[yVal][xVal]) {
                                bigger = false;
                            }
                        }
                    }
                }

                if (bigger && (accum[y][x] > (maxVal * 0.2))) {
                    centers.add(new int[]{x, y});
                    lines.setRGB(x, y, Color.CYAN.getRGB());
                }
            }
        }

        int biggestRadius = Math.min(200, length / 2);
        int[] bestRadii = new int[centers.size()];
        int[] bestRadiiScore = new int[centers.size()];
        int maxRScore = 0;

        for (int c = 0; c < centers.size(); c++) {
            int[] center = centers.get(c);
            for (int radius = 1; radius < biggestRadius; radius++) {
                int matches = Math.round((2 * DrawCircle(center[0], center[1], radius, true, foundCircles)) / (float)radius);
                if (bestRadiiScore[c] < matches) {
                    bestRadii[c] = radius;
                    bestRadiiScore[c] = matches;
                }
            }
            if (bestRadiiScore[c] > maxRScore) {
                maxRScore = bestRadiiScore[c];
            }
        }


        for (int i = 0; i < bestRadii.length; i++) {
            if (bestRadiiScore[i] > (0.2 * maxRScore)) {
                int[] center = centers.get(i);
                DrawCircle(center[0], center[1], bestRadii[i], false, foundCircles);
            }
        }

        return new BufferedImage[]{lines, foundCircles};
    }

    public MainWindow() {
        openMenuItem = new JMenuItem("Open");
        saveMenuItem = new JMenuItem("Save");
        // When clicked the button opens the file selector dialog and if the imageLabel is valid it sets it
        openMenuItem.addActionListener(e -> {
            ReadAndSetBaseImage();
            ProcessImage(false);
        });

        // When clicked the button opens a dialog to enable saving of the currently viewed image
        saveMenuItem.addActionListener(e -> SaveTabImage((JPanel) imageTabs.getSelectedComponent()));
    }

    public static void main(String[] args) {
        MainWindow mainWindow = new MainWindow();

        if (args.length > 0) {
            mainWindow.GUI = false;
            File inf = new File(args[0]);
            try {
                mainWindow.SetBaseImage(inf);
            } catch (IOException e) {
                System.out.println("Invalid file specified: " + args[0]);
                System.exit(0);
            }
            mainWindow.ProcessImage(true);
        } else {
            JFrame frame = new JFrame("Hough Detector");

            // Menu Bar
            JMenuBar menuBar = new JMenuBar();
            JMenu jMenu = new JMenu("File");
            jMenu.add(mainWindow.openMenuItem);
            jMenu.add(mainWindow.saveMenuItem);
            menuBar.add(jMenu);

            frame.setJMenuBar(menuBar);
            frame.setContentPane(mainWindow.mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null); // Center the frame
            frame.setVisible(true);
        }
    }
}
