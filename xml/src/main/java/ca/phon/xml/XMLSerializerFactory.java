package ca.phon.xml;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Obtain instances of {@link XMLSerializer} objects.
 * 
 */
public class XMLSerializerFactory {
	
	public XMLSerializerFactory() {
		
	}
	
	/**
	 * Obtain an XMLSerializer instance for the given class.
	 * 
	 * @param type
	 * @return serializer for the given type or <code>null</code> of
	 *  no serializer is found
	 */
	public XMLSerializer newSerializerForType(Class<?> type) {
		final ServiceLoader<XMLSerializer> loader = ServiceLoader.load(XMLSerializer.class);
		final Iterator<XMLSerializer> itr = loader.iterator();
		
		XMLSerializer retVal = null;
		while(itr.hasNext() && retVal == null) {
			final XMLSerializer currentSerializer = itr.next();
			if(currentSerializer.declaredType() == type) {
				retVal = currentSerializer;
			}
		}
		
		return retVal;
	}
	

}
