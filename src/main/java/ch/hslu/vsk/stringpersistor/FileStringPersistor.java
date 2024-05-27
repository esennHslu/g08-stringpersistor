package ch.hslu.vsk.stringpersistor;

import ch.hslu.vsk.stringpersistor.api.PersistedString;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

/**
 * Persists messages to files.
 */
public class FileStringPersistor implements StringPersistor {

    private static final String DELIMITER = ": ";
    private Path filePath;

    /**
     * Sets the logfile path. (upon saving: If no file exists, one is created, otherwise log is appended).
     *
     * @param path Path to the logfile.
     */
    @Override
    public synchronized void setFile(final Path path) {
        filePath = path;
    }

    /**
     * Appends information to the configured logfile. If file does not exist, it is created.
     *
     * @param timestamp Time of the message.
     * @param payload   The message.
     */
    @Override
    public synchronized void save(final Instant timestamp, final String payload) {

        if (filePath == null) {
            throw new IllegalStateException("StringPersistor file path not set");
        }

        String inlinedPayload = payload.replace("\r\n", " ").replace("\n", " ");

        try {
            Path parentDir = filePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Files.write(filePath, buildByteString(timestamp, inlinedPayload), CREATE, WRITE, APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets the last n messages from the configured logfile.
     *
     * @param count Maximum amount of messages to get.
     * @return A list of the last n persisted strings of the currently configured logfile.
     */
    @Override
    public synchronized List<PersistedString> get(final int count) {
        if (filePath == null) {
            throw new IllegalStateException("StringPersistor file path not set");
        }

        if (!Files.exists(filePath)) {
            return Collections.emptyList();
        }

        List<PersistedString> readPersistedStrings;

        try {
            List<String> lines = Files.readAllLines(filePath);
            readPersistedStrings = convertToPersistedStringList(lines, count);
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't read file: " + filePath, e);
        }

        return readPersistedStrings;
    }

    private byte[] buildByteString(final Instant timestamp, final String payload) {
        var formattedTime = InstantHelper.format(timestamp);
        return (formattedTime + DELIMITER + payload + System.lineSeparator()).getBytes();
    }

    private List<PersistedString> convertToPersistedStringList(final List<String> lines, final int amount) {
        return lines.stream()
                .limit(amount)
                .map(this::extractPersistedString)
                .toList();
    }

    private PersistedString extractPersistedString(final String fileLine) {
        String[] temp = fileLine.split(DELIMITER, 2);
        Instant timestamp = InstantHelper.parse(temp[0]);
        String payload = temp[1];
        return new PersistedString(timestamp, payload);
    }
}
