package ca.phon.session;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for session version information.
 * Used to help determine what versions are
 * supported by a set of implementing objects.
 * 
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionVersionInfo {

	/**
	 * Min version supported
	 */
	public String min();
	
	/**
	 * Max version supported
	 */
	public String max();
	
	/**
	 * Target version (same as max if unspecified)
	 */
	public String target() default "";
	
}
