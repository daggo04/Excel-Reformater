package com.eyeshare.Dag.view;

import com.eyeshare.Dag.profiles.ProfileManager;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ProfileManager profileManager;
    private MainPanel mainPanel;
    private ProfileManagementPanel profileManagementPanel;


    public MainFrame(ProfileManager profileManager) {
        this.profileManager = profileManager;

        setTitle("Excel Reformatter [BETA 1.0.0]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Set layout to CardLayout
        getContentPane().setLayout(new CardLayout());

        // Add panels
        mainPanel = new MainPanel(profileManager, this);
        ProfileManagementPanel profileManagementPanel = new ProfileManagementPanel(profileManager, this);
        getContentPane().add(mainPanel, "mainPanel");
        getContentPane().add(profileManagementPanel, "profileManagementPanel");
    }

    public void showPanel(String panelName) {
        CardLayout cardLayout = (CardLayout) getContentPane().getLayout();
        cardLayout.show(getContentPane(), panelName);
    }

    public MainPanel getMainPanel() { 
        return this.mainPanel;
    }

    public ProfileManagementPanel getProfileManagementPanel() {
        return this.profileManagementPanel;
    }

    public ProfileManager getProfileManager() {
        return this.profileManager;
    }
}
