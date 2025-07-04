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
package ca.phon.app.menu.edit;

import ca.phon.extensions.IExtendable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.lang.ref.WeakReference;

public class EditMenuListener implements MenuListener {
	
	private final WeakReference<Window> owner;
	
	public EditMenuListener(Window window) {
		super();
		this.owner = new WeakReference<Window>(window);
	}

	@Override
	public void menuSelected(MenuEvent e) {
		final JMenu editMenu = (JMenu)e.getSource();
		if(editMenu == null) return;
		editMenu.removeAll();
		
		// undo/redo
		final Window owner = this.owner.get();
		if(owner != null && owner instanceof IExtendable) {
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
		
		if(owner != null && owner instanceof IExtendable) {
			final IExtendable extWindow = (IExtendable)owner;
			final EditMenuModifier modifier = (EditMenuModifier)extWindow.getExtension(EditMenuModifier.class);
			if(modifier != null) {
				modifier.modfiyEditMenu(editMenu);
			}
		}
	}

	@Override
	public void menuDeselected(MenuEvent e) {
	}

	@Override
	public void menuCanceled(MenuEvent e) {
	}

}
