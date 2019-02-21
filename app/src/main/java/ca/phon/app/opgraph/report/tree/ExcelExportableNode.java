package ca.phon.app.opgraph.report.tree;

import ca.phon.app.log.ExcelExporter;

public class ExcelExportableNode extends TextNode {

	private ExcelExporter exporter;
	
	public ExcelExportableNode(String title, String text, ExcelExporter exporter) {
		super(title, text);
		
		this.exporter = exporter;
	}

	public ExcelExporter getExporter() {
		return this.exporter;
	}
	
}
