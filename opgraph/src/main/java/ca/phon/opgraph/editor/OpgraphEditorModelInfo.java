package ca.phon.opgraph.editor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides class metadata to editor models.  This information 
 * is used in menus and other UI elements.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpgraphEditorModelInfo {

	public String name();
	
	public String description();
	
}
