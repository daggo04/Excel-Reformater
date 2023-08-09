package com.eyeshare.Dag.utils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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
        File logDirectory = new File(System.getProperty("user.home") + "/.excelreformatter");
        if (!logDirectory.exists() && !logDirectory.mkdirs()) {
            throw new IOException("Could not create log directory: " + logDirectory);
        }
        if (createLog) {
            File logFile = new File(logDirectory, "log.txt");
            this.printWriter = new PrintWriter(new FileOutputStream(logFile, true));
            System.setOut(new PrintStream(new FileOutputStream(logFile)));
            System.setErr(new PrintStream(new FileOutputStream(logFile)));
        }
    }

    public void extractResources() throws IOException {
        log("Extracting Resources...");
        extractDirectory("/templates");
        extractDirectory("/profiles");
    }

    public void close() {
        if (printWriter != null) {
            printWriter.close();
        }
    }

    private void log(String message) {
        if (createLog && printWriter != null) {
            printWriter.println(message);
        }
    }

    private void extractDirectory(String directory) throws IOException {
        File outDir = new File(System.getProperty("user.home") + "/.excelreformatter" + directory);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        URL dirUrl = getClass().getResource(directory);

        if (dirUrl != null && dirUrl.getProtocol().equals("file")) {
            // Resources are available as plain files, probably running from an IDE
            File inDir = new File(dirUrl.getPath());
            copyDirectory(inDir.toPath(), outDir.toPath());
        } else {
            // Resources are not available as plain files, probably running from a JAR
            String[] resources = getResourceListing(getClass(), directory);
            log("Resources in " + directory + ": " + Arrays.toString(resources));
            for (String resource : resources) {
                File outFile = new File(outDir, resource);
                if (!outFile.exists()) {
                    try (InputStream inStream = getClass().getResourceAsStream(directory + "/" + resource)) {
                        Files.copy(inStream, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(srcPath -> {
            try {
                Path destPath = target.resolve(source.relativize(srcPath));
                if (!Files.exists(destPath)) {
                    Files.copy(srcPath, destPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String[] getResourceListing(Class<?> clazz, String path) throws IOException {
        URL dirURL = clazz.getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            try {
                return new File(dirURL.toURI()).list();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URI", e);
            }
        }

        if (dirURL == null) {
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));
            Enumeration<JarEntry> entries = jar.entries();
            Set<String> result = new HashSet<>();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) {
                    String entry = name.substring(path.length());
                    if (entry.indexOf("/") < 0) {
                        result.add(entry);
                    }
                }
            }
            return result.toArray(new String[0]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }
}
