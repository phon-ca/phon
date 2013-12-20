package ca.phon.query.script;

import java.io.IOException;
import java.net.URL;

import ca.phon.util.resources.ClassLoaderHandler;

public class SystemQueryScriptHandler extends ClassLoaderHandler<QueryScript>{
	
	private final static String LIST = "ca/phon/query/script/query.list";
	
	public SystemQueryScriptHandler() {
		super();
		super.loadResourceFile(LIST);
	}
	
	@Override
	public QueryScript loadFromURL(URL url) throws IOException {
		return new QueryScript(url);
	}

}
