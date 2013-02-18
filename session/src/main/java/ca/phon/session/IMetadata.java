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

package ca.phon.session;

/**
 * Transcript Metadata
 * 
 *
 */
public interface IMetadata {
	/**
	 * Title
	 * @return String
	 */
	public String getTitle();
	
	public void setTitle(String title);
	
	/**
	 * Creator
	 * @return String
	 */
	public String getCreator();
	
	public void setCreator(String creator);
	
	/**
	 * App ID
	 * @return String
	 */
	public String getAppID();
	
	public void setAppID(String appID);
	
	/**
	 * Subject
	 * @return String
	 */
	public String getSubject();
	
	public void setSubject(String subject);
	
	/**
	 * Description
	 * @return String
	 */
	public String getDescription();
	
	public void setDescription(String description);
	
	/**
	 * Publisher
	 * @return String
	 */
	public String getPublisher();
	
	public void setPublisher(String publisher);
	
	/**
	 * Contributer
	 * @return String
	 */
	public String getContributor();
	
	public void setContributor(String contributer);
	
	/**
	 * Date
	 * @return String
	 */
	public String getDate();
	
	public void setDate(String date);
	
	/**
	 * Type
	 * @return String
	 */
	public String getType();
	
	public void setType(String type);
	
	/**
	 * Format
	 * @return String
	 */
	public String getFormat();
	
	public void setFormat(String format);
	
	/**
	 * Identifier
	 * @return String
	 */
	public String getIdentifier();
	
	public void setIdentifier(String identifier);
	
	/**
	 * Source
	 * @return String
	 */
	public String getSource();
	
	public void setSource(String source);
	
	/**
	 * Language
	 * @return String
	 */
	public String getLanguage();
	
	public void setLanguage(String language);
	
	/**
	 * Relation
	 * @return String
	 */
	public String getRelation();
	
	public void setRelation(String relation);
	
	/**
	 * Coverage
	 * @return String
	 */
	public String getCoverage();
	
	public void setCoverage(String coverage);
	
	/**
	 * Rights
	 * @return String
	 */
	public String getRights();
	
	public void setRights(String rights);
	
}
