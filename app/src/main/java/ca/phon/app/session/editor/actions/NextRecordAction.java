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
package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ui.KeyStrokeUtil;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Move {@link SessionEditor} to next record.
 */
public class NextRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = 1663717136256443134L;

	private final static String CMD_NAME = "Next record";
	
	private final static String SHORT_DESC = "Go to next record";
	
	private final static KeyStroke KS =
			KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
					
	public NextRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC + " (" + KeyStrokeUtil.keyStrokeToString(KS) + ")");
		final ImageIcon nextIcon = IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName, "chevron_right", IconSize.MEDIUM, UIManager.getColor("Button.foreground"));
		putValue(SMALL_ICON, nextIcon);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final int newIndex = 
				(getEditor().getCurrentRecordIndex() == getEditor().getDataModel().getRecordCount()-1 ? 
						getEditor().getCurrentRecordIndex() : getEditor().getCurrentRecordIndex()+1);
        if(newIndex < 0 || newIndex >= getEditor().getSession().getRecordCount()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
		getEditor().setCurrentRecordIndex(newIndex);
	}
	
}
