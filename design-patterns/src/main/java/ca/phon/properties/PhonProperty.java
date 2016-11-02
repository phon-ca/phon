package ca.phon.properties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for phon properties.
 * This annotation is used to compile a list of available properties
 * at runtime.
 *
 *
 *
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface PhonProperty {

	public String name();
	
	public String description();
	
	public String defaultValue();
	
}
