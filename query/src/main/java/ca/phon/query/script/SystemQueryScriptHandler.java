package ca.phon.query.script;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import ca.phon.util.resources.ClassLoaderHandler;

public class SystemQueryScriptHandler extends ClassLoaderHandler<QueryScript>{

	private final static Logger LOGGER = Logger.getLogger(SystemQueryScriptHandler.class.getName());
	
	private final static String LIST = "script/query.list";
	
	public SystemQueryScriptHandler() {
		super();
		super.loadResourceFile(LIST);
	}
	
	@Override
	public QueryScript loadFromURL(URL url) throws IOException {
		return new QueryScript(url);
	}

}
