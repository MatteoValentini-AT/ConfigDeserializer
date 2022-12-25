package classes;

import at.matteovalentini.configdeserializer.ConfigValue;

public class ConfigClass {

    @ConfigValue
    public String stringField123;

    @ConfigValue(name = "intField123", defaultValue = "69")
    public int intField;

    @ConfigValue(description = "This is a boolean field")
    public boolean booleanField123;

    public double doubleField123;

    ConfigClass() {}
}