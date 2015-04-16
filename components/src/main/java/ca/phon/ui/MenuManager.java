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
package ca.phon.ui;

import java.awt.Window;
import java.util.List;

import javax.swing.JMenuBar;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.plugin.PluginManager;

/**
 * Helper class to build menus based on plugin
 * extensions.  Plugins can alter the window
 * menus in Phon by implementing an extension point
 * for the interface IPluginMenuFilter.
 * 
 * While plugins can alter any part of the menu,
 * it is recommended to only add entries to the 'Plugins'
 * menu unless absolutely necessary.
 */
public class MenuManager {
	
	/**
	 * Create the menu
	 * 
	 * @param owner
	 */
	public static JMenuBar createWindowMenuBar(Window owner) {
		JMenuBar menuBar = new JMenuBar();
		
		// get plugin extension points
		List<IPluginExtensionPoint<IPluginMenuFilter>> menuFilterExtPts =
			PluginManager.getInstance().getExtensionPoints(IPluginMenuFilter.class);
		for(IPluginExtensionPoint<IPluginMenuFilter> menuFilterExtPt:menuFilterExtPts) {
			
			// get the factory and create the filter object
			IPluginExtensionFactory<IPluginMenuFilter> menuFilterFactory = 
				menuFilterExtPt.getFactory();
			IPluginMenuFilter filter = menuFilterFactory.createObject();
			
			filter.filterWindowMenu(owner, menuBar);
		}
		
		return menuBar;
	}
	
}
