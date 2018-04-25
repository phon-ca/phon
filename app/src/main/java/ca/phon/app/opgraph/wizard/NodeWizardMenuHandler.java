/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.wizard;

import java.awt.Window;

import javax.swing.*;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.wizard.actions.WizardSettingsAction;
import ca.phon.opgraph.OpGraph;
import ca.phon.plugin.*;
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
