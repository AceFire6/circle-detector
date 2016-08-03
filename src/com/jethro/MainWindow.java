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
    private JLabel image;
    private JLabel EdgeImage;
    private JLabel CircleImage;
    private JPanel CircleImageTab;
    private JPanel EdgeImageTab;
    private JPanel ImageTab;
    private JPanel TabPanel;
    private JPanel MainPanel;

    private Image baseImg;
    private Image binaryImg;
    private Image edgeImg;
    private Image circleImg;

    public MainWindow() {
        // When clicked the button opens the file selector dialog and if the image is valid it sets it
        importImageButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, PNG, BMP & GIF Images",
                        "jpg", "png", "bmp", "gif");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(MainPanel);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        baseImg = ImageIO.read(chooser.getSelectedFile());
                        image.setIcon(new ImageIcon(baseImg));
                    } catch (IOException ioe) {
                        String msg = "Failed to open image: " + chooser.getSelectedFile().getName() + "\n";
                        msg += "Error: " + ioe.getMessage();
                        JOptionPane.showMessageDialog(MainPanel, msg, "Image IO Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Hough Detector");
        frame.setContentPane(new MainWindow().MainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(new Dimension(400, 600));
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }
}
