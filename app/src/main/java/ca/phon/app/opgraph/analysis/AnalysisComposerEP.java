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
package ca.phon.app.opgraph.analysis;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import ca.phon.app.log.*;
import ca.phon.app.modules.*;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.nodes.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.dag.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.plugin.*;
import ca.phon.project.*;
import ca.phon.ui.*;
import ca.phon.ui.nativedialogs.*;

@PhonPlugin(name=AnalysisComposerEP.EP_NAME)
public class AnalysisComposerEP implements IPluginEntryPoint {

	public final static String EP_NAME = "Analysis Composer";
	
	public final static String ANALYSIS_GRAPH = "graph";
	
	public final static String ANALYSIS_FILE = "file";
	
	private volatile CountDownLatch latch = new CountDownLatch(0);

	private SimpleEditor editor;

	@Override
	public String getName() {
		return EP_NAME;
	}

	public Future<SimpleEditor> getFutureEditor() {
		var retVal = new FutureTask<SimpleEditor>( () -> {
			latch.await();
			return AnalysisComposerEP.this.editor;
		});
		retVal.run();
		return retVal;
	}
	
	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		
		final Project project = epArgs.getProject();
		final OpGraph graph = epArgs.containsKey(ANALYSIS_GRAPH) ? (OpGraph)epArgs.get(ANALYSIS_GRAPH) : new OpGraph();
		final File file = (File)epArgs.get(ANALYSIS_FILE);
		latch = new CountDownLatch(1);
		Runnable onEDT = () -> {
			editor =
					new SimpleEditor(project, new AnalysisLibrary(), graph,
							new AnalysisEditorModelInstantiator(), new AnalysisNodeInstantiator(),
							(qs, reportGraph) ->  {
								try {
									return AnalysisLibrary.analysisFromQuery(qs, reportGraph);
								} catch (IOException | IllegalArgumentException | ItemMissingException | VertexNotFoundException | CycleDetectedException | InstantiationException | URISyntaxException | InvalidEdgeException e) {
									LogUtil.severe( e.getLocalizedMessage(), e);
									final MessageDialogProperties props = new MessageDialogProperties();
									props.setTitle("Composer (simple)");
									props.setHeader("Unable to create analysis from query");
									props.setMessage(e.getLocalizedMessage());
									props.setOptions(MessageDialogProperties.okOptions);
									props.setRunAsync(true);
									props.setParentWindow(CommonModuleFrame.getCurrentFrame());
									NativeDialogs.showMessageDialog(props);
								}
								return new MacroNode();
							} ,
							AnalysisRunner::new );
			if(file != null)
				editor.setCurrentFile(file);
			
			editor.getEditor().setIncludeQueries(true);			
			editor.pack();
			editor.setSize(new Dimension(1024, 768));
			editor.centerWindow();
			editor.setVisible(true);
			
			latch.countDown();
		};
		
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
		
	}

}
