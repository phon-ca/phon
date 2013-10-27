package ca.phon.app.session.editor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation for editor event methods.  If this annotation
 * is found on the implementing method,  the method is called on a 
 * background thread.  If the newThread argument is <code>true</code>
 * a new background thread is created and destroyed for the method.
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RunInBackground {

	/**
	 * Should the method be executed in it's own
	 * background thread.  By default, a shared
	 * background thread will be used.
	 */
	public boolean newThread() default false;
	
}
