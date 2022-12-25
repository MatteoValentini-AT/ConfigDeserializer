package classes;

import at.matteovalentini.configdeserializer.ConfigValue;

public class InvalidConfigClass {

    @ConfigValue
    private String stringField;

    public InvalidConfigClass(String name) {
        this.stringField = name;
    }
}
