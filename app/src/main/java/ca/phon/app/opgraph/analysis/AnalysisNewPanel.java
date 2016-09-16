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

import java.util.ArrayList;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.app.opgraph.editor.NewDialogPanel;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.project.Project;

public class AnalysisNewPanel extends NewDialogPanel {

	private static final long serialVersionUID = 8397066948261210026L;

	@Override
	public String getTitle() {
		return "Analysis";
	}

	@Override
	public OpgraphEditorModel createModel() {
		final OpGraph graph = new OpGraph();
		
		final ObjectNode projectNode = new ObjectNode(Project.class);
		projectNode.setContextKey("_project");
		graph.add(projectNode);
		
		final ObjectNode sessionListNode = new ObjectNode(ArrayList.class);
		sessionListNode.setContextKey("_selectedSessions");
		sessionListNode.setName("Selected Sessions");
		graph.add(sessionListNode);
		
		return new AnalysisOpGraphEditorModel(graph);
	}

}
