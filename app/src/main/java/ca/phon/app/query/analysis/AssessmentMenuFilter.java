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
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.query.analysis.actions.AssessmentAction;
import ca.phon.app.query.analysis.actions.AssessmentEditorAction;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.ui.CommonModuleFrame;

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
		int menuIdx = -1;
		for(int i = 0; i < menu.getMenuCount(); i++) {
			final JMenu m = menu.getMenu(i);
			if(m.getText().equals("Query")) {
				menuIdx = i;
				break;
			}
		}
		if(menuIdx < 0) return;
		
		if(!(owner instanceof CommonModuleFrame)) return;

		final JMenu assessmentMenu = new JMenu("Assessments");
		assessmentMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				setupAssessmentMenu((CommonModuleFrame)owner, assessmentMenu);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
				
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
				
			}
		});
		menu.add(assessmentMenu, menuIdx);
	}
	
	private void setupAssessmentMenu(CommonModuleFrame owner, JMenu menu) {
		menu.removeAll();
		
		menu.add(new AssessmentEditorAction(owner));
		menu.addSeparator();

		final JMenu userAssessmentItem = new JMenu("-- User Assessments --");
		userAssessmentItem.setEnabled(false);
		menu.add(userAssessmentItem);
		
		final AssessmentLibrary library = new AssessmentLibrary();
		for(Assessment assessment:library.userAssessments()) {
			menu.add(new AssessmentAction(owner, assessment));
		}
	}
	
	private final IPluginExtensionFactory<IPluginMenuFilter> factory =  new IPluginExtensionFactory<IPluginMenuFilter>() {
		
		@Override
		public IPluginMenuFilter createObject(Object... args) {
			return AssessmentMenuFilter.this;
		}
		
	};

}
