import at.matteovalentini.configdeserializer.ConfigDeserializer;
import at.matteovalentini.configdeserializer.ConfigException;
import classes.ConfigClass;
import classes.InvalidConfigClass;
import classes.ParseConfigClass;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigCreationTest {

    private static final Path configFile = Paths.get("testConfig.cfg");

    @Test
    public void testInvalidClass() throws IOException {
        assertThrows(ConfigException.InvalidClassException.class, () -> ConfigDeserializer.deserialize(configFile, InvalidConfigClass.class));
        Files.deleteIfExists(configFile);
    }

    @Test
    public void testConfigCreation() throws ConfigException.InvalidValueException, ConfigException.MissingValueException, ConfigException.InvalidClassException, ConfigException.InvalidPathException, IOException {
        ConfigDeserializer.deserialize(configFile, ConfigClass.class);
        assertTrue(Files.exists(configFile));
        String content = new String(Files.readAllBytes(configFile));
        assertTrue(content.contains("stringField123:"));
        assertTrue(content.contains("intField123: 69"));
        assertTrue(content.contains("booleanField123:"));
        assertTrue(!content.contains("doubleField123:"));
        Files.deleteIfExists(configFile);
    }

    @Test
    public void testConfigFileParsing() throws ConfigException.InvalidValueException, ConfigException.MissingValueException, ConfigException.InvalidClassException, ConfigException.InvalidPathException, IOException {
        ParseConfigClass parsed = ConfigDeserializer.deserialize(configFile, ParseConfigClass.class);
        assertTrue(parsed.stringField.equals("Hello World"));
        Files.deleteIfExists(configFile);
    }
}
