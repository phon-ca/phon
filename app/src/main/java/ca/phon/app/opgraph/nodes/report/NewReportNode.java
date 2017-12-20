package ca.phon.app.opgraph.nodes.report;

import java.awt.*;
import java.util.Properties;

import javax.swing.*;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.report.tree.*;

@OpNodeInfo(name="New Report", description="New report tree", category="Report Tree", showInLibrary=true)
public class NewReportNode extends OpNode implements NodeSettings {
	
	public final static String REPORT_TREE_KEY = "__reportTree";
	
	private final InputField reportRootInput =
			new InputField("root", "Report root node", true, true, ReportTreeNode.class);
	
	private final OutputField reportOutput = 
			new OutputField("reportTree", "Report tree", true, ReportTree.class);
	
	private final OutputField reportRootOutput =
			new OutputField("root", "Root node of report tree", true, ReportTreeNode.class);
	
	private final static String GLOBAL_REPORT_PROP = NewReportNode.class.getName() + ".setGlobalReport";
	private boolean isSetGlobalReport = true;
	
	private JPanel settingsPanel;
	private JCheckBox setGlobalReportBox;

	public NewReportNode() {
		super();
		
		putField(reportRootInput);
		putField(reportOutput);
		putField(reportRootOutput);
		
		putExtension(NodeSettings.class, this);
	}
	
	public boolean isSetGlobalReport() {
		return (this.setGlobalReportBox != null ? this.setGlobalReportBox.isSelected() : this.isSetGlobalReport);
	}
	
	public void setSetGlobalReport(boolean setGlobalReport) {
		this.isSetGlobalReport = setGlobalReport;
		if(this.setGlobalReportBox != null)
			this.setGlobalReportBox.setSelected(setGlobalReport);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final ReportTreeNode root = 
				(context.get(reportRootInput) != null ? (ReportTreeNode)context.get(reportRootInput) : new SectionHeaderNode("root"));
		
		final ReportTree reportTree = new ReportTree(root);
		context.put(reportOutput, reportTree);
		context.put(reportRootOutput, root);
		
		if(isSetGlobalReport())
			context.getParent().put(REPORT_TREE_KEY, reportTree);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new BorderLayout());
			
			setGlobalReportBox = new JCheckBox("Set as global report");
			setGlobalReportBox.setSelected(isSetGlobalReport);
			settingsPanel.add(setGlobalReportBox, BorderLayout.NORTH);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		retVal.setProperty(GLOBAL_REPORT_PROP, Boolean.toString(isSetGlobalReport()));
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setSetGlobalReport(Boolean.parseBoolean(properties.getProperty(GLOBAL_REPORT_PROP, "true")));
	}

}
