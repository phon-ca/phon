/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.phon.query.db.xml;

import ca.phon.query.db.*;
import ca.phon.query.db.xml.io.query.*;

import javax.xml.datatype.*;
import java.time.*;
import java.util.*;


/**
 * XML-based implementation of {@link Query}.
 */
public class XMLQuery implements Query, JAXBWrapper<QueryType> {
	/** JAXB object */
	protected QueryType query;
	
	/**
	 * Default constructor.
	 */
	XMLQuery() {
		this(new QueryType());
	}
	
	/**
	 * Constructs query from a JAXB query object.
	 * @param query
	 */
	XMLQuery(QueryType query) {
		this(query, "unnamed_query"); 
		
		// So that marshalled files have these required elements
		if(query.getScript() == null) {
			query.setScript(new ScriptType());
			query.getScript().setSource("");
		}
	}
	
	/**
	 * Construct an XMLQuery from a JAXB query object.
	 * @param query
	 */
	public XMLQuery(QueryType query, String name) {
		this.query = query;
		query.setName(name);
	}
	
	/**
	 * Obtain the JAXB query object associated with this query.
	 * @return
	 */
	@Override
	public QueryType getXMLObject() {
		return query;
	}

	@Override
	public String getName() {
		return query.getName();
	}

	@Override
	public void setName(String name) {
		if(name != null && name.length() > 0)
			query.setName(name);
	}
	
	@Override
	public UUID getUUID() {
		return UUID.fromString(query.getUuid());
	}

	@Override
	public void setUUID(UUID uuid) {
		query.setUuid(uuid.toString());
	}

	@Override
	public LocalDateTime getDate() {
		final XMLGregorianCalendar xmlDate = query.getDate();
		// ensure timezone neutral
		xmlDate.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
		
		return LocalDateTime.ofInstant(xmlDate.toGregorianCalendar().toInstant(), ZoneId.systemDefault());
	}

	@Override
	public void setDate(LocalDateTime date) {
		try {
			query.setDate(
				DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(GregorianCalendar.from(date.atZone(ZoneId.systemDefault())))
					);
		} catch(DatatypeConfigurationException e) {
		}
	}

	@Override
	public boolean isStarred() {
		return query.isStarred();
	}

	@Override
	public void setStarred(boolean starred) {
		query.setStarred(starred);
	}
	
	@Override
	public Script getScript() {
		return new XMLScript(query.getScript());
	}
	
	@Override
	public void setScript(Script script) {
		if(script instanceof XMLScript)
			query.setScript( ((XMLScript)script).getXMLObject() );
	}
	
	@Override
	public List<String> getTags() {
		return query.getTag();
	}

	@Override
	public String getComments() {
		return query.getComments();
	}

	@Override
	public void setComments(String comments) {
		query.setComments(comments);
	}
	
	
}
