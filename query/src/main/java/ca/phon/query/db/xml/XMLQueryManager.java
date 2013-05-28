/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ca.phon.engines.search.db.Query;
import ca.phon.engines.search.db.QueryFactory;
import ca.phon.engines.search.db.QueryManager;
import ca.phon.engines.search.db.ResultSetManager;
import ca.phon.engines.search.db.xml.io.query.QueryType;

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
			// Use JAXBElement wrapper around object because they do not have
			// the XMLRootElement annotation
			final QueryType qt = ((XMLQuery)query).getXMLObject();
			final JAXBElement<QueryType> jaxbElem = (new ca.phon.engines.search.db.xml.io.query.ObjectFactory()).createQuery(qt);
						
			// Initialize marshaller and write to disk
			final JAXBContext context = JAXBContext.newInstance("ca.phon.engines.search.db.xml.io.query");
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
	public Query loadQuery(String path) throws IOException {
		final File queryFile = new File(path);
		return new XMLLazyQuery((XMLResultSetManager)createResultSetManager(), queryFile);
	}
}
