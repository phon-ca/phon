package ca.phon.session.spi;

import ca.phon.session.CommentEnum;

public interface CommentSPI {
	
	/** 
	 * Get the comment string.
	 * @return String
	 */
	public String getValue();
	
	/**
	 * Set the comment string.
	 * @param comment
	 */
	public void setValue(String comment);
	
	/**
	 * Get the type.
	 * @return CommentEnum
	 */
	public CommentEnum getType();
	
	/**
	 * Set the type.
	 * @param type
	 */
	public void setType(CommentEnum type);

}
