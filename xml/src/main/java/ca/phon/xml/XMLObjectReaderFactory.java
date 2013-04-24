package ca.phon.xml;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.xml.namespace.QName;

import ca.phon.xml.annotation.XMLSerial;

/**
 * Creates {@link XMLObjectReader} for given {@link QName}
 * references.
 * 
 */
public class XMLObjectReaderFactory {
	
	/**
	 * Class loader to use for loading readers.
	 * 
	 * 
	 */
	private final ClassLoader cl;
	
	public XMLObjectReaderFactory() {
		this(null);
	}
	
	public XMLObjectReaderFactory(ClassLoader cl) {
		super();
		this.cl = cl;
	}

	/**
	 * Return the XMLObjectReader for the given namespace
	 * and element name.
	 * 
	 * @param namespace
	 * @param name
	 * 
	 * @return an {@link XMLObjectReader} or <code>null</code> if
	 *  an appropriate reader could not be found.
	 */
	public <T> XMLObjectReader<T> createReader(String namespace, String name, Class<T> type) {
		return createReader(new QName(namespace, name), type);
	}
	
	/**
	 * Return the XMLObjectReader for the given {@link QName}
	 * 
	 * @param qname
	 * 
	 * @return an {@link XMLObjectReader} or <code>null</code> if
	 *  an appropriate reader could not be found.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> XMLObjectReader<T> createReader(QName qname, Class<T> type) {
		final ServiceLoader<XMLObjectReader> loader = ServiceLoader.load(XMLObjectReader.class, this.cl);		
		final Iterator<XMLObjectReader> itr = loader.iterator();
		
		while(itr.hasNext()) {
			final XMLObjectReader<?> reader = itr.next();
			final XMLSerial annotation = reader.getClass().getAnnotation(XMLSerial.class);
			if(annotation != null) {
				final QName tName = new QName(annotation.namespace(), annotation.elementName());
				// TODO should really check the parameterized type here instead of
				// the annotation value
				if(tName.equals(qname) && type == annotation.bindType()) {
					return (XMLObjectReader<T>)reader;
				}
			}
		}
		
		return null;
	}
	
}
