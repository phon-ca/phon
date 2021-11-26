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
package ca.phon.app.opgraph.editor;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.undo.*;

import ca.phon.app.opgraph.library.*;
import ca.phon.app.opgraph.nodes.ScriptNodeEditor;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.app.query.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.components.*;
import ca.phon.opgraph.app.components.canvas.*;
import ca.phon.opgraph.app.edits.graph.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.extensions.*;
import ca.phon.opgraph.library.*;
import ca.phon.query.db.Script;
import ca.phon.ui.jbreadcrumb.*;
import ca.phon.util.*;

/**
 * Base model for the opgraph editor.
 */
public abstract class OpgraphEditorModel extends GraphEditorModel {

	private Map<String, JComponent> viewMap;

	private NodeSettingsPanel nodeSettingsPanel;

	private OpgraphScriptEditor opgraphScriptEditor;

	public static enum ViewLocation {
		NORTH,
		EAST,
		SOUTH,
		WEST,
		CENTER;
	}

	public OpgraphEditorModel() {
		this(new OpGraph());
	}

	public OpgraphEditorModel(OpGraph opgraph) {
		super();

		getDocument().reset(null, opgraph);

		getDocument().getUndoSupport().addUndoableEditListener( (e) -> {
			final WizardExtension wizardExt = getDocument().getGraph().getExtension(WizardExtension.class);
			if(wizardExt == null) return;

			final UndoableEdit edit = e.getEdit();
			if(edit instanceof DeleteNodesEdit) {
				for(OpNode node:((DeleteNodesEdit)edit).getNodes()) {
					removeNode(wizardExt, node);
				}
			}
		} );
	}

	private void removeNode(WizardExtension wizardExt, OpNode node) {
		if(wizardExt.isNodeOptional(node))
			wizardExt.removeOptionalNode(node);
		if(wizardExt.containsNode(node)) {
			wizardExt.setNodeForced(node, false);
			wizardExt.removeNode(node);
		}
		if(node instanceof CompositeNode) {
			final OpGraph graph = ((CompositeNode)node).getGraph();
			for(OpNode subnode:graph.getVertices()) {
				removeNode(wizardExt, subnode);
			}
		}
	}

	protected Map<String, JComponent> getViewMap() {
		if(viewMap == null) {
			viewMap = new TreeMap<>();
			final JPanel canvasPanel = new JPanel(new BorderLayout());
			canvasPanel.add(new JBreadcrumbScrollPane<>(getBreadcrumb()), BorderLayout.NORTH);
			canvasPanel.add(new GraphCanvasScroller(getCanvas()), BorderLayout.CENTER);
			viewMap.put("Canvas", canvasPanel);
			viewMap.put("Console", new JScrollPane(getConsolePanel()));
			viewMap.put("Debug", new JScrollPane(getDebugInfoPanel()));
			viewMap.put("Connections", new JScrollPane(getNodeFieldsPanel()));
			viewMap.put("Library", getLibraryView());
			viewMap.put("Settings", getNodeSettings());
			viewMap.put("Script Editor", getScriptNodeEditor());
			viewMap.put("Outline", getGraphOutline());
		}
		return this.viewMap;
	}

	@Override
	public NodeSettingsPanel getNodeSettings() {
		if(nodeSettingsPanel == null) {
			nodeSettingsPanel = new NodeSettingsPanel(getDocument()) {
				@Override
				protected Component getNodeSettingsComponent(NodeSettings settings) {
					Component comp = settings.getComponent(getDocument());
					if(!(comp instanceof ScriptPanel)) {
						final JScrollPane scroller = new JScrollPane(comp);
						scroller.getVerticalScrollBar().setUnitIncrement(10);
						comp = scroller;
					}
					return comp;
				}
			};
		}
		return nodeSettingsPanel;
	}
	
	/**
	 * Get custom node library view.
	 *
	 * @return node library view
	 */
	public LibraryView getLibraryView() {
		final NodeLibrary library = getNodeLibrary().getLibrary();
		return new LibraryView(library);
	}

	/**
	 * Return the editor title for the given model.
	 *
	 * @return title
	 */
	public abstract String getTitle();

	/**
	 * Get the default folder when displaying the open/save dialog.
	 *
	 * @return folder path
	 */
	public String getDefaultFolder() {
		return PrefHelper.getUserDataFolder() + File.separator + "macros";
	}

	/**
	 * Return a list of all available view names.  Custom views
	 * should be added to this list by subclasses.
	 *
	 * @return list of available view names
	 */
	public List<String> getAvailableViewNames() {
		final List<String> retVal = new ArrayList<>();
		retVal.addAll(getViewMap().keySet());
		return retVal;
	}

	/**
	 * Get specified view component
	 *
	 * @param viewName
	 * @return
	 */
	public JComponent getView(String viewName) {
		return getViewMap().get(viewName);
	}

	public OpgraphScriptEditor getScriptNodeEditor() {
		if(opgraphScriptEditor == null) {
			opgraphScriptEditor = new OpgraphScriptEditor(getDocument());
		}
		return opgraphScriptEditor;
	}

	@Override
	public GraphCanvas getCanvas() {
		final GraphCanvas retVal = super.getCanvas();
		retVal.setPreferredSize(new Dimension(4096, 4096));
		return retVal;
	}

	/**
	 * Get initial view location as a rectangle.
	 *
	 * @return initial view rect
	 */
	public Rectangle getInitialViewBounds(String viewName) {
		Rectangle retVal = new Rectangle();
		switch(viewName) {
		case "Canvas":
			retVal.setBounds(200, 0, 600, 600);
			break;

		case "Outline":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Console":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Debug":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Connections":
			retVal.setBounds(800, 200, 200, 200);
			break;

		case "Library":
			retVal.setBounds(0, 0, 200, 200);
			break;

		case "Settings":
			retVal.setBounds(800, 0, 200, 200);
			break;

		case "Script Editor":
			retVal.setBounds(800, 200, 200, 200);
			break;

		default:
			retVal.setBounds(0, 0, 200, 200);
			break;
		}
		return retVal;
	}

	/**
	 *
	 * @return
	 */
	public ViewLocation getDefaultViewLocation(String viewName) {
		ViewLocation retVal = ViewLocation.CENTER;

		switch(viewName) {
		case "Canvas":
			retVal = ViewLocation.CENTER;
			break;

		case "Console":
			retVal = ViewLocation.WEST;
			break;

		case "Debug":
			retVal = ViewLocation.WEST;
			break;

		case "Connections":
			retVal = ViewLocation.CENTER;
			break;

		case "Library":
			retVal = ViewLocation.WEST;
			break;

		case "Settings":
			retVal = ViewLocation.CENTER;
			break;

		case "Script Editor":
			retVal = ViewLocation.WEST;
			break;

		case "Outline":
			retVal = ViewLocation.WEST;
			break;
		}
		return retVal;
	}

	/**
	 * Get initial visiblity of view.
	 *
	 * @return <code>true</code> if view is visible in default layout,
	 * <code>false</code> otherwise
	 */
	public boolean isViewVisibleByDefault(String viewName) {
		boolean retVal = false;
		switch(viewName) {
		case "Canvas":
			retVal = true;
			break;

		case "Console":
			retVal = false;
			break;

		case "Debug":
			retVal = true;
			break;

		case "Connections":
			retVal = true;
			break;

		case "Library":
			retVal = true;
			break;

		case "Settings":
			retVal = true;
			break;

		case "Script Editor":
			retVal = true;
			break;

		case "Outline":
			retVal = true;
			break;

		default:
			break;
		}
		return retVal;
	}

	/**
	 * Return the noun associated with the type of graph.
	 * The first element of the
	 * {@link Tuple} is the singleton version, while the
	 * second element is the plural.  String should be
	 * returned as all lower case.
	 *
	 * @return Tuple<String, String>
	 */
	public abstract Tuple<String, String> getNoun();

	/**
	 * Validate document before saving.
	 *
	 * @return <code>true</code> if document (graph) is valid, <code>false</code> otherwise
	 */
	public boolean validate() {
		final NodeEditorSettings settings = new NodeEditorSettings();
		settings.setModelType(getClass().getName());
		getDocument().getGraph().putExtension(NodeEditorSettings.class, settings);

		return true;
	}

	/**
	 * Called just before starting debuging.
	 *
	 * @param context
	 */
	public void setupContext(OpContext context) {
	}

}
