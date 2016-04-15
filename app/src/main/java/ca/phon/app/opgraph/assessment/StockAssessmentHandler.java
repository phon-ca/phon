package ca.phon.app.opgraph.assessment;

import java.io.IOException;
import java.net.URL;

import ca.phon.util.resources.ClassLoaderHandler;

public class StockAssessmentHandler extends ClassLoaderHandler<URL> {
	
	private final static String STOCK_ASSESSMENT_LIST = "assessments/assessments.list";
	
	public StockAssessmentHandler() {
		super();
		loadResourceFile(STOCK_ASSESSMENT_LIST);
	}

	@Override
	public URL loadFromURL(URL url) throws IOException {
		return url;
	}
	
}
