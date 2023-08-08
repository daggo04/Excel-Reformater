package com.eyeshare.Dag.view;

import com.eyeshare.Dag.profiles.OpType;
import com.eyeshare.Dag.profiles.Operation;
import com.eyeshare.Dag.profiles.Profile;
import com.eyeshare.Dag.profiles.ProfileManager;

import com.google.gson.JsonSyntaxException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileManagementPanel extends JPanel {

    // Managers
    private ProfileManager profileManager;
    private MainFrame mainFrame;

    private JList<String> operationsList;
    private DefaultListModel<String> operationsListModel;

    private JComboBox<String> profileComboBox;
    private JButton createProfileButton;
    private JButton deleteProfileButton;
    private JButton loadProfileButton;
    private JButton addCopyColumnButton;
    private JButton addCopySplitRowButton;
    private JButton setNamingConventionButton;
    private JButton loadTemplateButton;

    public ProfileManagementPanel(ProfileManager profileManager, MainFrame mainFrame) {
        this.profileManager = profileManager;
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Operations List
        operationsListModel = new DefaultListModel<>();
        operationsList = new JList<>(operationsListModel);
        JScrollPane operationsScrollPane = new JScrollPane(operationsList);
        operationsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        add(operationsScrollPane, gbc);

        // Profile Managing Buttons Panel
        JPanel profileManagingButtonsPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        add(profileManagingButtonsPanel, gbc);

        // Profile ComboBox
        profileComboBox = new JComboBox<>();
        profileComboBox.addActionListener(e -> {onProfileSelectionChanged();});
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        profileManagingButtonsPanel.add(profileComboBox, gbc);
        updateProfileComboBox();

        // Create Profile Button
        createProfileButton = new JButton("Create Profile");
        createProfileButton.addActionListener(e -> {createProfile();});
        gbc.gridy = 1;
        profileManagingButtonsPanel.add(createProfileButton, gbc);

        // Delete Profile Button
        deleteProfileButton = new JButton("Delete Profile");
        deleteProfileButton.addActionListener(e -> deleteProfile());
        gbc.gridy = 2;
        profileManagingButtonsPanel.add(deleteProfileButton);

        // Load Profile Button
        loadProfileButton = new JButton("Load Profile");
        loadProfileButton.addActionListener(e -> {loadProfileFromFile();});
        gbc.gridy = 3;
        profileManagingButtonsPanel.add(loadProfileButton, gbc);

        // Operation Managing Buttons Panel
        JPanel operationManagingButtonsPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(operationManagingButtonsPanel, gbc);

        // Operation Managing Label
        // JLabel operationManagingLabel = new JLabel("Operation Managing:");
        // gbc.gridy = 0;
        // gbc.weighty = 0;
        // gbc.weightx = 1;
        // gbc.fill = GridBagConstraints.HORIZONTAL;
        // operationManagingButtonsPanel.add(operationManagingLabel);

        // Add Copy Column Button
        addCopyColumnButton = new JButton("Add Copy Column");
        addCopyColumnButton.addActionListener(e -> {addCopyColumnOperation();});
        gbc.gridy = 0;
        gbc.weighty = 0;
        operationManagingButtonsPanel.add(addCopyColumnButton, gbc);

        // Add Copy Split Row Button
        addCopySplitRowButton = new JButton("Add Copy Split Row");
        addCopySplitRowButton.addActionListener(e -> {addCopySplitRowOperation();});
        gbc.gridy = 1;
        operationManagingButtonsPanel.add(addCopySplitRowButton, gbc);

        JButton deleteOperationButton = new JButton("Delete Operation");
        deleteOperationButton.addActionListener(e -> {
            int selectedIndex = operationsList.getSelectedIndex();
            if (selectedIndex >= 0) {
                String profileName = (String) profileComboBox.getSelectedItem();
                if (profileName != null) {
                    Profile profile = profileManager.loadProfile(profileName);
                    if (profile != null) {
                        int operationIndex = profileManager.getOperationIndexFromPrettyStringIndex(profileName, selectedIndex);
                        if (operationIndex >= 0) {
                            profile.getOperations().remove(operationIndex);
                            profileManager.updateProfile(profile);
                            onProfileSelectionChanged();
                        }
                    }
                }
            }
        });
        gbc.gridy = 2;
        operationManagingButtonsPanel.add(deleteOperationButton);
        
        
        // Set Naming Convention Button
        setNamingConventionButton = new JButton("Set Naming Convention");
        setNamingConventionButton.addActionListener(e -> {setNamingConvention();});
        gbc.gridy = 3;
        operationManagingButtonsPanel.add(setNamingConventionButton, gbc);

        // Load Template Button
        loadTemplateButton = new JButton("Load Template");
        loadTemplateButton.addActionListener(e -> {
            List<String> templates = profileManager.getAvailableTemplates();
            JComboBox<String> templateComboBox = new JComboBox<>(templates.toArray(new String[0]));
        
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("Select a template:"));
            panel.add(templateComboBox);
        
            JButton importButton = new JButton("Import");
            importButton.addActionListener(importEvent -> {
                importTemplate();
                // Refresh the list of available templates and update the combo box
                List<String> templates2 = profileManager.getAvailableTemplates();
                templateComboBox.setModel(new DefaultComboBoxModel<>(templates2.toArray(new String[0])));
            });
        
            panel.add(importButton);
            panel.add(new JLabel("<html><p>Note: A template is used to get sheet and column names for the output file.</p></html>"));
        
            int result = JOptionPane.showConfirmDialog(null, panel, "Load Template", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String selectedTemplate = (String) templateComboBox.getSelectedItem();
                if (selectedTemplate != null) {
                    String profileName = (String) profileComboBox.getSelectedItem();
                    if (profileName != null) {
                        Profile profile = profileManager.loadProfile(profileName);
                        if (profile != null) {
                            profile.setTemplatePath("/templates/" + selectedTemplate);
                            profileManager.updateProfile(profile);
                            onProfileSelectionChanged();
                        }
                    }
                }
            }
        });
        gbc.gridy = 4;
        operationManagingButtonsPanel.add(loadTemplateButton, gbc);

        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(e -> {
            // Switch back to the main panel
            mainFrame.showPanel("mainPanel");
        });
        gbc.gridy = 10;
        add(doneButton, gbc);
    }

    private void deleteProfile() {
        String profileName = (String) profileComboBox.getSelectedItem();
        if (profileName != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected profile?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                profileManager.removeProfile(profileName);
                updateProfileComboBox();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No profile selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProfileComboBox() {
        // Remove all existing items
        profileComboBox.removeAllItems();

        // Add the profile names from the ProfileManager
        for (String profileName : profileManager.getProfileNames()) {
            profileComboBox.addItem(profileName);
        }
        mainFrame.getMainPanel().updateProfileComboBox();
    }

    private void createProfile() {
        String profileName = JOptionPane.showInputDialog(this, "Enter a name for the new profile:");
        if (profileName == null || profileName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Profile name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (profileManager.profileExists(profileName)) {
            JOptionPane.showMessageDialog(this, "Profile with this name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        profileManager.createProfile(profileName);
        profileManager.saveProfile(profileName);
        updateProfileComboBox();
        profileComboBox.setSelectedItem(profileName);

        // Updating profile combobox on mainpanel
    }

    private void loadProfileFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String fileContent = new String(Files.readAllBytes(selectedFile.toPath()));
                Profile loadedProfile = profileManager.getGson().fromJson(fileContent, Profile.class);
                if (profileManager.profileExists(loadedProfile.getName())) {
                    JOptionPane.showMessageDialog(this, "Profile with this name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                profileManager.addProfile(loadedProfile);
                updateProfileComboBox();
                profileComboBox.setSelectedItem(loadedProfile.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading profile file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (JsonSyntaxException e) {
                JOptionPane.showMessageDialog(this, "Invalid profile JSON file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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

    // Operaton Managing Buttons functionality
    private void addCopyColumnOperation() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
    
        panel.add(new JLabel("Source Sheet:"));
        JTextField srcSheetField = new JTextField();
        panel.add(srcSheetField);
    
        panel.add(new JLabel("Source Column:"));
        JTextField srcColField = new JTextField();
        panel.add(srcColField);
    
        panel.add(new JLabel("Destination Sheet:"));
        JTextField dstSheetField = new JTextField();
        panel.add(dstSheetField);
    
        panel.add(new JLabel("Destination Column:"));
        JTextField dstColField = new JTextField();
        panel.add(dstColField);
    
        int result = JOptionPane.showConfirmDialog(null, panel, "Add Copy Column Operation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("srcSheet", Double.parseDouble(srcSheetField.getText()));
            parameters.put("srcCol", Double.parseDouble(srcColField.getText()));
            parameters.put("dstSheet", Double.parseDouble(dstSheetField.getText()));
            parameters.put("dstCol", Double.parseDouble(dstColField.getText()));
    
            String profileName = (String) profileComboBox.getSelectedItem();
            if (profileName != null) {
                Profile profile = profileManager.loadProfile(profileName);
                if (profile != null) {
                    Operation<Object> operation = new Operation<>(OpType.COPY_COLUMN, parameters);
                    profile.getOperations().add(operation);
                    profileManager.updateProfile(profile);
                    onProfileSelectionChanged();
                }
            }
        }
    }

    private void addCopySplitRowOperation() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel instructions = new JLabel("<html><p>Instructions:</p>"
                + "<ul>"
                + "<li>Source Sheet: Index of the sheet you want to copy rows from.</li>"
                + "<li>Destination Sheet: Index of the sheet you want to copy rows to.</li>"
                + "<li>Start Row: Row number to start copying from.</li>"
                + "<li>Include Headers: If checked, headers will be copied from the source sheet.</li>"
                + "<li>Header Column: Index of the column containing the headers in the source sheet.</li>"
                + "<li>Add Column Mapping: Define which source column maps to which destination column.</li>"
                + "<li>To define a split, map severeal columns to the same column in the destination.</li>"
                + "</ul></html>");
        panel.add(instructions);

        JPanel inputPanel = new JPanel(new GridLayout(0, 2)){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(400, 300);
            };
        };
    
        inputPanel.add(new JLabel("Source Sheet:"));
        JTextField srcSheetField = new JTextField();
        inputPanel.add(srcSheetField);
    
        inputPanel.add(new JLabel("Destination Sheet:"));
        JTextField dstSheetField = new JTextField();
        inputPanel.add(dstSheetField);
    
        inputPanel.add(new JLabel("Start Row:"));
        JTextField startRowField = new JTextField();
        inputPanel.add(startRowField);
    
        inputPanel.add(new JLabel("Include Headers:"));
        JCheckBox includeHeadersCheckbox = new JCheckBox();
        inputPanel.add(includeHeadersCheckbox);
    
        inputPanel.add(new JLabel("Header Column:"));
        JTextField headerColField = new JTextField();
        inputPanel.add(headerColField);
    
        Map<Double, Double> columnMap = new HashMap<>();
        JButton addColumnMappingButton = new JButton("Add Column Mapping");
        addColumnMappingButton.addActionListener(e -> {
            JTextField srcColField = new JTextField();
            JTextField dstColField = new JTextField();
            JPanel columnMappingPanel = new JPanel(new GridLayout(0, 2));
            columnMappingPanel.add(new JLabel("Source Column:"));
            columnMappingPanel.add(srcColField);
            columnMappingPanel.add(new JLabel("Destination Column:"));
            columnMappingPanel.add(dstColField);
    
            int result = JOptionPane.showConfirmDialog(null, columnMappingPanel, "Add Column Mapping", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                columnMap.put(Double.parseDouble(srcColField.getText()), Double.parseDouble(dstColField.getText()));
            }
        });
        inputPanel.add(addColumnMappingButton);
        //inputPanel.add(new JLabel()); // Empty label to fill the grid

        panel.add(inputPanel);
    
        int result = JOptionPane.showConfirmDialog(null, panel, "Add Copy Split Row Operation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("srcSheet", srcSheetField.getText());
            parameters.put("dstSheet", dstSheetField.getText());
            parameters.put("startRow", Integer.parseInt(startRowField.getText()));
            parameters.put("colMap", columnMap);
            parameters.put("includeHeaders", includeHeadersCheckbox.isSelected());
            parameters.put("headerCol", Double.parseDouble(headerColField.getText()));
    
            String profileName = (String) profileComboBox.getSelectedItem();
            if (profileName != null) {
                Profile profile = profileManager.loadProfile(profileName);
                if (profile != null) {
                    Operation<Object> operation = new Operation<>(OpType.COPY_SPLIT_ROW, parameters);
                    profile.getOperations().add(operation);
                    profileManager.updateProfile(profile);
                    onProfileSelectionChanged();
                }
            }
        }
    }

    private void setNamingConvention(){
        JOptionPane.showMessageDialog(null, "Not implemented yet. Sorry! Default naming convention is to preserve the original name.");
    }

    public void importTemplate() {
        JFileChooser fileChooser = new JFileChooser();
    
        // Create a custom FileFilter to only accept Excel files
        FileFilter excelFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                
                String fileName = f.getName().toLowerCase();
                return fileName.endsWith(".xls") || fileName.endsWith(".xlsx");
            }
    
            @Override
            public String getDescription() {
                return "Excel Files (*.xls, *.xlsx)";
            }
        };
        
        // Add the custom FileFilter to the file chooser
        fileChooser.setFileFilter(excelFilter);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                URI templatesDirURI = getClass().getResource("/templates").toURI();
                Path templatesPath = Paths.get(templatesDirURI);
                Path destination = templatesPath.resolve(selectedFile.getName());
    
                // Check if the file already exists in the templates folder
                if (Files.exists(destination)) {
                    JOptionPane.showMessageDialog(null,
                            "This template already exists. Please rename the template before importing.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    Files.copy(selectedFile.toPath(), destination);
                }
    
            } catch (IOException | URISyntaxException e) {
                System.out.println("Error importing template: " + e.getMessage());
            }
        }
    }

    
}    