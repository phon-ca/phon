/*
 * 
 */
package ca.phon.properties;

import java.lang.annotation.*;

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
