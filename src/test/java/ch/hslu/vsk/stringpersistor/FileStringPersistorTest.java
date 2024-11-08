package ch.hslu.vsk.stringpersistor;

import ch.hslu.vsk.stringpersistor.api.PersistedString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testcases for StringPersistorTest.
 */
final class FileStringPersistorTest {

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void testStringPersistor(@TempDir Path tempDir) {

        // Arrange
        var testMessage = "This is a test message , äöü ê] \n :) $";
        var timeStamp = Instant.parse("2024-05-01T10:10:00.00Z");
        Path file = tempDir.resolve("temp_log.txt");
        var stringPersistor = new FileStringPersistor();

        // Act
        stringPersistor.setFile(file);
        stringPersistor.save(timeStamp, testMessage);
        var answer = stringPersistor.get(Integer.MAX_VALUE);

        // Assert
        assertEquals(new PersistedString(timeStamp, testMessage.replaceAll("\\r?\\n", " ")), answer.getFirst());
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void testStringPersistorGetWithoutFirstSaving(@TempDir Path tempDir) {

        // Arrange
        Path file = tempDir.resolve("temp_log.txt");
        var stringPersistor = new FileStringPersistor();

        // Act
        stringPersistor.setFile(file);
        var answer = stringPersistor.get(Integer.MAX_VALUE);

        // Assert
        assertEquals(0, answer.size());
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void testStringPersistorPathNotFound(@TempDir Path tempDir) {

        // Arrange
        var testMessage = "This is a test message , äöü ê] :) $";
        var timeStamp = Instant.parse("2024-05-01T10:10:00.00Z");
        Path file = tempDir.resolve("Logs").resolve("temp_log.txt");
        var stringPersistor = new FileStringPersistor();

        // Act
        stringPersistor.setFile(file);
        stringPersistor.save(timeStamp, testMessage);
        var answer = stringPersistor.get(Integer.MAX_VALUE);

        // Assert
        assertEquals(new PersistedString(timeStamp, testMessage), answer.getFirst());
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void testStringPersistorNoFileSetOnSave() {

        // Arrange
        var testMessage = "Foo";
        var timeStamp = Instant.parse("2024-05-01T10:10:00.00Z");
        var stringPersistor = new FileStringPersistor();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> stringPersistor.save(timeStamp, testMessage));
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void testStringPersistorNoFileSetOnGet() {

        // Arrange
        var stringPersistor = new FileStringPersistor();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> stringPersistor.get(Integer.MAX_VALUE));
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void testStringPersistor_1100Entries(@TempDir Path tempDir) {

        // Arrange
        var stringPersistor = new FileStringPersistor();
        Path file = tempDir.resolve("temp_log.txt");

        stringPersistor.setFile(file);
        for (int i = 0; i < 1100; i++) {
            stringPersistor.save(Instant.parse("2024-05-01T10:10:00.00Z"), "Test message nr" + i);
        }

        // Act
        var answer = stringPersistor.get(Integer.MAX_VALUE);

        // Assert
        assertThat(answer.size()).isEqualTo(1100);
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void testStringPersistor_ReadLessThanAvailableEntries(@TempDir Path tempDir) {

        // Arrange
        var stringPersistor = new FileStringPersistor();
        Path file = tempDir.resolve("temp_log.txt");

        stringPersistor.setFile(file);
        for (int i = 0; i < 4; i++) {
            stringPersistor.save(Instant.parse("2024-05-01T10:10:00.00Z"), "Test message nr" + i);
        }

        // Act
        var answer = stringPersistor.get(2);

        // Assert
        assertThat(answer.size()).isEqualTo(2);
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void testStringPersistor_Read100EntriesInLessThan200ms(@TempDir Path tempDir) {

        // Arrange
        var stringPersistor = new FileStringPersistor();
        Path file = tempDir.resolve("temp_log.txt");

        stringPersistor.setFile(file);
        String message = String.join("", Collections.nCopies(1100, "X"));
        for (int i = 0; i < 100; i++) {
            stringPersistor.save(Instant.parse("2024-05-01T10:10:00.00Z"), message);
        }

        // Act
        long startTime = System.nanoTime();
        stringPersistor.get(Integer.MAX_VALUE);
        long endTime = System.nanoTime();
        double elapsedMs = (endTime - startTime) / 1E6;

        // Assert
        assertThat(elapsedMs).isLessThanOrEqualTo(200);
    }
}
