package ca.phon.app.session.editor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for editor event handlers.  If the specified event method
 * has this annotation it will be called on the AWT event dispatch
 * thread.  This is the default behaviors, this annotation is provided
 * to be explicit.
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RunOnEDT {

}
