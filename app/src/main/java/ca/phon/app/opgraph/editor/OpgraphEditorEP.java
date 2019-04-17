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
package ca.phon.app.opgraph.editor;

import java.io.File;
import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.app.opgraph.macro.MacroOpgraphEditorModel;
import ca.phon.plugin.IPluginEntryPoint;

public class OpgraphEditorEP implements IPluginEntryPoint {

	public final static String OPGRAPH_FILE_KEY = "file";
	
	public final static String OPGRAPH_MODEL_KEY = "model";
	
	public final static String EP_NAME = "OpgraphEditor";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final OpgraphEditorModel model =
				(args.containsKey(OPGRAPH_MODEL_KEY) ? (OpgraphEditorModel)args.get(OPGRAPH_MODEL_KEY) : 
					new MacroOpgraphEditorModel());
		final Runnable onEDT = () -> {
			final OpgraphEditor editor = new OpgraphEditor(model);
			
			if(args.containsKey(OPGRAPH_FILE_KEY)) {
				editor.setCurrentFile((File)args.get(OPGRAPH_FILE_KEY));
			}
			
			editor.pack();
			editor.setSize(1064, 768);
			editor.setLocationByPlatform(true);
			editor.setVisible(true);
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

}
