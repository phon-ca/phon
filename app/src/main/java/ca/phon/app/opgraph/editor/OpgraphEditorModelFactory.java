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

import ca.phon.app.opgraph.macro.MacroOpgraphEditorModel;
import ca.phon.opgraph.OpGraph;
import ca.phon.plugin.*;

import java.util.*;

public class OpgraphEditorModelFactory {
	
	public final static String MODEL_TYPE_KEY = "ca.phon.app.opgraph.editor.modeltype";
	
	/**
	 * Return a list of available models types.  Each model should 
	 * be a subclass of {@link OpgraphEditorModel}.  Each subclass
	 * should implement both the default constructor and the {@link OpGraph}
	 * constructor as well as handle both cases in the {@link IPluginExtensionFactory}
	 * implementation.
	 * 
	 * @return list of available editor model types
	 */
	public List<Class<? extends OpgraphEditorModel>> availableModelTypes() {
		final List<IPluginExtensionPoint<OpgraphEditorModel>> extPts = 
				PluginManager.getInstance().getExtensionPoints(OpgraphEditorModel.class);
		
		List<Class<? extends OpgraphEditorModel>> retVal = new ArrayList<>();
		for(IPluginExtensionPoint<OpgraphEditorModel> extPt:extPts) {
			retVal.add(extPt.getFactory().createObject().getClass());
		}
		
		return retVal;
	}
	
	public Map<Class<? extends OpgraphEditorModel>, IPluginExtensionFactory<? extends OpgraphEditorModel>>
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
	
	public OpgraphEditorModel fromType(Class<? extends OpgraphEditorModel> modelType) {
		final Map<Class<? extends OpgraphEditorModel>, IPluginExtensionFactory<? extends OpgraphEditorModel>> factoryMap = 
				availableFactories();
		final IPluginExtensionFactory<? extends OpgraphEditorModel> factory = factoryMap.get(modelType);
		return (factory != null ? factory.createObject() : null);
	}
	
	/**
	 * Create a new {@link OpgraphEditorModel} from the given {@link OpGraph}.
	 * If the {@link OpGraph} includes a a setting for the property
	 * <code>ca.phon.app.opgraph.editor.modeltype</code> this method will attempt
	 * to create the appropriate model using the registered factory. Otherwise
	 * a {@link MacroOpgraphEditorModel} is returned.
	 * 
	 * @param graph
	 * @throws ClassNotFoundException
	 */
	public OpgraphEditorModel fromGraph(OpGraph graph) throws ClassNotFoundException {
		final Map<Class<? extends OpgraphEditorModel>, IPluginExtensionFactory<? extends OpgraphEditorModel>> factoryMap = 
				availableFactories();
		
		final NodeEditorSettings editorSettings = graph.getExtension(NodeEditorSettings.class);
		String modelClassname = MacroOpgraphEditorModel.class.getName();
		if(editorSettings != null) {
			modelClassname = editorSettings.getModelType();
		}
		Class<?> modelClass = Class.forName(modelClassname);
		if(modelClass != null && OpgraphEditorModel.class.isAssignableFrom(modelClass)) {
			@SuppressWarnings("unchecked")
			Class<? extends OpgraphEditorModel> clazz = (Class<? extends OpgraphEditorModel>)modelClass;
			final IPluginExtensionFactory<? extends OpgraphEditorModel> factory = factoryMap.get(clazz);
			if(factory != null) {
				return factory.createObject(graph);
			} else {
				return new MacroOpgraphEditorModel(graph);
			}
		} else {
			return new MacroOpgraphEditorModel(graph);
		}
	}

}
