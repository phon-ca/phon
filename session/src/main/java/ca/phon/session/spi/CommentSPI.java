package ca.phon.session.spi;

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
	 * Get the tag.
	 * @return tag for the comment
	 */
	public String getTag();
	
	/**
	 * Set the tag (cannot be <code>null</code>).
	 * @param type
	 */
	public void setTag(String tag);

}
