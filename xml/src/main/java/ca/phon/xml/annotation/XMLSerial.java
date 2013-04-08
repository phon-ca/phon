package ca.phon.xml.annotation;

/**
 * Annotation that declares the elements that can
 * be procesed using the associated xml reader/writer.
 *
 */
public @interface XMLSerial {
	
	public String namespace = "";
	
	public String elementName = "";
	
	public Class<?> bindType = Object.class;

}
