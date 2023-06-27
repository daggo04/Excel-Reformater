package com.eyeshare.Dag.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;



public class ResourcesExtractor {

    private PrintWriter printWriter;
    public boolean createLog;

    public ResourcesExtractor(boolean createLog) throws IOException {
        File logDirectory = new File(System.getProperty("user.home") + "/.Excel_Reformatter_Resources");
        if (!logDirectory.exists()) {
            if (!logDirectory.mkdirs()) {
                throw new IOException("Could not create log directory: " + logDirectory);
            }
        }
        if (createLog){
            File logFile = new File(logDirectory, "log.txt");
            this.printWriter = new PrintWriter(new FileOutputStream(logFile, true));
            System.setOut(new PrintStream(new FileOutputStream(logFile)));
            System.setErr(new PrintStream(new FileOutputStream(logFile)));
        }
    }


    public void extractResources() throws IOException {
        if (createLog) {
            // print the java version
            printWriter.println("Java Version: " + System.getProperty("java.version"));
            printWriter.println("Extracting Resources...");
            // print the classpath
            printWriter.println("Classpath: " + System.getProperty("java.class.path"));

            // print the class loader
            printWriter.println("Class Loader: " + getClass().getClassLoader());

            extractDirectory("/templates");
            extractDirectory("/profiles");
        }
    }

    public void close() {
        printWriter.close();
    }

    public static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(srcPath -> {
            try {
                Path destPath = target.resolve(source.relativize(srcPath));
                try {
                    Files.copy(srcPath, destPath);
                } catch (FileAlreadyExistsException e) {
                    // Ignore
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private void extractDirectory(String directory) throws IOException {
        File outDir = new File(System.getProperty("user.home") + "/.Excel_Reformatter_Resources" + directory);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        String classpath = System.getProperty("java.class.path");

        if (classpath.contains(".app")) {
            // If running from an .app bundle on MacOS
            int appIndex = classpath.indexOf(".app");
            int pathEnd = classpath.indexOf("/", appIndex);
            String appPath = classpath.substring(0, pathEnd);
            String resourceDirPath = appPath + "/Contents/app/classes" + directory;
            File resourceDir = new File(resourceDirPath);
            copyDirectory(resourceDir.toPath(), outDir.toPath());
        } else {
            URL dirUrl = getClass().getResource(directory);
            System.out.println("URL for " + directory + ": " + dirUrl);

            if (dirUrl != null && dirUrl.getProtocol().equals("file")) {
                // Resources are available as plain files, probably running from an IDE
                File inDir = new File(dirUrl.getPath());
                copyDirectory(inDir.toPath(), outDir.toPath());
            } else {
                // Resources are not available as plain files, probably running from a JAR
                String[] resources = getResourceListing(getClass(), directory);
                System.out.println("Resources in " + directory + ": " + Arrays.toString(resources));
                for (String resource : resources) {
                    File outFile = new File(outDir, resource);
                    System.out.println("Outfile: " + outFile);
                    if (!outFile.exists()) {
                        InputStream inStream = getClass().getResourceAsStream(directory + "/" + resource);
                        System.out.println("InputStream for " + directory + "/" + resource + ": " + inStream);
                        Files.copy(inStream, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }





    /**
     * Retrieves all the resources from the resource folder
     * (See https://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory)
     * @param clazz any java class that lives in the same place as the resources you want.
     * @param path should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws IOException
     */
    private String[] getResourceListing(Class<?> clazz, String path) throws IOException {
        URL dirURL = clazz.getResource(path);
        System.out.println("Directory URL: " + dirURL);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            try {
                return new File(dirURL.toURI()).list();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URI", e);
            }
        }

        if (dirURL == null) {
            /* In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/")+".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            System.out.println("Jar path: " + jarPath);
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    System.out.println("Jar entry name: " + name);
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just continue with the next iteration
                        continue;
                    }
                    result.add(entry);
                }
            }
            System.out.println("Resource entries in " + path + ": " + result);
            return result.toArray(new String[0]);

        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }
}
