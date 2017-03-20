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
package ca.phon.app.opgraph.macro;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.editor.OpgraphEditorModelInfo;
import ca.phon.app.opgraph.nodes.PhonNodeLibrary;
import ca.phon.util.Tuple;

@OpgraphEditorModelInfo(name="General", description="Empty graph with default context")
public class MacroOpgraphEditorModel extends OpgraphEditorModel {

	public MacroOpgraphEditorModel() {
		this(new OpGraph());
	}

	public MacroOpgraphEditorModel(OpGraph opgraph) {
		super(opgraph);

		PhonNodeLibrary.install(getNodeLibrary().getLibrary());
	}

	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("macro", "macros");
	}

	@Override
	public String getTitle() {
		return "Composer (Macro)";
	}

}
