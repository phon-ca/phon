package ca.phon.xml.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that declares the elements that can
 * be procesed using the associated xml reader/writer.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME) 
@Inherited
@Documented
public @interface XMLSerial {
	
	public String namespace() default "";
	
	public String elementName();
	
	public Class<?> bindType() default Object.class;

}
