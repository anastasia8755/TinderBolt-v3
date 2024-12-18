package com.javarush.telegram.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BotInfoReader {
    private static Properties properties;

    static {
        try (FileInputStream fis = new FileInputStream("src/main/resources/bot.properties")) {
            properties = new Properties();
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config properties file.", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
