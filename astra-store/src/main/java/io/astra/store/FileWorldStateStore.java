package io.astra.store;

import io.astra.api.WorldState;
import io.astra.api.WorldStates;
import io.astra.api.store.WorldStateStore;
import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/** A {@link WorldStateStore} that persists state to individual files on disk. */
public class FileWorldStateStore implements WorldStateStore {
    private final Path baseDir;

    public FileWorldStateStore(Path baseDir) {
        this.baseDir = baseDir;
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create store directory: " + baseDir, e);
        }
    }

    private Path fileFor(String sessionId) {
        return baseDir.resolve(sessionId.replaceAll("[^a-zA-Z0-9_.-]", "_") + ".state");
    }

    @Override
    public void save(String sessionId, WorldState state) {
        Path file = fileFor(sessionId);
        Properties props = new Properties();
        props.putAll(state.asMap());
        try (OutputStream os = Files.newOutputStream(file)) {
            props.store(os, "Astra WorldState: " + sessionId);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save state: " + sessionId, e);
        }
    }

    @Override
    public Optional<WorldState> load(String sessionId) {
        Path file = fileFor(sessionId);
        if (!Files.exists(file)) return Optional.empty();
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(file)) {
            props.load(is);
        } catch (IOException e) {
            return Optional.empty();
        }
        Map<String, String> map = new java.util.LinkedHashMap<>();
        for (String key : props.stringPropertyNames()) {
            map.put(key, props.getProperty(key));
        }
        return Optional.of(WorldStates.of(map));
    }

    @Override
    public void delete(String sessionId) {
        try {
            Files.deleteIfExists(fileFor(sessionId));
        } catch (IOException ignored) {}
    }

    @Override
    public boolean exists(String sessionId) {
        return Files.exists(fileFor(sessionId));
    }
}
