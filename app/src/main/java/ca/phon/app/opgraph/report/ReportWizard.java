/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.report;

import java.awt.BorderLayout;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.*;

import ca.gedge.opgraph.*;
import ca.phon.app.opgraph.editor.actions.OpenComposerAction;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.query.ResultSetSelector;
import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.session.SessionPath;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.Tuple;

public class ReportWizard extends NodeWizard {

	private static final long serialVersionUID = 3616649077398530316L;

	private WizardStep selectResultSetsStep;
	private ResultSetSelector resultSetSelector;

	public ReportWizard(String title, Processor processor, OpGraph graph) {
		super(title, processor, graph);
		
		selectResultSetsStep = addSelectResultSetsStep();
		
		gotoStep(0);
	}
	
	public List<SessionPath> getSelectedSessions() {
		final ResultSet[] selectedResultSets = resultSetSelector.getSelectedSearches();
		
		return Arrays.stream(selectedResultSets)
					.map( (rs) -> new SessionPath(rs.getSessionPath()) )
					.sorted()
					.collect( Collectors.toList() );
	}
	
	private WizardStep addSelectResultSetsStep() {
		WizardStep retVal = new WizardStep() {
			@Override
			public boolean validateStep() {
				return resultSetSelector.getSelectedSearches().length > 0;
			}
			
		};
		
		retVal.setTitle("Select Results");
		retVal.setLayout(new BorderLayout());
		
		final Processor processor = getProcessor();
		final Project project = (Project)processor.getContext().get("_project");
		final String queryId = (String)processor.getContext().get("_queryId");
		
		final QueryManager queryManager = QueryManager.getSharedInstance();
		final ResultSetManager rsManager = queryManager.createResultSetManager();
		final List<Query> projectQueries = rsManager.getQueries(project);
		final String qId = queryId;

		Optional<Query> selectedQuery =
				projectQueries.parallelStream()
					.filter( q -> q.getUUID().toString().equals(qId) )
					.findAny();
		if(selectedQuery.isPresent()) {
			resultSetSelector = new ResultSetSelector(project, selectedQuery.get());
			resultSetSelector.selectAll();
		}
		
		TitledPanel tp = new TitledPanel("Result Sets", resultSetSelector);
		retVal.add(tp, BorderLayout.CENTER);
		
		int insertIdx = 0;
		if(getWizardExtension().getWizardMessage() != null
				&& getWizardExtension().getWizardMessage().length() > 0) {
			insertIdx = 1;
		}
		retVal.setNextStep(insertIdx+1);
		retVal.setPrevStep(insertIdx-1);

		super.addWizardStep(insertIdx, retVal);

		if(insertIdx == 1) {
			getWizardStep(0).setNextStep(insertIdx);
		}
		getWizardStep(insertIdx+1).setPrevStep(insertIdx);
		
		return retVal;
	}
	
	@Override
	public void gotoStep(int stepIdx) {
		if(getWizardStep(stepIdx) == reportDataStep) {
			if(resultSetSelector != null) {
				getProcessor().getContext().put("_selectedSessions", getSelectedSessions());
			}
		}
		super.gotoStep(stepIdx);
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		
		final Processor processor = getProcessor();
		final Project project = (Project)processor.getContext().get("_project");
		final String queryId = (String)processor.getContext().get("_queryId");
		
		final MenuBuilder builder = new MenuBuilder(menuBar);
		builder.addSeparator("File@1", "composer");
		
		final OpenSimpleReportComposerAction openSimpleComposerAct = new OpenSimpleReportComposerAction(project, queryId, getGraph());
		openSimpleComposerAct.putValue(Action.NAME, "Open report in Composer (simple)...");
		builder.addItem("File@composer", openSimpleComposerAct).addActionListener( (e) -> close() );
		
		final OpenComposerAction openComposerAct = new OpenComposerAction(getGraph());
		openComposerAct.putValue(Action.NAME, "Open report in Composer (advanced)...");
		builder.addItem("File@" + openSimpleComposerAct.getValue(Action.NAME), openComposerAct).addActionListener( (e) -> close() );
	}
	
	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("Report", "Reports");
	}
	
}
