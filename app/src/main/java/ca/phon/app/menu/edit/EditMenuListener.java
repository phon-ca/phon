/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.menu.edit;

import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.undo.UndoManager;

import ca.phon.extensions.IExtendable;

public class EditMenuListener implements MenuListener {
	
	private final Window owner;
	
	public EditMenuListener(Window window) {
		super();
		this.owner = window;
	}

	@Override
	public void menuSelected(MenuEvent e) {
		final JMenu editMenu = (JMenu)e.getSource();
		if(editMenu == null) return;
		editMenu.removeAll();
		
		// undo/redo
		if(owner instanceof IExtendable) {
			final IExtendable extWindow = (IExtendable)owner;
			
			final UndoManager manager = extWindow.getExtension(UndoManager.class);
			
			if(manager != null) {
				final UndoCommand undoCommand = new UndoCommand(manager);
				final JMenuItem undoItem = new JMenuItem(undoCommand);
				editMenu.add(undoItem);
				
				final RedoCommand redoCommand = new RedoCommand(manager);
				final JMenuItem redoItem = new JMenuItem(redoCommand);
				editMenu.add(redoItem);
				
				editMenu.addSeparator();
			}
			
		}
		
		// cut 
		final JMenuItem cutItem = new JMenuItem(new CutCommand());
		editMenu.add(cutItem);
		
		// copy 
		final JMenuItem copyItem = new JMenuItem(new CopyCommand());
		editMenu.add(copyItem);
		
		// paste
		final JMenuItem pasteItem = new JMenuItem(new PasteCommand());
		editMenu.add(pasteItem);
		
		editMenu.addSeparator();
		
		// prefs
		final JMenuItem prefsItem = new JMenuItem(new PreferencesCommand());
		editMenu.add(prefsItem);
	}

	@Override
	public void menuDeselected(MenuEvent e) {
	}

	@Override
	public void menuCanceled(MenuEvent e) {
	}

}
