package classes;

import at.matteovalentini.configdeserializer.ConfigValue;

public class ParseConfigClass {

    @ConfigValue(defaultValue = "Hello World")
    public String stringField;
}
