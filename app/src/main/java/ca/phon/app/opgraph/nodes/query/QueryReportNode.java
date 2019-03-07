package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.nodes.RecordContainer;
import ca.phon.app.opgraph.nodes.RecordContainerTypeValidator;
import ca.phon.app.opgraph.report.ReportWizardExtension;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.OpgraphIO;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Select query report from file or created using the Report Composer.
 *  
 */
@OpNodeInfo(name="Query Report", category="Query", description="Execute query report", showInLibrary=true)
public class QueryReportNode extends OpNode implements NodeSettings {
	
	private final static String DEFAULT_REPORT_LOCATION = "ca/phon/app/query/default_report.xml";
	
	private InputField projectInput = new InputField("project", "temporary project input", false, true, Project.class);
	
	private InputField queryInput = new InputField("query", "query id", false, true, String.class);
	
	private InputField sessionsInput = new InputField("sessions", "List of sessions or query results", false,
			true, new RecordContainerTypeValidator());
	
	private OutputField reportOutput = new OutputField("report", "report tree", true, ReportTree.class);
	
	private JPanel settingsPanel;
	private DropDownButton selectReportButton;
	private JLabel reportNameLabel;
		
	private URL reportURL;
	private OpGraph reportGraph;
	
	public static URL getDefaultReportURL() {
		return ClassLoader.getSystemResource(DEFAULT_REPORT_LOCATION);
	}
	
	public QueryReportNode() {
		this(getDefaultReportURL());
	}
	
	public QueryReportNode(URL reportGraphURL) {
		super();
		
		this.reportURL = reportGraphURL;
		
		setupFields();
		putExtension(NodeSettings.class, this);
	}
	
	public QueryReportNode(OpGraph reportGraph) {
		super();
		
		this.reportGraph = reportGraph;
		
		setupFields();
		putExtension(NodeSettings.class, this);
	}
	
	private void setupFields() {
		putField(projectInput);
		putField(queryInput);
		putField(sessionsInput);
		
		putField(reportOutput);
	}
	
	public OpGraph getReportGraph() {
		if(this.reportGraph == null) {
			try {
				this.reportGraph = loadReportGraph();
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
		return this.reportGraph;
	}
	
	public void setReportGraph(OpGraph reportGraph) {
		this.reportGraph = reportGraph;
	}

	public URL getReportGraphURL() {
		return this.reportURL;
	}
	
	public void setReportGraphURL(URL reportURL) {
		if(reportURL == null)
			reportURL = ClassLoader.getSystemResource(DEFAULT_REPORT_LOCATION);
		this.reportURL = reportURL;
		this.reportGraph = null;
		if(this.reportNameLabel != null) {
			updateLabel();
		}
	}
	
	private void updateLabel() {
		 final URL url = getReportGraphURL();
		 if(url != null) {
			 if(url.equals(getDefaultReportURL())) {
				 reportNameLabel.setText("Default report");
				 reportNameLabel.setToolTipText("Default report (query information, aggregate, tables by session)");
			 } else {
				 try {
					String filename = URLDecoder.decode(url.getPath(), "UTF-8");
					File f = new File(filename);
					
					String reportName = f.getName();
					if(reportName.indexOf('.') > 0) {
						reportName = reportName.substring(0, reportName.lastIndexOf('.'));
					}
					reportNameLabel.setText(reportName);
					reportNameLabel.setToolTipText("Using report named '" + reportName + "'");
				} catch (UnsupportedEncodingException e) {
					LogUtil.warning(e);
				}
			 }
		 } else {
			 reportNameLabel.setText("Custom report (embedded)");
			 reportNameLabel.setToolTipText("Embedded report - use report composer to edit");
		 }
	}
	
	protected OpGraph loadReportGraph() throws IOException {
		if(this.reportURL == null) 
			throw new IOException(new NullPointerException());
		return OpgraphIO.read(this.reportURL.openStream());
	}

	@Override
	public void operate(OpContext ctx) throws ProcessingException {
		final Project project = (Project)ctx.get(projectInput);
		final String queryId = (String)ctx.get(queryInput);
		@SuppressWarnings("unchecked")
		final Object inputObj = (List<SessionPath>)ctx.get(sessionsInput);
		if(inputObj == null)
			throw new ProcessingException(null, "No input sessions");
		
		final List<RecordContainer> recordContainers =
				RecordContainer.toRecordContainers(project, inputObj);
		
		final OpGraph graph = getReportGraph();
		if(graph == null)
			throw new ProcessingException(null, "Report graph not found");
		
		final ReportTree reportTree = new ReportTree();
		final Processor processor = new Processor(graph);
		processor.getContext().put("_project", project);
		processor.getContext().put("_queryId", queryId);
		processor.getContext().put("_selectedSessions", recordContainers);
		processor.getContext().put("_reportTree", reportTree);
		
		// execute graph
		processor.stepAll();
		
		ctx.put(reportOutput, reportTree);
	}

	private void setupReportSelectionMenu(JPopupMenu menu) {
		menu.removeAll();
		
		// default report
		final PhonUIAction defaultReportAct = new PhonUIAction(this, "setReportGraphURL", getDefaultReportURL());
		defaultReportAct.putValue(PhonUIAction.NAME, "Default report");
		defaultReportAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Use default report (query information, aggregate, tables by session)");
		menu.add(defaultReportAct);
		
		// TODO library reports
		
		menu.addSeparator();
		// browse for report
		final PhonUIAction browseAct = new PhonUIAction(this, "onBrowseForReport");
		browseAct.putValue(PhonUIAction.NAME, "Browse...");
		browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for report file on disk");
		menu.add(browseAct);
	}
	
	public void onBrowseForReport() {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setAllowMultipleSelection(false);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setCanChooseFiles(true);
		props.setCanChooseDirectories(false);
		props.setTitle("Browse for report");
		props.setRunAsync(true);
		props.setListener(this::browseDialogFinished);
		NativeDialogs.showOpenDialog(props);
	}
	
	public void browseDialogFinished(NativeDialogEvent evt) {
		if(evt.getDialogData() == null) return;
		
		String selectedPath = evt.getDialogData().toString();
		File selectedFile = new File(selectedPath);
		
		// attempt to open as an opgraph file
		try {
			OpGraph graph = OpgraphIO.read(selectedFile);
			
			// ensure graph is a report document
			if(graph.getExtension(ReportWizardExtension.class) == null) {
				throw new IOException("Selected file is not a report document");
			}
			
			setReportGraphURL(selectedFile.toURI().toURL());
		} catch (IOException e) {
			if(CommonModuleFrame.getCurrentFrame() != null) {
				CommonModuleFrame.getCurrentFrame().showMessageDialog("Error", "Could not open report " + e.getLocalizedMessage(), MessageDialogProperties.okOptions);
			}
			
			Toolkit.getDefaultToolkit().beep();
			LogUtil.warning(e);
		}
	}
	
	@Override
	public Component getComponent(GraphDocument arg0) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel();
			
			final Action act = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
				}
			};
			act.putValue(Action.NAME, "Select report");
			act.putValue(Action.SHORT_DESCRIPTION, "Select query report");
			act.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/report", IconSize.SMALL));
			act.putValue(DropDownButton.ARROW_ICON_GAP, 2);
			act.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
			
			final JPopupMenu menu = new JPopupMenu();
			act.putValue(DropDownButton.BUTTON_POPUP, menu);
			
			menu.addPopupMenuListener(new PopupMenuListener() {
				
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					setupReportSelectionMenu(menu);
				}
				
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}
				
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}
				
			});
			selectReportButton = new DropDownButton(act);
			selectReportButton.setOnlyPopup(true);
			
			reportNameLabel = new JLabel();
			updateLabel();

			settingsPanel.setLayout(new VerticalLayout());
			settingsPanel.add(reportNameLabel);
			settingsPanel.add(selectReportButton);
		}
		return settingsPanel;
	}

	// settings are handled by a custom xml serializer
	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		return retVal;
	}

	@Override
	public void loadSettings(Properties arg0) {
		
	}

}
