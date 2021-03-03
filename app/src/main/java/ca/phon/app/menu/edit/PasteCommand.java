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

import ca.phon.plugin.PluginAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * 
 */
public class PasteCommand extends PluginAction {

	private static final long serialVersionUID = -3389448241139697134L;
	
	private final static String EP = "Paste";
	
	public PasteCommand() {
		super(EP);
		putValue(Action.NAME, "Paste");
		putValue(Action.SHORT_DESCRIPTION, "Edit: paste");
		putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
	}
	
}
