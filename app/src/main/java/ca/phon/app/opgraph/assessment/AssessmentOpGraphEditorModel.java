package ca.phon.app.opgraph.assessment;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.editor.DefaultOpgraphEditorModel;
import ca.phon.app.opgraph.nodes.query.QueryNode;
import ca.phon.app.opgraph.nodes.query.QueryNodeData;
import ca.phon.app.opgraph.nodes.query.QueryNodeInstantiator;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.workspace.Workspace;

public class AssessmentOpGraphEditorModel extends DefaultOpgraphEditorModel {

	private JPanel debugSettings;
	
	private JComboBox<Project> projectList;
	
	private SessionSelector sessionSelector;
	
	public AssessmentOpGraphEditorModel() {
		this(new OpGraph());
	}

	public AssessmentOpGraphEditorModel(OpGraph opgraph) {
		super(opgraph);
		
		addQueryNodes();
		
		WizardExtension wizardExt = opgraph.getExtension(WizardExtension.class);
		if(wizardExt != null || !(wizardExt instanceof AssessmentWizardExtension)) {
			wizardExt = new AssessmentWizardExtension(opgraph);
			opgraph.putExtension(WizardExtension.class, wizardExt);
		}
	}
	
	@Override
	protected Map<String, JComponent> getViewMap() {
		final Map<String, JComponent> retVal = super.getViewMap();
		retVal.put("Debug Settings", getDebugSettings());
		return retVal;
	}
	
	protected JComponent getDebugSettings() {
		if(debugSettings == null) {
			debugSettings = new JPanel();
			
			final Workspace workspace = Workspace.userWorkspace();
			projectList = new JComboBox<Project>(workspace.getProjects().toArray(new Project[0]));
			projectList.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Project"), 
					projectList.getBorder()));
			
			projectList.addItemListener( (e) -> sessionSelector.setProject((Project)projectList.getSelectedItem()) );
			
			sessionSelector = new SessionSelector();
			final JScrollPane sessionScroller = new JScrollPane(sessionSelector);
			sessionScroller.setBorder(BorderFactory.createTitledBorder("Sessions"));
			
			debugSettings.setLayout(new BorderLayout());
			debugSettings.add(projectList, BorderLayout.NORTH);
			debugSettings.add(sessionScroller, BorderLayout.CENTER);
		}
		return debugSettings;
	}

	@Override
	public Rectangle getInitialViewBounds(String viewName) {
		Rectangle retVal = new Rectangle();
		switch(viewName) {
		case "Canvas":
			retVal.setBounds(200, 0, 600, 600);
			break;
			
		case "Debug Settings":
			retVal.setBounds(0, 200, 200, 200);
			break;
			
		case "Console":
			retVal.setBounds(0, 200, 200, 200);
			break;
			
		case "Debug":
			retVal.setBounds(0, 200, 200, 200);
			break;
			
		case "Defaults":
			retVal.setBounds(800, 200, 200, 200);
			break;
			
		case "Library":
			retVal.setBounds(0, 0, 200, 200);
			break;
			
		case "Settings":
			retVal.setBounds(800, 0, 200, 200);
			break;
			
		default:
			retVal.setBounds(0, 0, 200, 200);
			break;
		}
		return retVal;
	}
	
	private void addQueryNodes() {
		Consumer<QueryScript> addToLibrary = (QueryScript script) -> {
			final QueryName qn = script.getExtension(QueryName.class);
			final String name = (qn != null ? qn.getName() : "<unknown>");
			try {
				final URI queryNodeClassURI = new URI("class", QueryNode.class.getName(), qn.getName());
				final QueryNodeInstantiator instantiator = new QueryNodeInstantiator();
				
				final QueryNodeData nodeData = new QueryNodeData(script, queryNodeClassURI, name, "", "Query", instantiator);
				getNodeLibrary().getLibrary().put(nodeData);
			} catch (URISyntaxException e) {
				
			}
		};
		
		final QueryScriptLibrary library = new QueryScriptLibrary();
		library.stockScriptFiles().forEach(addToLibrary);
		library.userScriptFiles().forEach(addToLibrary);
	}

	@Override
	public boolean isViewVisibleByDefault(String viewName) {
		return super.isViewVisibleByDefault(viewName)
				|| viewName.equals("Debug Settings");
	}

	@Override
	public String getDefaultFolder() {
		return super.getDefaultFolder();
	}

	@Override
	public boolean validate() {
		return super.validate();
	}

	@Override
	public void setupContext(OpContext context) {
		super.setupContext(context);
		
		context.put("_project", projectList.getSelectedItem());
		context.put("_selectedSessions", sessionSelector.getSelectedSessions());
	}
	
}
