/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.opgraph.report;

import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.app.query.ResultSetSelector;
import ca.phon.opgraph.*;
import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.session.SessionPath;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.Tuple;
import org.apache.commons.lang.StringEscapeUtils;

import java.awt.*;
import java.io.OutputStream;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

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
	protected NodeWizardReportGenerator createReportGenerator(ReportTree reportTree, String reportTemplate, OutputStream fout) {
		final NodeWizardReportGenerator retVal = super.createReportGenerator(reportTree, reportTemplate, fout);
		final Project project = (Project)getProcessor().getContext().get("_project");
		if(project != null) {
			retVal.addCustomJs(String.format("window.projectLocation = '%s'", StringEscapeUtils.escapeHtml(project.getLocation())));
		}
		return retVal;
	}
	
	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("Report", "Reports");
	}
	
}
