package ca.phon.app.session.editor.tier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ca.phon.plugin.IPluginExtensionPoint;

/**
 * Annotation requried for {@link TierEditor} {@link IPluginExtensionPoint}s.
 * 
 * This annotation should exist on the extension point implementation.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TierEditorInfo {

	public Class<?> type() default String.class;
	
}
