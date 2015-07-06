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
package ca.phon.app.query.analysis;

import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ca.phon.app.query.analysis.actions.AssessmentEditorAction;
import ca.phon.app.query.analysis.actions.PCCAction;
import ca.phon.app.query.analysis.actions.PMLUAction;
import ca.phon.app.query.analysis.actions.PhoneAccuracyAction;
import ca.phon.app.query.analysis.actions.PhoneInventoryAction;
import ca.phon.app.query.analysis.actions.WordMatchAction;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.PrefHelper;

/**
 * Add canned analysis menu items to windows.
 */
public class AssessmentMenuFilter implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return factory;
	}

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menu) {
		JMenu toolsMenu = null;
		for(int i = 0; i < menu.getMenuCount(); i++) {
			final JMenu m = menu.getMenu(i);
			if(m.getText().equals("Tools")) {
				toolsMenu = m;
				break;
			}
		}
		if(toolsMenu == null) return;
		
		if(!(owner instanceof CommonModuleFrame)) return;
		
		toolsMenu.addSeparator();
		final JMenuItem headerItm = new JMenuItem("-- Assessments --");
		headerItm.setEnabled(false);
		
		toolsMenu.add(headerItm);
		toolsMenu.add(new PhoneInventoryAction((CommonModuleFrame)owner));
		toolsMenu.add(new PhoneAccuracyAction((CommonModuleFrame)owner));
		
		toolsMenu.add(new WordMatchAction((CommonModuleFrame)owner));
		
		toolsMenu.add(new PCCAction((CommonModuleFrame)owner));
		
		toolsMenu.add(new PMLUAction((CommonModuleFrame)owner));
		
		if(PrefHelper.getBoolean("phon.debug", Boolean.FALSE)) {
			toolsMenu.addSeparator();
			toolsMenu.add(new AssessmentEditorAction((CommonModuleFrame)owner));
		}
	}
	
	private final IPluginExtensionFactory<IPluginMenuFilter> factory =  new IPluginExtensionFactory<IPluginMenuFilter>() {
		
		@Override
		public IPluginMenuFilter createObject(Object... args) {
			return AssessmentMenuFilter.this;
		}
		
	};


}
