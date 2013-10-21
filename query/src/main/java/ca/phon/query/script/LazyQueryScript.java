package ca.phon.query.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.script.BasicScript;

/**
 * Defers loadsing of the query script until data is needed.
 * 
 */
public class LazyQueryScript extends BasicScript {

	private final static Logger LOGGER = Logger.getLogger(LazyQueryScript.class.getName());
	
	private boolean loaded = false;
	
	private final URL scriptURL;
	
	public LazyQueryScript(String script) {
		super(script);
		scriptURL = null;
	}
	
	public LazyQueryScript(URL url) {
		super("");
		this.scriptURL = url;
		
		// setup QueryName extension
		try {
			final Path path = Paths.get(url.toURI());
			final Path fileName = path.getFileName();
			putExtension(QueryName.class, new QueryName(fileName.toString()));
		} catch (URISyntaxException e) {
		} finally {}
	}
	
	@Override
	public String getScript() {
		if(!loaded) {
			// load script
			readScript();
			loaded = true;
		}
		return super.getScript();
	}
	
	private void readScript() {
		if(scriptURL == null) return;
		
		try(InputStream in = getScriptURL().openStream()) {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			final StringBuffer buffer = getBuffer();
			String line = null;
			while((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append("\n");
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	public URL getScriptURL() {
		return scriptURL;
	}
	
}
