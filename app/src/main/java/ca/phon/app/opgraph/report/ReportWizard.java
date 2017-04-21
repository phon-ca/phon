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

import javax.swing.JMenuBar;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.editor.actions.OpenNodeEditorAction;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
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
		
		final MenuBuilder builder = new MenuBuilder(menuBar);
		builder.addSeparator("File@1", "save");
		builder.addItem("File@save", new SaveReportAction(this));
		
		final PhonUIAction openEditorAct = new PhonUIAction(this, "onOpenEditor");
		openEditorAct.putValue(PhonUIAction.NAME, "Open report in Composer...");
		builder.addItem("File@" + SaveReportAction.TXT, openEditorAct);
	}
	
	public void onOpenEditor(PhonActionEvent pae) {
		final OpenNodeEditorAction act = new OpenNodeEditorAction(getGraph());
		act.actionPerformed(pae.getActionEvent());
		
		dispose();
	}
	
	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("Report", "Reports");
	}
	
}
