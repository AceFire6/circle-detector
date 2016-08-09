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

    private Image baseImg;
    private Image binaryImg;
    private Image edgeImg;
    private Image circleImg;

    /**
     * Sets the JLabel icon.
     * @param label JLabel to set.
     * @param img Image to set the JLabel's icon to.
     */
    private void setIconImage(JLabel label, Image img) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, PNG, BMP & GIF Images",
                "jpg", "png", "bmp", "gif");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(MainPanel);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                img = ImageIO.read(chooser.getSelectedFile());
                label.setIcon(new ImageIcon(img));
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
        }

        for (int i = 0; i < gaussRange.length; i++) {
            gaussRange[i] = gaussRange[i] / total;
        }

        return gaussRange;
    }

    public MainWindow() {
        // When clicked the button opens the file selector dialog and if the imageLabel is valid it sets it
        importImageButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setIconImage(imageLabel, baseImg);
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
