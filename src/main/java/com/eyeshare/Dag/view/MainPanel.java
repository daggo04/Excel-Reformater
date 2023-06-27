package com.eyeshare.Dag.view;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;


import com.eyeshare.Dag.functionality.ExcelReformatter;
import com.eyeshare.Dag.profiles.ProfileManager;
import com.eyeshare.Dag.profiles.NamingConvention;
import com.eyeshare.Dag.profiles.Profile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainPanel extends JPanel {
    // Managers
    private ProfileManager profileManager;
    private MainFrame mainFrame;

    // Data fields
    private JList<String> sourceFilesList;
    private DefaultListModel<String> FilesListModel;
    private File[] selectedFiles;
    private File destinationFolder;
    private JList<String> operationsList;
    private DefaultListModel<String> operationsListModel;


    // Components
    private JScrollPane filesScrollPane;
    private JButton chooseFilesButton;
    private JButton chooseDestinationButton;
    private JComboBox<String> profileComboBox;
    private JButton manageProfilesButton;
    private JButton applyProfileButton;

    public MainPanel(ProfileManager profileManager, MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.destinationFolder = new File(System.getProperty("user.home"));
        this.selectedFiles = new File[0];
        this.profileManager = profileManager;

        setPreferredSize(new Dimension(800, 600));

        initComponents();
    }

    // Adding some graphics to the panel to make it look nicer
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.DARK_GRAY.brighter());
        Rectangle rect = new Rectangle(0, getHeight()-40, getWidth(), getHeight()+20);
        g2d.fill(rect);
        g2d.drawLine(getWidth()/2, getHeight()- 100, getWidth()/2, getHeight());

        
    }
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        LineBorder border = new LineBorder(Color.BLACK, 2, true);
        
        // Source Files List
        this.FilesListModel = new DefaultListModel<>();
        this.sourceFilesList = new JList<>(FilesListModel);
        filesScrollPane = new JScrollPane(sourceFilesList);
        filesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        filesScrollPane.setBorder(border);
        
        // Operations List
        operationsListModel = new DefaultListModel<>();
        operationsList = new JList<>(operationsListModel);
        JScrollPane operationsScrollPane = new JScrollPane(operationsList);
        operationsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        operationsScrollPane.setBorder(border);
        
        // Create JSplitPane and add the JScrollPanes
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filesScrollPane, operationsScrollPane);
        splitPane.setResizeWeight(0.5); // Set the divider to be at the middle initially
        splitPane.setContinuousLayout(true); // Update the layout continuously while resizing
    
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(splitPane, gbc);

        gbc. gridwidth = 1;

        // Choose Files Button
        chooseFilesButton = new JButton("Choose Files");
        chooseFilesButton.addActionListener(e -> chooseFiles());
        
        // Choose Destination Button
        chooseDestinationButton = new JButton("Choose Destination");
        chooseDestinationButton.addActionListener(e -> chooseDestination());
        
        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        leftButtonsPanel.add(chooseFilesButton);
        leftButtonsPanel.add(chooseDestinationButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(leftButtonsPanel, gbc);
        
        // Profile ComboBox
        profileComboBox = new JComboBox<>();
        profileComboBox.addActionListener(e -> onProfileSelectionChanged());
        updateProfileComboBox();
    
        // Manage Profiles Button
        manageProfilesButton = new JButton("Manage Profiles");
        manageProfilesButton.addActionListener(e -> manageProfiles());
        
        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rightButtonsPanel.add(profileComboBox);
        rightButtonsPanel.add(manageProfilesButton);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(rightButtonsPanel, gbc);
    
        // Apply Profile Button
        applyProfileButton = new JButton("Apply Profile");
        applyProfileButton.addActionListener(e -> applyProfile());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(applyProfileButton, gbc);
    }

    private void chooseFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Source Files");
        fileChooser.setMultiSelectionEnabled(true); // Allow multiple file selection
    
        // Set file filter for Excel files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xls", "xlsx");
        fileChooser.setFileFilter(filter);
    
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedFiles = fileChooser.getSelectedFiles();
    
            // Update the FilesListModel
            updateFilesList();
        }
    }
    

    private void chooseDestination() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Destination Folder");
        fileChooser.setMultiSelectionEnabled(false); // Forbid multiple file selection
    
        // Set file selection mode to directories only
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
        // Set the destination folder as the chosen directory
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            destinationFolder = fileChooser.getSelectedFile();
    
            // Update the FilesListModel
            updateFilesList();
        }
    }
    

    private void updateFilesList() {
        FilesListModel.clear();
        FilesListModel.addElement("Destination Folder:");
        FilesListModel.addElement("    " + destinationFolder.getPath());
        FilesListModel.addElement("Selected Files:");
        for (File file : selectedFiles) {
            FilesListModel.addElement("   " + file.getName());
        }
    }
    

    private void manageProfiles() {
        mainFrame.showPanel("profileManagementPanel");
    }

    private void applyProfile() {
        Profile selectedProfile = profileManager.loadProfile((String) profileComboBox.getSelectedItem());
    
        // Iterate over the selected files and apply the profile
        for (int i = 0; i < selectedFiles.length; i++) {
            File inputFile = selectedFiles[i];
            String outputFileName = getOutputFileName(inputFile, selectedProfile.getNamingConvention(), null, i + 1);
            try {
                ExcelReformatter reformatter = new ExcelReformatter(inputFile.getAbsolutePath(), selectedProfile);
                reformatter.applyProfile();
                reformatter.saveOutputWorkbook(destinationFolder.getAbsolutePath() + "/" + outputFileName);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error processing file: " + inputFile.getName() + "\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void updateProfileComboBox() {
        // Remove all existing items
        profileComboBox.removeAllItems();
    
        // Add the profile names from the ProfileManager
        for (String profileName : profileManager.getProfileNames()) {
            profileComboBox.addItem(profileName);
        }
        onProfileSelectionChanged();
    }

    private void onProfileSelectionChanged() {
        String selectedProfileName = (String) profileComboBox.getSelectedItem();
        if (selectedProfileName != null) {
            updateOperationsList(selectedProfileName);
        }
    }

    private void updateOperationsList(String profileName) {
        // Clear the existing list
        operationsListModel.clear();
    
        // Add the pretty operation strings for the selected profile
        ArrayList<String> prettyOperations = profileManager.operationsAsPrettyString(profileName);
        for (String operationString : prettyOperations) {
            operationsListModel.addElement(operationString);
        }
    }

    private String getOutputFileName(File inputFile, NamingConvention namingConvention, String customNamePrefix, int fileNumber) {
        String outputFileName;
        String fileName = inputFile.getName();
        int extensionIndex = fileName.lastIndexOf('.');
        String fileNameWithoutExtension = extensionIndex == -1 ? fileName : fileName.substring(0, extensionIndex);
        String extension = extensionIndex == -1 ? "" : fileName.substring(extensionIndex + 1);
    
        switch (namingConvention) {
            case PRESERVE_NAME:
                outputFileName = fileNameWithoutExtension + "_" + fileNumber + "." + extension;
                break;
            case CUSTOM_NAME:
                outputFileName = customNamePrefix + "_" + fileNumber + "." + extension;
                break;
            default:
                throw new IllegalArgumentException("Unsupported naming convention: " + namingConvention);
        }
        return outputFileName;
    }


    
}