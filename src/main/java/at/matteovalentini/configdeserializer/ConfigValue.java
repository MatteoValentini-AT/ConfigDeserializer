package at.matteovalentini.configdeserializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a field as a config value. All properties are optional.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {
    /**
     * Name of the config value. If not specified, the name of the field will be used
     */
    String name() default "";

    /**
     * Whether the config value is required. If true, the config file must contain a value for this field
     */
    boolean required() default false;

    /**
     * Default value of the config value. If the config file doesn't contain a value for this field, this value will be used
     */
    String defaultValue() default "";

    /**
     * Description of the config value. This will be used to generate the config file
     */
    String description() default "";

    /**
     * Minimum value of the config value. If the config file contains a value that is lower than this, an exception will be thrown. For strings, this is the minimum length.
     * Only check if limited is true
     */
    double min() default 0;

    /**
     * Maximum value of the config value. If the config file contains a value that is higher than this, an exception will be thrown. For strings, this is the maximum length.
     * Only check if limited is true
     */
    double max() default 0;

    /**
     * Whether to check the min and max values. If false, the min and max values will be ignored
     */
    boolean limited() default false;
}
