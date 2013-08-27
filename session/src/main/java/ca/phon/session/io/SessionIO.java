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
	 * Group name (e.g., 'ca.phon')
	 * @return
	 */
	public String group() default "";
	
	/**
	 * id
	 * @return 
	 */
	public String id();
	
	/**
	 * Version
	 * @return
	 */
	public String version();
	
	/**
	 * name
	 */
	public String name() default "";
	
	/**
	 * mimetype
	 * 
	 */
	public String mimetype();
	
	/**
	 * extension
	 */
	public String extension();
}
