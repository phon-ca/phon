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
package ca.phon.ui.menu;

import ca.phon.plugin.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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
