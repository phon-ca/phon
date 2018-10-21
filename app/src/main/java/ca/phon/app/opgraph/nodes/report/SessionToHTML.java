package ca.phon.app.opgraph.nodes.report;

import java.awt.Component;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;

@OpNodeInfo(name="Session to HTML", description="Add session as HTML", category="Report", showInLibrary=true)
public class SessionToHTML extends OpNode implements NodeSettings {
	
	private final InputField projectInput = new InputField("project", "Project", false, true, Project.class);
	
	private final InputField sessionInput = new InputField("session", "Session", false, true, SessionPath.class, Session.class);

	private final OutputField htmlOutput = new OutputField("html", "HTML output", true, String.class);
	
	private boolean includeSyllabification = false;
	
	private boolean includeAlignment = false;
	
	private JPanel settingsPanel;
	private JCheckBox includeAlignmentBox;
	private JCheckBox includeSyllabificationBox;
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel != null) {
			
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadSettings(Properties properties) {
		// TODO Auto-generated method stub
		
	}
	
}
