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
package ca.phon.app.opgraph.wizard;

import java.awt.Window;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.wizard.actions.WizardSettingsAction;
import ca.phon.opgraph.OpGraph;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.ui.menu.MenuBuilder;

public class NodeWizardMenuHandler implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		final IPluginExtensionFactory<IPluginMenuFilter> retVal = (args) -> {
			return NodeWizardMenuHandler.this;
		};
		return retVal;
	}

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menu) {
		if(!(owner instanceof OpgraphEditor)) return;
		final OpgraphEditor editor = (OpgraphEditor)owner;
		if(editor.getModel() == null || editor.getModel().getDocument() == null) return;
		final OpGraph graph = editor.getModel().getDocument().getGraph();
		final WizardExtension ext = graph.getExtension(WizardExtension.class);
		if(ext != null) {
			final MenuBuilder builder = new MenuBuilder(menu);
			final WizardSettingsAction act = new WizardSettingsAction(editor);
			builder.addMenu(".@Edit", "Wizard");
			builder.addItem("Wizard", new JMenuItem(act));
		}
	}

	
	
}
