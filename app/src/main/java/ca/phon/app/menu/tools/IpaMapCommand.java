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
package ca.phon.app.menu.tools;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ca.phon.ipamap.*;
import ca.phon.plugin.*;

/**
 * Toggle visiblity of the {@link IpaMap}
 *
 */
public class IpaMapCommand extends PluginAction {
	
	private static final long serialVersionUID = -4547303616567952540L;

	public IpaMapCommand() {
		super(IPAMapEP.EP_NAME);
		putValue(Action.NAME, "IPA Map");
		putValue(Action.SHORT_DESCRIPTION, "IPA Map");
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
	}
	
}
