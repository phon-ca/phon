package ca.phon.app.opgraph.report;

import java.io.IOException;
import java.net.URL;

import ca.phon.util.resources.ClassLoaderHandler;

public class StockReportHandler extends ClassLoaderHandler<URL> {

	private final static String STOCK_REPORT_LIST = "reports/reports.list";
	
	public StockReportHandler() {
		super();
		loadResourceFile(STOCK_REPORT_LIST);
	}

	@Override
	public URL loadFromURL(URL url) throws IOException {
		return url;
	}

}
