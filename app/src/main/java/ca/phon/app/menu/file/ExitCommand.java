/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.menu.file;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.*;

import ca.phon.plugin.PluginAction;
import ca.phon.util.OSInfo;

/**
 * Command for exiting the application.
 */
public class ExitCommand extends PluginAction {

	private static final long serialVersionUID = -2743379216250130058L;
	
	private final static String EP = "Exit";
	
	public ExitCommand() {
		super(EP);
		putValue(Action.NAME, "Exit");
		putValue(Action.SHORT_DESCRIPTION, "Exit the application.");
		if(!OSInfo.isMacOs())
			putValue(Action.ACCELERATOR_KEY, 
					KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

}
