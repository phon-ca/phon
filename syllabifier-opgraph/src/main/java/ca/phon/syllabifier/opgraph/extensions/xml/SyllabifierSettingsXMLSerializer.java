/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.syllabifier.opgraph.extensions.xml;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.io.xml.XMLSerializer;
import ca.gedge.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.util.Language;

public class SyllabifierSettingsXMLSerializer implements XMLSerializer {
	
	static final String NAMESPACE = "https://www.phon.ca/ns/syllabifier";
	static final String PREFIX = "ops";
	
	static final QName SETTINGS_QNAME = new QName(NAMESPACE, "settings", PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc,
			Element parentElem, Object obj) throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");
		
		if(!(obj instanceof SyllabifierSettings))
			throw new IOException(SyllabifierSettingsXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());
		
		// setup namespace for document
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);
		
		// Create metadata element
		final SyllabifierSettings meta = (SyllabifierSettings)obj;
		final Element metaElem = doc.createElementNS(NAMESPACE, PREFIX + ":settings");
		metaElem.setAttribute("name", meta.getName());
		metaElem.setAttribute("lang", meta.getLanguage().toString());
		
		parentElem.appendChild(metaElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory,
			OpGraph graph, Object parent, Document doc, Element elem)
			throws IOException {
		if(graph != parent) {
			throw new IOException("SyllabifierSettings can only exist once per graph.");
		}
		if(SETTINGS_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			final SyllabifierSettings settings = new SyllabifierSettings();

			final String name = elem.getAttribute("name");
			settings.setName(name);
			
			final String lang = elem.getAttribute("lang");
			settings.setLanguage(Language.parseLanguage(lang));
			
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
