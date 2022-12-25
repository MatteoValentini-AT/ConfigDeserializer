package at.matteovalentini.configdeserializer;

/**
 * Class containing all the exceptions that can be thrown by the ConfigDeserializer
 */
public class ConfigException {

    /**
     * Exception thrown when the config file is not found or the path is invalid
     */
    public static class InvalidPathException extends Exception {
        InvalidPathException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when the config class is invalid (no empty constructor, no @ConfigValue annotation)
     */
    public static class InvalidClassException extends Exception {
        InvalidClassException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when the config file contains a value that cannot be parsed
     */
    public static class InvalidValueException extends Exception {
        InvalidValueException(String message) { super(message); }
    }

    /**
     * Exception thrown when the config file is missing a required value
     */
    public static class MissingValueException extends Exception {
        MissingValueException(String message) { super(message); }
    }
}
