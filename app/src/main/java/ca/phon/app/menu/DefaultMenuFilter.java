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
package ca.phon.app.menu;

import java.awt.Window;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.menu.edit.EditMenuListener;
import ca.phon.app.menu.file.ExitCommand;
import ca.phon.app.menu.file.WorkspaceCommand;
import ca.phon.app.menu.file.WorkspaceMenuListener;
import ca.phon.app.menu.help.HelpCommand;
import ca.phon.app.menu.help.LogCommand;
import ca.phon.app.menu.query.QueryMenuListener;
import ca.phon.app.menu.tools.BasicSyllabifierTestCommand;
import ca.phon.app.menu.tools.IpaMapCommand;
import ca.phon.app.menu.tools.LanguageCodesCommand;
import ca.phon.app.menu.window.OpenWindowsMenuListener;
import ca.phon.app.prefs.PhonProperties;
import ca.phon.app.workspace.WorkspaceDialog;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.PrefHelper;

/**
 * Create the default menu for all Phon windows.
 * 
 */
public class DefaultMenuFilter implements IPluginMenuFilter {

	private final static Logger LOGGER = Logger
			.getLogger(DefaultMenuFilter.class.getName());
	
	@Override
	public void filterWindowMenu(Window owner, JMenuBar menu) {
		addFileMenu(owner, menu);
		addEditMenu(owner, menu);
		addWorkspaceMenu(owner, menu);
		addQueryMenu(owner, menu);
		addToolsMenu(owner, menu);
		addPluginMenu(owner, menu);
		addWindowMenu(owner, menu);
		addHelpMenu(owner, menu);
	}
	
	/**
	 * Add 'File' menu
	 */
	protected void addFileMenu(Window owner, JMenuBar menu) {
		JMenu fileMenu = new JMenu("File");
		
		final JMenuItem exitItem = new JMenuItem(new ExitCommand());
		fileMenu.add(exitItem);
		
		menu.add(fileMenu);
	}

	/**
	 * Add 'Edit' menu
	 * 
	 */
	protected void addEditMenu(Window owner, JMenuBar menu) {
		JMenu editMenu = new JMenu("Edit");
		
		final MenuListener editListener = new EditMenuListener(owner);
		editMenu.addMenuListener(editListener);
		
		final MenuEvent me = new MenuEvent(editMenu);
		editListener.menuSelected(me);
		
		menu.add(editMenu);
	}
	
	/**
	 * Add 'Query' menu
	 */
	protected void addQueryMenu(Window owner, JMenuBar menu) {
		if(!(owner instanceof CommonModuleFrame)) return;
		final CommonModuleFrame frame = (CommonModuleFrame)owner;
		if(!(frame instanceof WorkspaceDialog)) {
			final JMenu queryMenu  = new JMenu("Query");
			queryMenu.addMenuListener(queryMenuListener);
			
			final MenuEvent me = new MenuEvent(queryMenu);
			queryMenuListener.menuSelected(me);
			
			menu.add(queryMenu);
		}
	}
	
	/**
	 * Add 'Tools' menu
	 */
	protected void addToolsMenu(Window owner, JMenuBar menu) {
		JMenu toolsMenu = new JMenu("Tools");
		
		// ipa chart
		final JMenuItem ipaItem = new JMenuItem(new IpaMapCommand());
		toolsMenu.add(ipaItem);
		
		toolsMenu.addSeparator();
		
		// language codes
		final JMenuItem langItem = new JMenuItem(new LanguageCodesCommand());
		toolsMenu.add(langItem);
		
		if(PrefHelper.getBoolean(PhonProperties.DEBUG, false)) {
			toolsMenu.add(new BasicSyllabifierTestCommand());
		}
		
		menu.add(toolsMenu);
	}
	
	/**
	 * Add 'Window' menu
	 */
	protected void addWindowMenu(Window owner, JMenuBar menu) {
		JMenu windowMenu = new JMenu("Window");
		final MenuListener openWindowMenuListener = new OpenWindowsMenuListener(owner);
		windowMenu.addMenuListener(openWindowMenuListener);
		
		final MenuEvent me = new MenuEvent(windowMenu);
		openWindowMenuListener.menuSelected(me);
		
		// keep on tops
		menu.add(windowMenu);
	}
	
	/**
	 * Add 'Plugin' menu
	 */
	protected void addPluginMenu(Window owner, JMenuBar menu) {
		JMenu pluginMenu = new JMenu("Plugins");
		menu.add(pluginMenu);
	}
	
	/**
	 * Add 'Help' menu
	 */
	protected void addHelpMenu(Window owner, JMenuBar menu) {
		JMenu helpMenu = new JMenu("Help");
		
		// log
		final JMenuItem logItem = new JMenuItem(new LogCommand());
		helpMenu.add(logItem);
		
		helpMenu.addSeparator();
		
		// about
		final JMenuItem aboutItem = new JMenuItem(new HelpCommand());
		helpMenu.add(aboutItem);
		
		menu.add(helpMenu);
	}
	
	/**
	 * Add 'Workspace' menu
	 */
	protected void addWorkspaceMenu(Window owner, JMenuBar menu) {
		JMenu workspaceMenu = new JMenu("Workspace");
		
		workspaceMenu.add(new WorkspaceCommand());
		
		final JMenu workspaceProjectsMenu = new JMenu("Workspace projects");
		workspaceProjectsMenu.addMenuListener(new WorkspaceMenuListener());
		workspaceMenu.add(workspaceProjectsMenu);
		
		menu.add(workspaceMenu);
	}
	
	/**
	 * Query menu generator
	 */
	private final MenuListener queryMenuListener = new QueryMenuListener();
	
}
