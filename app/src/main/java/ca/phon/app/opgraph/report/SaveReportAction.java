package ca.phon.app.opgraph.report;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.TreePath;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.analysis.SaveAnalysisAction;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.PrefHelper;

public class SaveReportAction extends HookableAction {

	private static final long serialVersionUID = 7018411088372966429L;

	private final static Logger LOGGER = Logger.getLogger(SaveAnalysisAction.class.getName());
	
	private final static String DEFAULT_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "reports";
	
	public final static String TXT = "Save report...";
	
	public final static String DESC = "Save report with current setup to file.";
	
	private final ReportWizard wizard;
	
	public SaveReportAction(ReportWizard wizard) {
		super();
		
		this.wizard = wizard;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	public ReportWizard getWizard() {
		return this.wizard;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		File defaultFolder = new File(DEFAULT_FOLDER);
		if(!defaultFolder.exists()) {
			defaultFolder.mkdirs();
		}
		
		final OpGraph graph = getWizard().getGraph();
		graph.setId("root");
		final WizardExtension ext = getWizard().getWizardExtension();
		
		for(OpNode optionalNode:ext.getOptionalNodes()) {
			TreePath tp = getWizard().getOptionalsTree().getNodePath(optionalNode);
			boolean enabled = getWizard().getOptionalsTree().isPathChecked(tp);
			
			ext.setOptionalNodeDefault(optionalNode, enabled);
		}
		
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setInitialFolder(DEFAULT_FOLDER);
		props.setInitialFile(getWizard().getWizardExtension().getWizardTitle() + ".xml");
		props.setParentWindow(getWizard());
		props.setTitle("Save report to file");
		props.setPrompt("Save report");
		props.setListener( (e) -> {
			if(e.getDialogData() != null) {
				String saveAs = (String)e.getDialogData();
				try {
					OpgraphIO.write(graph, new File(saveAs));
				} catch (Exception e1) {
					e1.printStackTrace();
					LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
					final MessageDialogProperties msgProps = new MessageDialogProperties();
					msgProps.setOptions(MessageDialogProperties.okOptions);
					msgProps.setTitle("Unable to save analysis");
					msgProps.setMessage(e1.getLocalizedMessage());
					msgProps.setParentWindow(getWizard());
					NativeDialogs.showMessageDialog(msgProps);
				}
			}
		});
		NativeDialogs.showSaveDialog(props);
	}
	
}
