package com.gmail.at.kotamadeo.server.util;

import lombok.experimental.UtilityClass;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@UtilityClass
public final class ServerUtil {
    private static List<String> validPaths;
    private static Properties properties;
    private static List<String> allowedMethods;

    public static void addPathToValidPaths(String path) {
        validPaths.add(path);
    }

    public static void removePathFromValidPaths(String path) {
        validPaths.remove(path);
    }

    public static List<String> getAllowedMethods() {
        if (allowedMethods == null) {
            allowedMethods = new ArrayList<>();
            allowedMethods.add(getPropertyByKey("server.methodPost"));
            allowedMethods.add(getPropertyByKey("server.methodGet"));
        }
        return allowedMethods;
    }

    public static void addAllowedMethods(String method) {
        allowedMethods.add(method);
    }

    public static void removeAllowedMethods(String method) {
        allowedMethods.remove(method);
    }

    public static List<String> getValidPaths() {
        if (validPaths == null) {
            validPaths = new ArrayList<>();
            try {
                validPaths.addAll(Files.readAllLines(Path.of("src/main/resources", "valid-paths.txt")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return validPaths;
    }

    private static void loadProperties() {
        properties = new Properties();
        Path path = Path.of("src/main/resources", "server.properties");
        try (FileReader fileReader = new FileReader(path.toFile())) {
            properties.load(fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPropertyByKey(String key) {
        if (properties == null) {
            loadProperties();
        }
        return properties.getProperty(key);
    }
}
