package ca.phon.session.io;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation providing runtime support of Session IO
 * version information.
 *
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionIO {
	
	/**
	 * Human-readable name 
	 * @return
	 */
	public String name() default "";
	
	/**
	 * Version
	 * @return
	 */
	public String version();
	
}
