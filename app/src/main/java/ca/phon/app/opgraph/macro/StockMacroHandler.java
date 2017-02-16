package ca.phon.app.opgraph.macro;

import java.io.IOException;
import java.net.URL;

import ca.phon.util.resources.ClassLoaderHandler;

public class StockMacroHandler extends ClassLoaderHandler<URL> {

	private final static String STOCK_MACRO_LIST = "macro/macro.list";
	
	public StockMacroHandler() {
		super();
		loadResourceFile(STOCK_MACRO_LIST);
	}

	@Override
	public URL loadFromURL(URL url) throws IOException {
		return url;
	}
	
}
