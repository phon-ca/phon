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
package ca.phon.app.menu.window;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import ca.phon.ui.CommonModuleFrame;

/**
 * Close the specified window, prompting a save dialog
 * if necessary.
 *
 */
public class CloseWindowCommand extends AbstractAction {

	private final Window window;
	
	public CloseWindowCommand(Window window) {
		super();
		this.window = window;
		putValue(NAME, "Close");
		putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(window instanceof CommonModuleFrame){
			((CommonModuleFrame)window).close();
		} else {
			window.setVisible(false);
		}
	}
	
}
