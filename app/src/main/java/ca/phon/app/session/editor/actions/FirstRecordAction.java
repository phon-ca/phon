/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ui.nativedialogs.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Move {@link SessionEditor} to first record.
 *
 */
public class FirstRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = 3517301960659867141L;
	
	private final static String CMD_NAME = "First record";
	
	private final static String SHORT_DESC = "Go to first record";
	
	private final static String ICON = "actions/go-first";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_P, 
					(OSInfo.isMacOs() ? KeyEvent.CTRL_MASK : KeyEvent.ALT_MASK) | KeyEvent.SHIFT_MASK);

	public FirstRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getEditor().setCurrentRecordIndex(0);
	}

}
