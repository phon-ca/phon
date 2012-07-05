package ca.phon.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;

/**
 * 
 */
public class XMLUtil {
	
	/**
	 * Provide a catalog resolver for all schemas
	 * identified in the $classloader::xml/catalog.cat
	 * resource.
	 * 
	 * @return the catalog resolver
	 * 
	 */
	public static XMLResolver getXMLResolver() {
		return new ClassloaderXMLResolver();
	}
	
	public static XMLResolver getXMLResolver(ClassLoader classLoader) {
		return new ClassloaderXMLResolver(classLoader);
	}
	
	/**
	 * Create a new xml input factory.
	 */
	public static XMLInputFactory newInputFactory() {
		final XMLInputFactory retVal = XMLInputFactory.newFactory();
		retVal.setXMLResolver(getXMLResolver());
		return retVal;
	}
	
	/**
	 * Create a new xml input factory
	 * 
	 * @param blah
	 * @param blah2
	 * 
	 */
	public static XMLInputFactory newInputFactory(String factoryID, ClassLoader classLoader) {
		final XMLInputFactory retVal = XMLInputFactory.newFactory(factoryID, classLoader);
		retVal.setXMLResolver(getXMLResolver(classLoader));
		return retVal;
	}
}
