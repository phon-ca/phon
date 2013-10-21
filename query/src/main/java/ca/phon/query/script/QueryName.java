package ca.phon.query.script;

import ca.phon.extensions.Extension;

/**
 * Extension for {@link QueryScript} objects which provides
 * name information for the script object.
 */
@Extension(QueryScript.class)
public class QueryName {

	private String name;
	
	public QueryName(String name) {
		this.name = name;
	}
	
	public String getName() {
		 return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
