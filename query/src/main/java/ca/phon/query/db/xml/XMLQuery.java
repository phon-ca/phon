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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import ca.phon.query.db.Query;
import ca.phon.query.db.Script;
import ca.phon.query.db.xml.io.query.QueryType;
import ca.phon.query.db.xml.io.query.ScriptType;


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
