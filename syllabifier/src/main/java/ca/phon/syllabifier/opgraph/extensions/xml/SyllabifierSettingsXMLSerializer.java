package ca.phon.syllabifier.opgraph.extensions.xml;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.gedge.opgraph.OperableGraph;
import ca.gedge.opgraph.io.xml.XMLSerializer;
import ca.gedge.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;

public class SyllabifierSettingsXMLSerializer implements XMLSerializer {
	
	static final String NAMESPACE = "http://phon.ling.mun.ca/ns/syllabifier";
	static final String PREFIX = "ops";
	
	static final QName SETTINGS_QNAME = new QName(NAMESPACE, "settings", PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc,
			Element parentElem, Object obj) throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");
		
		if(!(obj instanceof SyllabifierSettings))
			throw new IOException(SyllabifierSettingsXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());
		
		// Create metadata element
		final SyllabifierSettings meta = (SyllabifierSettings)obj;
		final Element metaElem = doc.createElementNS(NAMESPACE, PREFIX + ":settings");
		metaElem.setAttributeNS(NAMESPACE, PREFIX + ":name", meta.getName());
//		final Element nameElem = doc.createElementNS(NAMESPACE, PREFIX + ":name");
//		nameElem.setTextContent(meta.getName());
//		metaElem.appendChild(nameElem);
		
//		final Element langElem = doc.createElementNS(NAMESPACE, PREFIX + ":lang");
//		langElem.setTextContent(meta.getLanguage());
//		metaElem.appendChild(langElem);
		metaElem.setAttributeNS(NAMESPACE, PREFIX + ":lang", meta.getLanguage());
		
		parentElem.appendChild(metaElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory,
			OperableGraph graph, Object parent, Document doc, Element elem)
			throws IOException {
		if(graph != parent) {
			throw new IOException("SyllabifierSettings can only exist once per graph.");
		}
		if(SETTINGS_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			final SyllabifierSettings settings = new SyllabifierSettings();

			final String name = elem.getAttributeNS(NAMESPACE, "name");
			settings.setName(name);
			
			final String lang = elem.getAttributeNS(NAMESPACE, "lang");
			settings.setLanguage(lang);
			
			graph.putExtension(SyllabifierSettings.class, settings);
		}
		
		return null;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == SyllabifierSettings.class);
	}

	@Override
	public boolean handles(QName name) {
		return SETTINGS_QNAME.equals(name);
	}

}
