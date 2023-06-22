package com.eyeshare.Dag.profiles;

import com.eyeshare.Dag.utils.OperationDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * ProfileManager
 * Manages the profiles
 * Singleton class
 */
public class ProfileManager {
    private static final String APP_DIR = System.getProperty("user.dir");
    private static final String PROFILES_DIR = APP_DIR + "/profiles";
    private static final String TEMPLATES_DIR = APP_DIR + "/templates";
    private List<String> profileNames;
    private Gson gson;


    public ProfileManager() {
        this.profileNames = new ArrayList<>();
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Operation.class, new OperationDeserializer());
        this.gson = gsonBuilder.create();

        createDirectoryIfNotExists(PROFILES_DIR);
        createDirectoryIfNotExists(TEMPLATES_DIR);
        
        loadProfileNames();
    }

    /**
     * Get the GSON object used to serialize and deserialize profiles
     * @return
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Get a List of of all profile names as Strings
     * @return
     */
    public List<String> getProfileNames() {
        return profileNames;
    }


    /**
     * Given a profile name as a String returns a {@link Profile} object representing the profile
     * @param name
     * @return Profile
     */
    public Profile loadProfile(String name) {
        try {
            FileReader reader = new FileReader(getProfilePath(name));
            Type profileType = new TypeToken<Profile>() {}.getType();
            Profile profile = gson.fromJson(reader, profileType);
            reader.close();
            return profile;
        } catch (FileNotFoundException e) {
            System.out.println("Profile file not found: " + name);
            e.printStackTrace();  // Print stack trace for FileNotFoundException
            return null;
        } catch (IOException e) {
            System.out.println("Error reading profile file: " + e.getMessage());
            e.printStackTrace();  // Print stack trace for IOException
            return null;
        }
    }

    /**
     * Adds a profile to the list of profiles and saves it to a file
     * @param profile
     */
    public void addProfile(Profile profile) {
        profileNames.add(profile.getName());
        saveProfile(profile);
    }

    /**
     * Given the name of a proifle as a String.
     * Removes a profile from the list of profiles and deletes the file
     * @param name
     */
    public void removeProfile(String name) {
        profileNames.remove(name);
        try {
            Files.delete(Paths.get(getProfilePath(name)));
        } catch (IOException e) {
            System.out.println("Error deleting profile file: " + e.getMessage());
        }
    }

    /**
     * Given a profile as a {@link Profile} object.
     * Saves the profile to a file
     * @param profile
     */
    public void updateProfile(Profile profile) {
        saveProfile(profile);
    }

    
    /**
     * Checks if a profile exists given a profile name as a String
     * Retruns true if the profile exists, false otherwise
     * @param name
     * @return
     */
    public boolean profileExists(String name) {
        return profileNames.contains(name);
    }

    /**
     * Given a profile name as a String.
     * creates a new profile with the given name and saves it to a file
     */
    public void createProfile(String name) {
        Profile newProfile = new Profile(name);
        profileNames.add(name);
        saveProfile(newProfile);
    }

    /**
     * Given a profile as a {@link Profile} object.
     * Saves the profile to a file
     * @param profile
     */
    public void saveProfile(String name) {
        Profile profile = loadProfile(name);
        if (profile != null) {
            saveProfile(profile);
        }
    }


    /**
     * Given the name of a profile as a String.
     * Loads the profile and reformats it as a pretty String and returns it
     * The pretty String is used to display the profile in the GUI
     * @param profileName
     * @return
     */
    public ArrayList<String> operationsAsPrettyString(String profileName) {
        ArrayList<String> prettyOperations = new ArrayList<>();
        Profile profile = loadProfile(profileName);
        if (profile != null) {
            prettyOperations.add("Profile Name: " + profile.getName());
            prettyOperations.add("Template Path: " + profile.getTemplatePath());
            prettyOperations.add("Naming Convention : " + profile.getNamingConvention());
            for (Operation<?> operation : profile.getOperations()) {
                Map<String, ?> params = operation.getParameters();
                StringBuilder main_sb = new StringBuilder();
                main_sb.append(operation.getType().toString());
                main_sb.append(" - ");
                main_sb.append("Source Sheet: " +params.get("srcSheet"));
                main_sb.append(" - ");
                main_sb.append("Destination Sheet: " +params.get("dstSheet"));
                prettyOperations.add(main_sb.toString());
                if (operation.getType() == OpType.COPY_SPLIT_ROW) {
                    @SuppressWarnings("unchecked")
                    Map<Double, Object> colMap = (Map<Double, Object>) params.get("colMap");
                    for (Map.Entry<Double, Object> entry : colMap.entrySet()) {
                        String col_str = ("    Col:" + entry.getKey().toString() + " copy to Col:" + entry.getValue().toString());
                        prettyOperations.add(col_str);
                    }
                }else{
                    String col_str = ("    Col:" + params.get("srcCol") + " copy to Col:" + params.get("dstCol"));
                    prettyOperations.add(col_str);
                }
            }
        }
        return prettyOperations;
    }
    
    /**
     * Given a profile name as a String and an index of a line in the pretty string of the profile
     * Returns the index of the operation in the profile that corresponds to the line in the pretty string
     * The pretty String is used to display the profile in the GUI
     * @param profileName
     * @param prettyStringIndex
     * @return operationIndex
     */
    public int getOperationIndexFromPrettyStringIndex(String profileName, int prettyStringIndex) {
        Profile profile = loadProfile(profileName);
        if (profile != null) {
            int operationIndex = -1;
            int currentIndex = 3; // Start from the index after the initial elements (Profile Name, Template Path, and Naming Convention)
            for (int i = 0; i < profile.getOperations().size(); i++) {
                if (currentIndex == prettyStringIndex) {
                    operationIndex = i;
                    break;
                }
                currentIndex++; // Increment for the main operation line
                Operation<?> operation = profile.getOperations().get(i);
                if (operation.getType() == OpType.COPY_SPLIT_ROW) {
                    @SuppressWarnings("unchecked")
                    Map<Double, Object> colMap = (Map<Double, Object>) operation.getParameters().get("colMap");
                    currentIndex += colMap.size(); // Increment for each column mapping line
                } else {
                    currentIndex++; // Increment for the single column line
                }
            }
            return operationIndex;
        }
        return -1;
    }

    /**
     * Returns a list of all avaialble templates in the templates directory
     * @return
     */
    public List<String> getAvailableTemplates() {
        List<String> templates = new ArrayList<>();
        File templatesDir = new File(TEMPLATES_DIR);
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
        }
        for (File file : templatesDir.listFiles()) {
            String fileName = file.getName();
            if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                templates.add(fileName);
            }
        }
        System.out.println("Available templates: " + templates);
        return templates;
    }
    
    private void loadProfileNames() {
        try {
            Path profilesDirectory = Paths.get(PROFILES_DIR);
            if (!Files.exists(profilesDirectory)) {
                System.out.println("Profiles directory does not exist: " + PROFILES_DIR);
                return;
            }
            profileNames = Files.list(profilesDirectory)
                    .map(path -> path.getFileName().toString().replace(".json", ""))
                    .collect(Collectors.toList());
            System.out.println("Loaded profile names: " + profileNames);
        } catch (IOException e) {
            System.out.println("Error reading profiles directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

     private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    

    private void saveProfile(Profile profile) {
        try {
            FileWriter writer = new FileWriter(getProfilePath(profile.getName()));
            gson.toJson(profile, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving profile: " + e.getMessage());
        }
    }

    private String getProfilePath(String name) {
        return PROFILES_DIR + File.separator + name + ".json";
    }
}