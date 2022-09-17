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
package ca.phon.app.opgraph.editor.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.macro.MacroOpgraphEditorModel;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.app.OpgraphIO;

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;

public class OpenComposerAction extends HookableAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(OpenComposerAction.class.getName());

	private final String TXT = "Composer (advanced)...";
	
	private final String DESC = "Open advanced Composer";
	
	private URL documentURL;
	
	private OpGraph graph;
	
	public OpenComposerAction() {
		super();
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	public OpenComposerAction(URL documentURL) {
		super();
		
		this.documentURL = documentURL;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	public OpenComposerAction(OpGraph opgraph) {
		super();
		
		this.graph = opgraph;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	public URL getDocumentURL() {
		return this.documentURL;
	}
	
	public void setDocumentURL(URL documentURL) {
		this.documentURL = documentURL;
	}
	
	public OpGraph getGraph() {
		return graph;
	}

	public void setGraph(OpGraph graph) {
		this.graph = graph;
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final OpgraphEditorModelFactory factory = new OpgraphEditorModelFactory();
		OpgraphEditorModel editorModel = new MacroOpgraphEditorModel();
		if(getGraph() != null) {
			try {
				editorModel = factory.fromGraph(getGraph());
			} catch (ClassNotFoundException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		} else if(getDocumentURL() != null) {
			try {
				OpgraphIO.read(getDocumentURL().openStream());
				editorModel = factory.fromGraph(graph);
			} catch (IOException | ClassNotFoundException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		final OpgraphEditor editor = new OpgraphEditor(editorModel);
		try {
			if(getDocumentURL() != null)
				editor.setCurrentFile(new File(documentURL.toURI()));
		} catch (URISyntaxException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		editor.pack();
		editor.setSize(1064, 768);
		editor.setLocationByPlatform(true);
		editor.setVisible(true);
	}

}
