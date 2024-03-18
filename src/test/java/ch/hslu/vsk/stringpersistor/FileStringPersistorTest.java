package ch.hslu.vsk.stringpersistor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;

import ch.hslu.vsk.stringpersistor.api.PersistedString;
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
    public void TestStringPersistor(@TempDir Path tempDir) {

        // Arrange
        var testMessage = "This is a test message , äöü ê] :) $";
        var timeStamp = Instant.now();
        Path file = tempDir.resolve("temp_log.txt");
        var stringPersistor = new FileStringPersistor();

        // Act
        stringPersistor.setFile(file);
        stringPersistor.save(timeStamp, testMessage);
        var answer = stringPersistor.get(Integer.MAX_VALUE);

        // Assert
        assertEquals(answer.getFirst(),new PersistedString(timeStamp, testMessage));
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void TestStringPersistorNoFileSetOnSave() {

        // Arrange
        var testMessage = "Foo";
        var timeStamp = Instant.now();
        var stringPersistor = new FileStringPersistor();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> stringPersistor.save(timeStamp, testMessage));
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void TestStringPersistorNoFileSetOnGet() {

        // Arrange
        var stringPersistor = new FileStringPersistor();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> stringPersistor.get(Integer.MAX_VALUE));
    }

    /**
     * TestCase for {@link FileStringPersistor}}.
     */
    @Test
    public void TestStringPersistor_1100Entries(@TempDir Path tempDir) {

        // Arrange
        var stringPersistor = new FileStringPersistor();
        Path file = tempDir.resolve("temp_log.txt");

        stringPersistor.setFile(file);
        for (int i = 0; i < 1100; i++) {
            stringPersistor.save(Instant.now(), "Test message nr" + i);
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
    public void TestStringPersistor_ReadLessThanAvailableEntries(@TempDir Path tempDir) {

        // Arrange
        var stringPersistor = new FileStringPersistor();
        Path file = tempDir.resolve("temp_log.txt");

        stringPersistor.setFile(file);
        for (int i = 0; i < 4; i++) {
            stringPersistor.save(Instant.now(), "Test message nr" + i);
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
    public void TestStringPersistor_Read100EntriesInLessThan200ms(@TempDir Path tempDir) {

        // Arrange
        var stringPersistor = new FileStringPersistor();
        Path file = tempDir.resolve("temp_log.txt");

        stringPersistor.setFile(file);
        String message = String.join("", Collections.nCopies(1100, "X"));
        for (int i = 0; i < 100; i++) {
            stringPersistor.save(Instant.now(), message);
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
