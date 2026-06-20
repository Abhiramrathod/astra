package io.astra.config;

import io.astra.api.config.AstraConfig;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class PropertiesFileConfigProvider extends MapConfigProvider {
    public PropertiesFileConfigProvider(String path) {
        load(Path.of(path));
    }

    public PropertiesFileConfigProvider(Path path) {
        load(path);
    }

    private void load(Path path) {
        if (!Files.exists(path)) return;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config from " + path, e);
        }
        for (String key : props.stringPropertyNames()) {
            set(key, props.getProperty(key));
        }
    }
}
