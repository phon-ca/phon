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

import java.awt.Window;

import javax.swing.JMenuBar;

import ca.phon.app.opgraph.editor.actions.OpenComposerAction;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.ui.menu.MenuBuilder;

public class NodeEditorMenuHandler implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	public NodeEditorMenuHandler() {
	}

	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return (args) -> this;
	}

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menuBar) {
		final MenuBuilder builder = new MenuBuilder(menuBar);
		
		builder.addSeparator("./Tools",	"composer");
		builder.addItem("./Tools@composer", new OpenComposerAction());
	}

}
