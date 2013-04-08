package ca.phon.xml;

import javax.xml.namespace.QName;

/**
 * Creates {@link XMLObjectReader} for given {@link QName}
 * references.
 * 
 */
public class XMLObjectReaderFactory {

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
	public XMLObjectReader createReader(String namespace, String name) {
		return createReader(new QName(namespace, name));
	}
	
	/**
	 * Return the XMLObjectReader for the given {@link QName}
	 * 
	 * @param qname
	 * 
	 * @return an {@link XMLObjectReader} or <code>null</code> if
	 *  an appropriate reader could not be found.
	 */
	public XMLObjectReader createReader(QName qname) {
		return null;
	}
	
}
