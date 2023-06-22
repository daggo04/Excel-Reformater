package com.eyeshare.Dag.view;

import com.eyeshare.Dag.profiles.ProfileManager;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ProfileManager profileManager;

    public MainFrame(ProfileManager profileManager) {
        this.profileManager = profileManager;

        setTitle("Excel Reformatter [BETA 0.01]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Set layout to CardLayout
        getContentPane().setLayout(new CardLayout());

        // Add panels
        MainPanel mainPanel = new MainPanel(profileManager, this);
        ProfileManagementPanel profileManagementPanel = new ProfileManagementPanel(profileManager, this);
        getContentPane().add(mainPanel, "mainPanel");
        getContentPane().add(profileManagementPanel, "profileManagementPanel");
    }

    public void showPanel(String panelName) {
        CardLayout cardLayout = (CardLayout) getContentPane().getLayout();
        cardLayout.show(getContentPane(), panelName);
    }
}
