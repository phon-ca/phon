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
package ca.phon.app.opgraph.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.editor.OpgraphEditorModelFactory;
import ca.phon.app.opgraph.macro.MacroOpgraphEditorModel;
import ca.phon.opgraph.OpgraphIO;

public class OpenNodeEditorAction extends HookableAction {
	
	private final static Logger LOGGER = Logger.getLogger(OpenNodeEditorAction.class.getName());

	private final String TXT = "Open Node Editor";
	
	private final String DESC = "Open node editor";
	
	private URL documentURL;
	
	private OpGraph graph;
	
	public OpenNodeEditorAction() {
		super();
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	public OpenNodeEditorAction(URL documentURL) {
		super();
		
		this.documentURL = documentURL;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	public OpenNodeEditorAction(OpGraph opgraph) {
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} else if(getDocumentURL() != null) {
			try {
				OpgraphIO.read(getDocumentURL().openStream());
				editorModel = factory.fromGraph(graph);
			} catch (IOException | ClassNotFoundException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		final OpgraphEditor editor = new OpgraphEditor(editorModel);
		try {
			if(getDocumentURL() != null)
				editor.setCurrentFile(new File(documentURL.toURI()));
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		editor.pack();
		editor.setSize(1064, 768);
		editor.setLocationByPlatform(true);
		editor.setVisible(true);
	}

}
