package ca.phon.app.opgraph.nodes.report;

import ca.phon.app.log.ExcelExporter;
import ca.phon.app.opgraph.report.tree.ExcelExportableNode;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;

@OpNodeInfo(name="Excel Exportable Text Section", description="Text Section with Excel export option", category="Report", showInLibrary=true)
public class ExcelExportableSectionNode extends TextSectionNode {
	
	private InputField exporterInputField = 
			new InputField("exporter", "Excel exporter instance", false, true, ExcelExporter.class);

	public ExcelExportableSectionNode() {
		super();
		
		putField(exporterInputField);
	}

	@Override
	protected ReportTreeNode createReportSectionNode(OpContext context) {
		final String title = (context.get(sectionNameInput) != null ? context.get(sectionNameInput).toString() : "");
		final String inputText = (context.get(textInput) != null ? context.get(textInput).toString() : null);
		final String sectionText = (inputText != null ? inputText : getText() );
		
		return new ExcelExportableNode(title, sectionText, (ExcelExporter)context.get(exporterInputField));
	}
	
}
