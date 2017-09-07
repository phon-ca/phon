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

import java.awt.event.*;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.util.icons.*;

/**
 * Move {@link SessionEditor} to next record.
 */
public class NextRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = 1663717136256443134L;

	private final static String CMD_NAME = "Next record";
	
	private final static String SHORT_DESC = "Go to next record";
	
	private final static String ICON = "actions/go-next";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
					
	public NextRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final int newIndex = 
				(getEditor().getCurrentRecordIndex() == getEditor().getDataModel().getRecordCount()-1 ? 
						getEditor().getCurrentRecordIndex() : getEditor().getCurrentRecordIndex()+1);
		getEditor().setCurrentRecordIndex(newIndex);
	}
	
}
