/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.project;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.phon.project.Project;
import ca.phon.workspace.Workspace;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Form for copying/moving sessions.  This form
 * displays an area for selecting a session to move
 * and a project/corpus to copy/move to.
 *
 */
public class CopySessionForm extends JPanel {
	/** The project combos */
	private JComboBox proj1Combo;
	private JComboBox proj2Combo;
	
	/** The corpus combo */
	private JComboBox corpus1Combo;
	private JComboBox corpus2Combo;
	
	/** The session to copy/move */
	private JComboBox sessionCombo;
	
	/** Constructor */
	public CopySessionForm() {
		super();
		
		init();
	}
	
	private void init() {
		// setup layout
		FormLayout layout = new FormLayout(
				"5dlu, pref, 3dlu, fill:pref:grow, 5dlu",
				"5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 5dlu, pref, 3dlu, pref, 3dlu, pref, 5dlu");
		CellConstraints cc = new CellConstraints();
		
		this.setLayout(layout);
		
		// create components
		final List<Project> openProjects = 
			Workspace.userWorkspace().getProjects();
		
		sessionCombo = new JComboBox();
		
		corpus1Combo = new JComboBox();
		corpus1Combo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				fillSessionList();
			}
			
		});
		corpus2Combo = new JComboBox();
		
		proj1Combo = new JComboBox(openProjects.toArray());
		proj1Combo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				fillCorpusList(proj1Combo);
			}
			
		});
		proj1Combo.setSelectedIndex(0);
		
		fillCorpusList(proj1Combo);
		fillSessionList();
		
		proj2Combo = new JComboBox(openProjects.toArray());
		proj2Combo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				fillCorpusList(proj2Combo);
			}
			
		});
		proj2Combo.setSelectedIndex(0);
		
		fillCorpusList(proj2Combo);
		
		// add components
		this.add(DefaultComponentFactory.getInstance().createSeparator("Selection"),
				cc.xyw(2, 2, 3));
		this.add(new JLabel("Project"), cc.xy(2, 4));
		this.add(proj1Combo, cc.xy(4, 4));
		
		this.add(new JLabel("Corpus"), cc.xy(2, 6));
		this.add(corpus1Combo, cc.xy(4, 6));
		
		this.add(new JLabel("Session"), cc.xy(2, 8));
		this.add(sessionCombo, cc.xy(4, 8));
		
		this.add(DefaultComponentFactory.getInstance().createSeparator("Destination"),
				cc.xyw(2, 10, 3));
		this.add(new JLabel("Project"), cc.xy(2, 12));
		this.add(proj2Combo, cc.xy(4, 12));
		
		this.add(new JLabel("Corpus"), cc.xy(2, 14));
		this.add(corpus2Combo, cc.xy(4, 14));
	}
	
	private void fillCorpusList(JComboBox projectBox) {
		// what corpus list do we fill in?
		JComboBox corpusBox = null;
		if(projectBox == proj1Combo)
			corpusBox = corpus1Combo;
		else if(projectBox == proj2Combo)
			corpusBox = corpus2Combo;
		
		if(corpusBox == null) return;
		
		// get the selected project
		Project selectedProject = 
			(Project)projectBox.getSelectedItem();
		corpusBox.removeAllItems();
		
		for(String corpus:selectedProject.getCorpora())
			corpusBox.addItem(corpus);
	}
	
	private void fillSessionList() {
		// get the selected project and corpus
		Project project = 
			(Project)proj1Combo.getSelectedItem();
		String corpus = 
			(String)corpus1Combo.getSelectedItem();
		
		sessionCombo.removeAllItems();
		for(String session:project.getCorpusSessions(corpus)) {
			sessionCombo.addItem(session);
		}
	}
	
	/* Getters/Setters */
	public Project getSelectedProject() {
		return (Project)proj1Combo.getSelectedItem();
	}
	
	public void setSelectedProject(Project project) {
		proj1Combo.setSelectedItem(project);
	}
	
	public Project getDestinationProject() {
		return (Project)proj2Combo.getSelectedItem();
	}
	
	public void setDestinationProject(Project project) {
		proj2Combo.setSelectedItem(project);
	}
	
	public String getSelectedCorpus() {
		return corpus1Combo.getSelectedItem().toString();
	}
	
	public void setSelectedCorpus(String corpus) {
		corpus1Combo.setSelectedItem(corpus);
	}
	
	public String getDestinationCorpus() {
		return corpus2Combo.getSelectedItem().toString();	
	}
	
	public String getSelectedSession() {
		return sessionCombo.getSelectedItem().toString();
	}
	
	public void setSelectedSession(String session) {
		sessionCombo.setSelectedItem(session);
	}
}
