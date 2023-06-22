package com.eyeshare.Dag.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;



public class ResourcesExtractor {

    public void extractResources() throws IOException {
        extractDirectory("/templates");
        extractDirectory("/profiles");
    }

    private void extractDirectory(String directory) throws IOException {
        File outDir = new File(System.getProperty("user.home") + "/.Excel_Reformatter_Resources" + directory);
        if (!outDir.exists()) {
            outDir.mkdirs();
            String[] resources = getResourceListing(getClass(), directory);
            for (String resource : resources) {
                File outFile = new File(outDir, resource);
                if (!outFile.exists()) {
                    InputStream inStream = getClass().getResourceAsStream(directory + "/" + resource);
                    Files.copy(inStream, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    result.add(entry);
                }
            }
            return result.toArray(new String[0]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }
}