package ch.hslu.vsk.stringpersistor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ch.hslu.vsk.stringpersistor.api.StringPersistor;
import ch.hslu.vsk.stringpersistor.api.PersistedString;

/**
 * Persists messages to files.
 */
public class FileStringPersistor implements StringPersistor {

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

        var persistedString = new PersistedString(timestamp, payload);

        try (var bw = new BufferedWriter(new FileWriter(this.filePath.toString(), StandardCharsets.UTF_8, true))) {
            bw.write(persistedString.getTimestamp().toString());
            bw.write(": ");
            bw.write(persistedString.getPayload());
            bw.newLine();
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

        var readPersistedStings = new ArrayList<PersistedString>();

        try (var reader = new BufferedReader(new FileReader(this.filePath.toString(), StandardCharsets.UTF_8))) {
            String nextLine;
            var counter = 1;
            while ((nextLine = reader.readLine()) != null && counter <= count) {
                counter++;
                var values = nextLine.split(": ", 2);
                readPersistedStings.add(new PersistedString(Instant.parse(values[0]), values[1]));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return readPersistedStings;
    }
}
