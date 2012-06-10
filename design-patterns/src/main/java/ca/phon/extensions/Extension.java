package ca.phon.extensions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Extension annotation.  Used by {@link ExtensionSupport} objects
 * for automatic extension loading during object construction.
 * 
 * The class defined by {@link #value()} is guarnteed to be the
 * type passed into the {@link ExtensionProvider#installExtension()}
 * method.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Extension {
	
	public Class<? extends IExtendable> value();

}
