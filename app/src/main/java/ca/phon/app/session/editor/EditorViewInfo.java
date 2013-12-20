package ca.phon.app.session.editor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for editor view extensions.  This annotation
 * should exist on the plug-in extension point implementation.
 * 
 * 
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditorViewInfo {
	
	/**
	 * Name of the view
	 */
	public String name();

	/**
	 * View category
	 */
	public EditorViewCategory category() default EditorViewCategory.PLUGINS;
	
	/**
	 * View icon
	 */
	public String icon() default "blank";
	
}
