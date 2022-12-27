# ConfigDeserializer
 
A Java package used to generate and deserialize config files. Simply annotate fields with ```@ConfigValue``` to make them a config property.

# Install (Maven Central)

tbd.

# Usage

First, create a config class and annotate all fields that should be included in the config file with ```@ConfigValue``` (see the table below for options). Supported types are ```byte, short, int, long, float, double, boolean, String``` and ```Class<? extends Enum>```. Example class:

```java
public class Config {

	@ConfigValue(description = "The name of the user")
	private String name;

	@ConfigValue(min = 0, max = 100)
	private int age;

	@ConfigValue(defaultValue = "true")
	private boolean isCool;
}
```

Then, call ```ConfigDeserializer.deserialize(Path configFilePath, Class<T> configClass)``` to deserialize the config file. If the file does not exist, it will be created with the default values. Example:

```java
Config config = ConfigDeserializer.deserialize(Paths.get("config.json"), Config.class);
```

## @ConfigValue parameters

NOTE: All parameters are optional.

| Parameter | Type | Description |
|-----------|------|-------------|
| name | String | The name of the config property. If not specified, the field name will be used. |
| required | boolean | Whether the config property is required. If true, a ```MissingValueExeception``` will be thrown when deserializing a config file with a missing value |
| description | String | A description of the config property, which will be included in the config file. |
| defaultValue | String | The default value of the config property. |
| min | int | The minimum value (or length if it is a string) of the config property. Only applies if ```limited``` is set to ```true``` |
| max | int | The maximum value (or length if it is a string) of the config property. Only applies if ```limited``` is set to ```true``` |
| limited | boolean | Whether the config property has a minimum and maximum value (or length if it is a string). |

## Config file format

A config file generated from the above example would look like this:

```
============] Config [============
#
# Syntax: key: value
#
# Comments: # this is a comment
#
==================================

isCool: true

# The name of the user
name: 

age: 
```

# License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
