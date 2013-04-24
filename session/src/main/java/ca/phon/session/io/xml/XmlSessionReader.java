package ca.phon.session.io.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ca.phon.session.Session;
import ca.phon.session.io.SessionReader;
import ca.phon.xml.XMLObjectReader;
import ca.phon.xml.XMLObjectReaderFactory;

/**
 * 
 */
public class XmlSessionReader implements SessionReader {

	@Override
	public Session readSession(InputStream in) throws IOException {
		Session retVal = null;
		
		final XMLObjectReader<Session> reader = readerFromStream(in);
		final Document doc = documentFromStream(in);
		final Element ele = doc.getDocumentElement();
		if(reader != null) {
			retVal = reader.read(doc, ele);
		}
		
		return retVal;
	}

	@Override
	public boolean canRead(InputStream in) throws IOException {
		boolean retVal = false;
		
		final XMLObjectReader<Session> reader = readerFromStream(in);
		retVal = (reader != null);
		
		return retVal;
	}
	
	/**
	 * Get an dom version of the xml stream
	 * 
	 * @param in
	 * @return dom document
	 */
	public Document documentFromStream(InputStream in) 
		throws IOException {
		Document retVal = null; 
		
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			retVal = builder.parse(in);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		}
		
		return retVal;
	}
	
	/**
	 * Get an XMLObjectReader for the given input stream.
	 * 
	 * @param in
	 * @return reader or <code>null</code>
	 * 
	 * @throws IOException
	 */
	private XMLObjectReader<Session> readerFromStream(InputStream in)
		throws IOException {
		XMLObjectReader<Session> retVal = null;
		
		try {
			final XMLEventReader reader = createXmlReader(in);
			
			StartElement startEle = null;
			while(reader.hasNext()) {
				final XMLEvent currentEvt = reader.peek();
				if(currentEvt.isStartElement()) {
					startEle = currentEvt.asStartElement();
					break;
				} else {
					reader.next();
				}
			}
			if(startEle != null) {
				final QName qname = startEle.getName();
				retVal = findSessionReader(qname);
			}
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
		
		return retVal;
	}
	
	/**
	 * Convert the given input stream into an XMLEventReader
	 * 
	 * @param is
	 * 
	 * @return an xml input factory
	 * 
	 * @throws IOException
	 */
	private XMLEventReader createXmlReader(InputStream is) 
		throws IOException {
		final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
		XMLEventReader xmlEventReader;
		try {
			xmlEventReader = xmlInputFactory.createXMLEventReader(is, "UTF-8");
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
		return xmlEventReader;
	}

	/**
	 * Find an appropriate {@link XMLObjectReader} that can
	 * process the given root element.
	 * 
	 * @param qname
	 * 
	 * @return a session reader for the given {@link QName}
	 *  or <code>null</code> if not found
	 */
	private XMLObjectReader<Session> findSessionReader(QName qname) {
		final XMLObjectReaderFactory factory = new XMLObjectReaderFactory();
		return factory.createReader(qname, Session.class);
	}
}
