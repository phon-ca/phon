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

import javax.swing.*;

import ca.gedge.opgraph.*;
import ca.phon.app.opgraph.editor.actions.OpenComposerAction;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.project.Project;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.Tuple;

public class ReportWizard extends NodeWizard {

	private static final long serialVersionUID = 3616649077398530316L;

	public ReportWizard(String title, Processor processor, OpGraph graph) {
		super(title, processor, graph);
		gotoStep(0);
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
