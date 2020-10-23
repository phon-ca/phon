package ca.phon.plugin;

import java.lang.annotation.*;

/**
 * Helps control load order of plugin extension points.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited()
public @interface Rank {
	
	public final static int DEFAULT_RANK = 10;

	public int value() default DEFAULT_RANK;
	
}
