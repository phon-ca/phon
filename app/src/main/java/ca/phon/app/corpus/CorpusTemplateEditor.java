package ca.phon.app.corpus;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import ca.phon.app.session.editor.RecordEditorPerspective;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;

public class CorpusTemplateEditor extends SessionEditor {

	private static final long serialVersionUID = -5676865486332827373L;

	private final static String PERSPECTIVE_NAME = "CorpusTemplate";
	private final static String PERSPECTIVE_FILE = "CorpusTemplate.xml";
	
	public CorpusTemplateEditor(Project project, Session session) {
		super(project, session, null);
		
		// setup custom perspective
		final URL perspectiveURL = getClass().getResource(PERSPECTIVE_FILE);
		if(perspectiveURL != null) {
			final RecordEditorPerspective perspective = new RecordEditorPerspective(PERSPECTIVE_NAME, perspectiveURL);
			getViewModel().applyPerspective(perspective);
		}
		
		getToolbar().setVisible(false);
	}
	
	@Override
	public String getTitle() {
		return "Session Template : " + getSession().getCorpus();
	}
	
	@Override
	public boolean saveData() 
			throws IOException {
		final Project project = getProject();
		project.saveSessionTemplate(getSession().getCorpus(), getSession());
		setModified(false);
		return true;
	}

}
