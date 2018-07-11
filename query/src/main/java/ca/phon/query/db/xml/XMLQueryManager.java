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

package ca.phon.query.db.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import ca.phon.query.db.Query;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSetManager;
import ca.phon.query.db.xml.io.query.ObjectFactory;
import ca.phon.query.db.xml.io.query.QueryType;

/**
 * XML-based implementation of {@link QueryManager}.
 */
public class XMLQueryManager extends QueryManager {
	
	@Override
	public QueryFactory createQueryFactory() {
		return new XMLQueryFactory();
	}

	@Override
	public ResultSetManager createResultSetManager() {
		return new XMLResultSetManager();
	}

	@Override
	public void saveQuery(Query query, String path) throws IOException {
		final File queryFile = new File(path);
		final File queryPath = queryFile.getParentFile();

		// Create the directory, if necessary
		if(!queryPath.exists() && !queryPath.mkdirs())
			return;

		try {
			final ObjectFactory factory = new ObjectFactory();
			// Use JAXBElement wrapper around object because they do not have
			// the XMLRootElement annotation
			final QueryType qt = ((XMLQuery)query).getXMLObject();
			final JAXBElement<QueryType> jaxbElem = factory.createQuery(qt);
						
			// Initialize marshaller and write to disk
			final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(jaxbElem, queryFile);
		} catch (JAXBException exc) {
			//PhonLogger.severe(XMLResultSetManager.class, "Could not save query to disk.");
			//PhonLogger.severe(XMLResultSetManager.class, "JAXBException: " + exc.getLocalizedMessage());
			throw new IOException("Could not save query to disk", exc);
		}
	}
	
	@Override
	public Query loadQuery(InputStream stream) throws IOException {
		try {
			final XMLInputFactory factory = XMLInputFactory.newFactory();
			final XMLEventReader eventReader = factory.createXMLEventReader(stream);
			final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			final JAXBElement<QueryType> queryTypeEle = 
				unmarshaller.unmarshal(eventReader, QueryType.class);
			
			return new XMLQuery(queryTypeEle.getValue());
		} catch (JAXBException  e) {
			throw new IOException(e);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Query loadQuery(String path) throws IOException {
		final File queryFile = new File(path);
		return new XMLLazyQuery((XMLResultSetManager)createResultSetManager(), queryFile);
	}
}
