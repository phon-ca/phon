package ca.phon.session.spi;

import ca.phon.session.Comment;

public interface SessionMetadataSPI {
	
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
	
	/**
	 * number of comments
	 * @return int
	 */
	public int getNumberOfComments();
	
	/**
	 * get comment at given index
	 * 
	 * @param idx
	 * @return comment
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public Comment getComment(int idx);
	
	/**
	 * Add comment
	 * 
	 * @param comment
	 */
	public void addComment(Comment comment);
	
	/**
	 * Remove comment
	 * 
	 * @param comment
	 */
	public void removeComment(Comment comment);
	public void removeComment(int idx);
	
}
