/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.syllabifier.editor.plugin;

import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import ca.gedge.opgraph.app.commands.core.NewCommand;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.plugin.PhonPlugin;
import ca.phon.syllabifier.editor.SyllabifierEditor;
import ca.phon.ui.action.PhonUIAction;

@PhonPlugin
public class SyllabifierEditorMenuHandler implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return factory;
	}
	
	private final IPluginExtensionFactory<IPluginMenuFilter> factory = new IPluginExtensionFactory<IPluginMenuFilter>() {
		
		@Override
		public IPluginMenuFilter createObject(Object... args) {
			return SyllabifierEditorMenuHandler.this;
		}
	};

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menu) {
		// find tools menu
		JMenu toolsMenu = null;
		for(int i = 0; i < menu.getMenuCount(); i++) {
			if(menu.getMenu(i).getText().equals("Tools")) {
				toolsMenu = menu.getMenu(i);
				break;
			}
		}
		
		if(toolsMenu != null) {
			final PhonUIAction showEditorAct = new PhonUIAction(this, "onShowEditor");
			showEditorAct.putValue(PhonUIAction.NAME, "Syllabifier Editor (opgraph)");
			toolsMenu.add(showEditorAct);
		}
	}
	
	public void onShowEditor() {
		SyllabifierEditor editor = new SyllabifierEditor();
		final NewCommand newCommand = new NewCommand();
		newCommand.actionPerformed(null);
		editor.setDefaultCloseOperation(SyllabifierEditor.DO_NOTHING_ON_CLOSE);
		editor.setSize(new Dimension(1024, 768));
		editor.setVisible(true); 
	}

}
