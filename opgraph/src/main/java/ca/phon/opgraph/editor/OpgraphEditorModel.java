package ca.phon.opgraph.editor;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;

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
			canvasPanel.add(getBreadcrumb(), BorderLayout.NORTH);
			canvasPanel.add(new JScrollPane(getCanvas()), BorderLayout.CENTER);
			viewMap.put("Canvas", canvasPanel);
			viewMap.put("Console", getConsolePanel());
			viewMap.put("Debug", getDebugInfoPanel());
			viewMap.put("Defaults", getNodeDefaults());
			viewMap.put("Library", getNodeLibrary());
			viewMap.put("Settings", getNodeSettings());
		}
		return this.viewMap;
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
			
		case "Console":
			retVal.setBounds(0, 200, 200, 200);
			break;
			
		case "Debug":
			retVal.setBounds(0, 200, 200, 200);
			break;
			
		case "Defaults":
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
			
		case "Defaults":
			retVal = true;
			break;
			
		case "Library":
			retVal = true;
			break;
			
		case "Settings":
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
