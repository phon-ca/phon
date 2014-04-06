package ca.phon.query.script;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
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
		String path = url.getPath();
		final int lastSlash = path.lastIndexOf("/");

		this.name = (lastSlash > 0 ? path.substring(lastSlash + 1) : path);
		
		final URLDecoder urlDecoder = new URLDecoder();
		try {
			this.name = urlDecoder.decode(this.name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		final int lastDot = this.name.lastIndexOf('.');
		if(lastDot > 0) {
			this.name = this.name.substring(0, lastDot);
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
