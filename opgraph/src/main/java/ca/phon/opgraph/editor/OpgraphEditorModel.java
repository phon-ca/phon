package ca.phon.opgraph.editor;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;

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
	
	public final static String MODEL_TYPE_KEY = "ca.phon.opgraph.editor.modeltype";
	
	/**
	 * Return a list of available models types.  Each model should 
	 * be a subclass of {@link OpgraphEditorModel}.  Each subclass
	 * should implement both the default constructor and the {@link OpGraph}
	 * constructor as well as handle both cases in the {@link IPluginExtensionFactory}
	 * implementation.
	 * 
	 * @return list of available editor model types
	 */
	public static List<Class<? extends OpgraphEditorModel>> availableModelTypes() {
		final List<IPluginExtensionPoint<OpgraphEditorModel>> extPts = 
				PluginManager.getInstance().getExtensionPoints(OpgraphEditorModel.class);
		
		List<Class<? extends OpgraphEditorModel>> retVal = new ArrayList<>();
		for(IPluginExtensionPoint<OpgraphEditorModel> extPt:extPts) {
			retVal.add(extPt.getFactory().createObject().getClass());
		}
		
		return retVal;
	}
	
	public static Map<Class<? extends OpgraphEditorModel>, IPluginExtensionFactory<? extends OpgraphEditorModel>>
		availableFactories() {
		final List<IPluginExtensionPoint<OpgraphEditorModel>> extPts = 
				PluginManager.getInstance().getExtensionPoints(OpgraphEditorModel.class);
		final Map<Class<? extends OpgraphEditorModel>, IPluginExtensionFactory<? extends OpgraphEditorModel>>
			retVal = new HashMap<>();
		
		for(IPluginExtensionPoint<OpgraphEditorModel> extPt:extPts) {
			final IPluginExtensionFactory<OpgraphEditorModel> factory = extPt.getFactory();
			retVal.put(factory.createObject().getClass(), factory);
		}
		
		return retVal;
	}
	
	/**
	 * Create a new {@link OpgraphEditorModel} from the given {@link OpGraph}.
	 * If the {@link OpGraph} includes a a setting for the property
	 * <code>ca.phon.opgraph.editor.modeltype</code> this method will attempt
	 * to create the appropriate model using the registered factory. Otherwise
	 * a {@link DefaultOpgraphEditorModel} is returned.
	 * 
	 * @param graph
	 * @throws ClassNotFoundException
	 */
	public static OpgraphEditorModel fromGraph(OpGraph graph) throws ClassNotFoundException {
		final Map<Class<? extends OpgraphEditorModel>, IPluginExtensionFactory<? extends OpgraphEditorModel>> factoryMap = 
				availableFactories();
		
		final NodeSettings graphSettings = graph.getExtension(NodeSettings.class);
		String modelClassname = DefaultOpgraphEditorModel.class.getName();
		if(graphSettings != null && graphSettings.getSettings().contains(MODEL_TYPE_KEY)) {
			modelClassname = graphSettings.getSettings().getProperty(MODEL_TYPE_KEY, modelClassname);
		}
		Class<?> modelClass = Class.forName(modelClassname);
		if(modelClass != null && modelClass.isAssignableFrom(OpgraphEditorModel.class)) {
			@SuppressWarnings("unchecked")
			Class<? extends OpgraphEditorModel> clazz = (Class<? extends OpgraphEditorModel>)modelClass;
			final IPluginExtensionFactory<? extends OpgraphEditorModel> factory = factoryMap.get(clazz);
			if(factory != null) {
				return factory.createObject(graph);
			} else {
				return new DefaultOpgraphEditorModel(graph);
			}
		} else {
			return new DefaultOpgraphEditorModel(graph);
		}
	}
	
	private Map<String, JComponent> viewMap;
	
	public OpgraphEditorModel() {
		this(new OpGraph());
	}

	public OpgraphEditorModel(OpGraph opgraph) {
		super();
		
		viewMap = new TreeMap<>();
		final JPanel canvasPanel = new JPanel(new BorderLayout());
		canvasPanel.add(getBreadcrumb(), BorderLayout.NORTH);
		canvasPanel.add(getCanvas(), BorderLayout.CENTER);
		viewMap.put("Canvas", canvasPanel);
		viewMap.put("Console", getConsolePanel());
		viewMap.put("Debug", getDebugInfoPanel());
		viewMap.put("Defaults", getNodeDefaults());
		viewMap.put("Library", getNodeLibrary());
		viewMap.put("Settings", getNodeSettings());
	}
	
	/**
	 * Return a list of all available view names.  Custom views
	 * should be added to this list by subclasses.
	 * 
	 * @return list of available view names
	 */
	public List<String> getAvailableViewNames() {
		final List<String> retVal = new ArrayList<>();
		retVal.addAll(viewMap.keySet());
		return retVal;
	}
	
	/**
	 * Get specified view component
	 * 
	 * @param viewName
	 * @return
	 */
	public JComponent getView(String viewName) {
		return viewMap.get(viewName);
	}
	
}
