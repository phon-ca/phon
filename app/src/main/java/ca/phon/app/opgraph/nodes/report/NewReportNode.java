package ca.phon.app.opgraph.nodes.report;

import java.awt.*;
import java.util.Properties;

import javax.swing.JPanel;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.report.tree.*;

@OpNodeInfo(name="New Report", description="New report tree", category="Report", showInLibrary=true)
public class NewReportNode extends OpNode implements NodeSettings {

	public final static String REPORT_TREE_KEY = "__reportTree";

	private final InputField reportNameInput =
			new InputField("root", "Report name", true, true, String.class);

	private final OutputField reportOutput =
			new OutputField("reportTree", "Report tree", true, ReportTree.class);

	private final OutputField reportRootOutput =
			new OutputField("root", "Root node of report tree", true, ReportTreeNode.class);

	private JPanel settingsPanel;

	public NewReportNode() {
		super();

		putField(reportNameInput);
		putField(reportOutput);
		putField(reportRootOutput);

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final ReportTreeNode root =
				(context.get(reportNameInput) != null ? new SectionHeaderNode(context.get(reportNameInput).toString()) : new SectionHeaderNode("root"));

		final ReportTree reportTree = new ReportTree(root);
		context.put(reportOutput, reportTree);
		context.put(reportRootOutput, root);
		
		if(context.containsKey(REPORT_TREE_KEY)) {
			final ReportTree masterReport = (ReportTree)context.get(REPORT_TREE_KEY);
			masterReport.getRoot().add(root);
		}
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new BorderLayout());
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
	}

}
