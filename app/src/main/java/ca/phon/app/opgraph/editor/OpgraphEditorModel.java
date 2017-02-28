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
package ca.phon.app.opgraph.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.canvas.GraphCanvas;
import ca.gedge.opgraph.library.NodeLibrary;
import ca.phon.app.opgraph.editor.library.LibraryView;
import ca.phon.ui.jbreadcrumb.JBreadcrumbScrollPane;
import ca.phon.util.PrefHelper;

/**
 * Base model for the opgraph editor.
 */
public abstract class OpgraphEditorModel extends GraphEditorModel {
	
	private Map<String, JComponent> viewMap;
	
	public OpgraphEditorModel() {
		this(new OpGraph());
	}

	public OpgraphEditorModel(OpGraph opgraph) {
		super();
		
		getDocument().reset(null, opgraph);
	}
	
	protected Map<String, JComponent> getViewMap() {
		if(viewMap == null) {
			viewMap = new TreeMap<>();
			final JPanel canvasPanel = new JPanel(new BorderLayout());
			canvasPanel.add(new JBreadcrumbScrollPane<>(getBreadcrumb()), BorderLayout.NORTH);
			canvasPanel.add(new JScrollPane(getCanvas()), BorderLayout.CENTER);
			viewMap.put("Canvas", canvasPanel);
			viewMap.put("Console", new JScrollPane(getConsolePanel()));
			viewMap.put("Debug", new JScrollPane(getDebugInfoPanel()));
			viewMap.put("Connections", new JScrollPane(getNodeFieldsPanel()));
			viewMap.put("Library", getLibraryView());
			viewMap.put("Settings", getNodeSettings());
			viewMap.put("Outline", getGraphOutline());
		}
		return this.viewMap;
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
			
		default:
			retVal.setBounds(0, 0, 200, 200);
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
			
		case "Outline":
			retVal = true;
			break;
			
		default:
			break;
		}
		return retVal;
	}
	
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
