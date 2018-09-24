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
package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * <p>Save the {@link Session} currently open in the {@link SessionEditor}.</p>
 */
public class SaveSessionAction extends SessionEditorAction {
	
	private static final long serialVersionUID = 1815240897720486382L;

	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SaveSessionAction.class.getName());
	
	private final static String CMD_NAME = "Save";
	
	private final static String SHORT_DESC = "Save session";
	
	private final static String ICON = "actions/filesave";

	private final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

	public SaveSessionAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		try {
			getEditor().saveData();
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

}
