package ca.phon.alignedTypesDatabase;

import java.util.Iterator;

public interface TypeIterator extends Iterator<String> {

	public String getTypePrefix();

	public void setTypePrefix(String type);

	public boolean isPrefixSearch();

	public void setPrefixSearch(boolean prefixSearch);

	/**
	 * @param type
	 */
	public void continueFrom(String type);

	public void reset();

}
