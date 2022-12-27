package at.matteovalentini.configdeserializer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to deserialize a config file into a config class using reflection.
 * The config class must have a default constructor and config fields should be annotated with @ConfigValue.
 */
public class ConfigDeserializer {

    private static final List<Class<?>> validTypes = List.of(new Class<?>[]{
            String.class,
            int.class,
            boolean.class,
            double.class,
            float.class,
            long.class,
            short.class,
            byte.class,
    });

    /**
     * Deserializes a config file into a config class. If the config file doesn't exist, it will be created and populated with the default values
     * @param configFile Path to the config file
     * @param configClass Class of the config
     * @param <T> Type of the config class
     * @return The config class
     * @throws ConfigException.InvalidPathException If the path is invalid
     * @throws ConfigException.InvalidClassException If the config class is invalid
     * @throws ConfigException.MissingValueException If a required value is missing
     * @throws ConfigException.InvalidValueException If a value is invalid
     */
    public static <T> T deserialize(Path configFile, Class<T> configClass) throws ConfigException.InvalidClassException,
                ConfigException.InvalidPathException, ConfigException.InvalidValueException, ConfigException.MissingValueException {

        HashMap<Field, ConfigValue> configValues;
        T config;

        try {
            configValues = getAnnotatedFields(configClass);
            Constructor<T> constructor = configClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            config = constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ConfigException.InvalidClassException("The class " + configClass.getName() + " has no accessible constructor without parameters");
        } catch (SecurityException e) {
            throw new ConfigException.InvalidClassException("Encountered SecurityException reading fields of class " + configClass.getName());
        }

        if (configValues.size() == 0)
            throw new ConfigException.InvalidClassException("The class " + configClass.getName() + " has no fields annotated with @ConfigValue");

        if (!Files.exists(configFile))
           createNewConfig(configFile, configValues);

        HashMap<String, String> configMap = readConfig(configFile);

        for (Map.Entry<Field, ConfigValue> entry : configValues.entrySet()) {
            Field field = entry.getKey();
            ConfigValue annotation = entry.getValue();
            String name = annotation.name().length() > 0 ? annotation.name() : field.getName();
            String value = configMap.get(name);
            if (value == null) {
                if (annotation.required())
                    throw new ConfigException.MissingValueException("The field " + field.getName() + " is required but missing in the config file");
            } else {
                try {
                    field.setAccessible(true);
                    field.set(config, parseValue(value, field, annotation));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new ConfigException.InvalidValueException("The value \"" + value + "\" is invalid for key " + name);
                }
            }
        }

        return config;
    }

    private static HashMap<Field, ConfigValue> getAnnotatedFields(Class<?> configClass) throws ConfigException.InvalidClassException {
        HashMap<Field, ConfigValue> fields = new HashMap<>();
        for (Field curr : configClass.getDeclaredFields()) {
            if (curr.isAnnotationPresent(ConfigValue.class)) {
                if (validTypes.contains(curr.getType()) || curr.getType().isEnum())
                    fields.put(curr, curr.getAnnotation(ConfigValue.class));
                else
                    throw new ConfigException.InvalidClassException("The field " + curr.getName() + " has an invalid type");
            }
        }
        return fields;
    }

    private static void createNewConfig(Path configFile, HashMap<Field, ConfigValue> fields) throws ConfigException.InvalidPathException {
        try {
            Files.createFile(configFile);
            StringBuilder sb = new StringBuilder();
            sb.append("============] Config [============\n#\n# Syntax: key: value\n#\n# Comments: # this is a comment\n#\n==================================\n\n");
            fields.forEach((field, annotation) -> {
                if (annotation.description().length() > 0)
                    sb.append("# ").append(annotation.description()).append("\n");
                sb.append(annotation.name().length() > 0 ? annotation.name() : field.getName()).append(": ").append(annotation.defaultValue()).append("\n\n");
            });
            String content = sb.toString();
            content = content.substring(0, content.length() - 1);
            Files.writeString(configFile, content);
        } catch (IOException exception) {
            throw new ConfigException.InvalidPathException("Could not create config file at " + configFile.toString());
        }
    }

    private static HashMap<String, String> readConfig(Path configFile) throws ConfigException.InvalidPathException {
        HashMap<String, String> config = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(configFile);
            lines.forEach(line -> {
                if (line.startsWith("#"))
                    return;
                int index = line.indexOf(":");
                if (index == -1)
                    return;
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                if (key.length() > 0 && value.length() > 0 && !config.containsKey(key))
                    config.put(key, value);
            });
        } catch (IOException e) {
            throw new ConfigException.InvalidPathException("Could not read config file at " + configFile.toString());
        }
        return config;
    }

    private static Object parseValue(String value, Field field, ConfigValue annotation) throws ConfigException.InvalidValueException {
        String name = annotation.name().length() > 0 ? annotation.name() : field.getName();
        if (field.getType() == String.class) {
            String val = value;
            if (annotation.limited() && val.length() > annotation.max())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too long for " + name);
            else if (annotation.limited() && val.length() < annotation.min())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too short for " + name);
            return val;
        } else if (field.getType() == byte.class) {
            byte val;
            try {
                val = Byte.parseByte(value);
            } catch (NumberFormatException e) {
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is not a valid byte for " + name);
            }
            if (annotation.limited() && val > annotation.max())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too big for " + name);
            else if (annotation.limited() && val < annotation.min())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too small for " + name);
            return val;
        } else if (field.getType() == short.class) {
            short val;
            try {
                val = Short.parseShort(value);
            } catch (NumberFormatException e) {
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is not a valid short for " + name);
            }
            if (annotation.limited() && val > annotation.max())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too big for " + name);
            else if (annotation.limited() && val < annotation.min())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too small for " + name);
            return val;
        } else if (field.getType() == int.class) {
            int val;
            try {
                val = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is not a valid int for " + name);
            }
            if (annotation.limited() && val > annotation.max())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too big for " + name);
            else if (annotation.limited() && val < annotation.min())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too small for " + name);
            return val;
        } else if (field.getType() == long.class) {
            long val;
            try {
                val = Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is not a valid long for " + name);
            }
            if (annotation.limited() && val > annotation.max())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too big for " + name);
            else if (annotation.limited() && val < annotation.min())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too small for " + name);
            return val;
        } else if (field.getType() == float.class) {
            float val;
            try {
                val = Float.parseFloat(value);
            } catch (NumberFormatException e) {
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is not a valid float for " + name);
            }
            if (annotation.limited() && val > annotation.max())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too big for " + name);
            else if (annotation.limited() && val < annotation.min())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too small for " + name);
            return val;
        } else if (field.getType() == double.class) {
            double val;
            try {
                val = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is not a valid double for " + name);
            }
            if (annotation.limited() && val > annotation.max())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too big for " + name);
            else if (annotation.limited() && val < annotation.min())
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is too small for " + name);
            return val;
        } else if (field.getType() == boolean.class) {
            boolean val;
            try {
                val = Boolean.parseBoolean(value);
            } catch (NumberFormatException e) {
                throw new ConfigException.InvalidValueException("The value \"" + value + "\" is not a valid boolean for " + name);
            }
            return val;
        } else if (field.getType().isEnum()) {
            Object[] enumConstants = field.getType().getEnumConstants();
            for (Object enumConstant : enumConstants) {
                if (enumConstant.toString().equalsIgnoreCase(value))
                    return enumConstant;
            }
            throw new ConfigException.InvalidValueException("The value \"" + value + "\" is not a valid enum for " + name);
        }
        return null;
    }
}
