package ca.phon.app.session.check;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXBusyLabel;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.JSArray;
import com.teamdev.jxbrowser.chromium.JSObject;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;

import ca.phon.app.log.LogUtil;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.session.SessionSelector;
import ca.phon.app.session.SessionSelectorActiveEditorSupport;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.ProcessorEvent;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.PluginManager;
import ca.phon.project.Project;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.session.SessionPath;
import ca.phon.session.check.SessionCheck;
import ca.phon.ui.CommonModuleFrameCreatedListener;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.wizard.BreadcrumbWizardFrame;
import ca.phon.ui.wizard.WizardStep;

/**
 * Check sessions for errors.
 */
public class SessionCheckWizard extends NodeWizard {

	private static final long serialVersionUID = 6650736926995551274L;
	
	private final static String SESSION_CHECK_GRAPH = "session_check.xml";

	private SessionSelector sessionSelector;
	private SessionSelectorActiveEditorSupport editorSupport;
		
	public static SessionCheckWizard newWizard(Project project) {
		final InputStream in = SessionCheckWizard.class.getResourceAsStream(SESSION_CHECK_GRAPH);
		if(in == null) throw new IllegalStateException(SESSION_CHECK_GRAPH + " not found");
		
		try {
			OpGraph graph = OpgraphIO.read(in);
			
			return new SessionCheckWizard(project, new Processor(graph), graph);
		} catch (IOException e) {
			LogUtil.severe(e);
			throw new IllegalStateException(e);
		}
	}
	
	private SessionCheckWizard(Project project, Processor processor, OpGraph graph) {
		super("Session Check", processor, graph);
		
		putExtension(Project.class, project);
		
		globalOptionsPanel.setVisible(false);
		
		init();
	}
	
	public Project getProject() {
		return getExtension(Project.class);
	}
	
	private void init() {
		WizardStep step1 = getWizardStep(0);
		
		TitledPanel tp = new TitledPanel("Select Sessions");
		sessionSelector = new SessionSelector(getProject());
		editorSupport = new SessionSelectorActiveEditorSupport();
		editorSupport.install(sessionSelector);
		sessionSelector.setPreferredSize(new Dimension(350, 0));
		tp.setLayout(new BorderLayout());
		tp.add(new JScrollPane(sessionSelector), BorderLayout.CENTER);
		step1.add(tp, BorderLayout.WEST);
	}
	
	
		
	@SuppressWarnings("unchecked")
	@Override
	public void executionEnded(ProcessorEvent pe) {
		super.executionEnded(pe);
		if(super.reportBufferAvailable()) return;
		
		final Browser browser = getBufferPanel().getBuffer("Report").getBrowser();
		JSValue tableMapVal = browser.executeJavaScriptAndReturnValue("tableMap");
		JSValue tableIds = browser.executeJavaScriptAndReturnValue("tableIds");
		if(tableMapVal == null || tableIds == null) return;
		Map<String, DefaultTableDataSource> tableMap = (Map<String, DefaultTableDataSource>)tableMapVal.asJavaObject();
		JSArray tableArray = tableIds.asArray();
		for(int i = 0; i < tableArray.length(); i++) {
			String tableId = tableArray.get(i).getStringValue();
			DefaultTableDataSource table = tableMap.get(tableId);
			if(table == null) continue;
			
			System.out.println(tableId);
		}
	}

	@Override
	public void next() {
		if(getCurrentStepIndex() == 0) {
			List<SessionPath> selectedSessions = sessionSelector.getSelectedSessions();
			if(selectedSessions.size() == 0) {
				showMessageDialog("Select Sessions", "Please select at least one session", MessageDialogProperties.okOptions);
				return;
			}
			
			getProcessor().getContext().put("_project", getProject());
			getProcessor().getContext().put("_selectedSessions", selectedSessions);
		}
		super.next();
	}
	
}
