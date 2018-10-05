/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;

/**
 * Undo command template.  Requires a {@link UndoManager} be provided.
 *
 */
public class UndoCommand extends AbstractAction {

	private static final long serialVersionUID = 9100941568698565601L;

	private UndoManager undoManager;
	
	public UndoCommand(UndoManager manager) {
		super();
		this.undoManager = manager;
		putValue(NAME, manager.getUndoPresentationName());
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(undoManager.canUndo())
			undoManager.undo();
	}
	
}
