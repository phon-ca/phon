/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.macro;

import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.editor.OpgraphEditorModelInfo;
import ca.phon.app.opgraph.nodes.PhonNodeLibrary;
import ca.phon.opgraph.OpGraph;
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