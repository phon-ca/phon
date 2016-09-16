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
package ca.phon.app.opgraph.report;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.editor.NewDialogPanel;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.nodes.query.QueryHistoryNode;

public class ReportNewPanel extends NewDialogPanel {

	private static final long serialVersionUID = 3486960052647071379L;

	@Override
	public String getTitle() {
		return "Report";
	}

	@Override
	public OpgraphEditorModel createModel() {
		final OpGraph graph = new OpGraph();
		
		final QueryHistoryNode historyNode = new QueryHistoryNode();
		graph.add(historyNode);
		
		return new ReportOpGraphEditorModel(graph);
	}

}
