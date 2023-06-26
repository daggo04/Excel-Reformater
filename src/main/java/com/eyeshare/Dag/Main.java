package com.eyeshare.Dag;

import java.io.IOException;

import javax.swing.SwingUtilities;

import com.eyeshare.Dag.profiles.ProfileManager;
import com.eyeshare.Dag.utils.ResourcesExtractor;
import com.eyeshare.Dag.view.MainFrame;

public class Main {
    public static void main(String[] args) {


        ResourcesExtractor extractor = new ResourcesExtractor();
        try {
            extractor.extractResources();
        } catch (IOException e) {
            System.err.println("Failed to extract resources: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Resources extracted successfully.");
        

        ProfileManager profileManager = new ProfileManager();

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(profileManager);
            mainFrame.setVisible(true);
        });
    }
}