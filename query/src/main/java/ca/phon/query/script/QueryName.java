package ca.phon.query.script;

import java.io.File;
import java.net.URL;

import ca.phon.extensions.Extension;

/**
 * Extension for {@link QueryScript} objects which provides
 * name information for the script object.
 */
@Extension(QueryScript.class)
public class QueryName {

	private String name;
	
	private URL location;
	
	public QueryName(URL url) {
		this.location = url;
		final String path = url.getPath();
		final int lastSlash = path.lastIndexOf(File.pathSeparatorChar);
		this.name = (lastSlash > 0 ? path.substring(lastSlash) : path);
	}
	
	public QueryName(String name) {
		this.name = name;
		this.location = null;
	}
	
	public String getName() {
		 return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public URL getLocation() {
		return location;
	}

	public void setLocation(URL location) {
		this.location = location;
	}
	
}
