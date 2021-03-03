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
package ca.phon.app.menu.workspace;

import ca.phon.plugin.PluginAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Opens the workspace window.
 */
public class WorkspaceCommand extends PluginAction {

	private static final long serialVersionUID = -8057848271906537053L;

	private final static String EP = "Workspace";
	
	public WorkspaceCommand() {
		super(EP);
		putValue(Action.NAME, "Show Workspace window");
		putValue(Action.SHORT_DESCRIPTION, "Open the workspace dialog.");
		putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_W, 
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_MASK));
	}
	
}
