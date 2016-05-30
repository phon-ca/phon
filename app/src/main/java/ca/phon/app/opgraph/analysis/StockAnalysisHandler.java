package ca.phon.app.opgraph.analysis;

import java.io.IOException;
import java.net.URL;

import ca.phon.util.resources.ClassLoaderHandler;

public class StockAnalysisHandler extends ClassLoaderHandler<URL> {
	
	private final static String STOCK_ANALYSIS_LIST = "analysis/analysis.list";
	
	public StockAnalysisHandler() {
		super();
		loadResourceFile(STOCK_ANALYSIS_LIST);
	}

	@Override
	public URL loadFromURL(URL url) throws IOException {
		return url;
	}
	
}
