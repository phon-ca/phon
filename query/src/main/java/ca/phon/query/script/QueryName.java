package ca.phon.query.script;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;

import ca.phon.extensions.Extension;

/**
 * Extension for {@link QueryScript} objects which provides
 * name information for the script object.
 */
@Extension(QueryScript.class)
public class QueryName {
	
	private static final Logger LOGGER = Logger.getLogger(QueryName.class
			.getName());

	private String name;
	
	private URL location;
	
	public QueryName(URL url) {
		this.location = url;
		String path;
		try {
			path = url.toURI().getPath();
			final int lastSlash = path.lastIndexOf(File.separatorChar);
			this.name = (lastSlash > 0 ? path.substring(lastSlash + 1) : path);
			this.name = StringEscapeUtils.unescapeXml(name);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
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
