package ca.phon.util;

import java.lang.annotation.*;

/**
 * Annotation used to provide links to online documentation for types and fields
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Documentation {
    String[] value();
}
