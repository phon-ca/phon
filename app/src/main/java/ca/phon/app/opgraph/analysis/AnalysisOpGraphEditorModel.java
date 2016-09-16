/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.editor.DefaultOpgraphEditorModel;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.workspace.Workspace;

public class AnalysisOpGraphEditorModel extends DefaultOpgraphEditorModel {

	private JPanel debugSettings;
	
	private JComboBox<Project> projectList;
	
	private SessionSelector sessionSelector;

	private NodeWizardPanel wizardPanel;
	
	public AnalysisOpGraphEditorModel() {
		this(new OpGraph());
	}

	public AnalysisOpGraphEditorModel(OpGraph opgraph) {
		super(opgraph);
		
		WizardExtension wizardExt = opgraph.getExtension(WizardExtension.class);
		if(wizardExt == null) {
			wizardExt = new AnalysisWizardExtension(opgraph);
			opgraph.putExtension(WizardExtension.class, wizardExt);
		}
	}
	
	public SessionSelector getSessionSelector() {
		return this.sessionSelector;
	}
	
	public NodeWizardPanel getWizardPanel() {
		if(wizardPanel == null) {
			wizardPanel = new NodeWizardPanel(getDocument(),
					getDocument().getGraph().getExtension(WizardExtension.class));
		}
		return wizardPanel;
	}

	@Override
	public List<String> getAvailableViewNames() {
		final List<String> retVal = new ArrayList<>(super.getAvailableViewNames());
		retVal.add("Wizard");
		return retVal;
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
			
			projectList.addItemListener( (e) -> {
				sessionSelector.setProject((Project)projectList.getSelectedItem());
			} );
			
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
			
		case "Wizard":
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
	
	@Override
	public JComponent getView(String viewName) {
		JComponent retVal = super.getView(viewName);
		if(viewName.equals("Wizard")) {
			retVal = getWizardPanel();
		}
		return retVal;
	}

	@Override
	public boolean isViewVisibleByDefault(String viewName) {
		return super.isViewVisibleByDefault(viewName)
				|| viewName.equals("Debug Settings")
				|| viewName.equals("Wizard");
	}

	@Override
	public String getDefaultFolder() {
		return super.getDefaultFolder();
	}
	
	@Override
	public String getTitle() {
		return "Analysis Editor";
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
