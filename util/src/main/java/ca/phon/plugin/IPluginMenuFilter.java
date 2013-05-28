/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.plugin;

import java.awt.Window;

import javax.swing.JMenuBar;

/**
 * A plugin extension point for controlling the window menu
 * for all Phon windows.  The provided filter will be called
 * to generate the appropriate window menu entries as needed
 * by the plugin.
 * 
 * 
 */
public interface IPluginMenuFilter {

	/**
	 * Add/remove/edit the window menu
	 * as needed by the implementing plugin.
	 * 
	 * @param owner the window which owns the menu
	 *  (may be null)
	 * @param menu the menu in question
	 */
	public void filterWindowMenu(Window owner, JMenuBar menu);
	
}
