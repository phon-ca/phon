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
import ca.phon.app.session.editor.view.transcript.TranscriptView;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class FindAndReplaceAction extends SessionEditorAction {

	private static final long serialVersionUID = -548370051934852629L;

//	private final static String TXT = "Find & Replace";
	
//	private final static String DESC = "Show Record Data view with Find & Replace UI visible";

//	private final static String ICON_NAME = "find_replace";
	
//	private final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());

	private final boolean showReplace;
	
	public FindAndReplaceAction(SessionEditor editor, boolean showReplace) {
		super(editor);
		this.showReplace = showReplace;
		
		putValue(NAME, (showReplace ? "Find & Replace" : "Find"));
		putValue(SHORT_DESCRIPTION, (showReplace ? "Show Record Data view with Find & Replace UI visible" : "Show Record Data view with Find UI visible"));
		putValue(SMALL_ICON, IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName,
			(showReplace ? "find_replace" : "search"), IconSize.SMALL, Color.darkGray));
		if(showReplace) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		} else {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		}
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		SessionEditor editor = getEditor();
		if(!editor.getViewModel().isShowing(TranscriptView.VIEW_NAME)) {
			editor.getViewModel().showView(TranscriptView.VIEW_NAME);
		}

		TranscriptView transcriptView = (TranscriptView) editor.getViewModel().getView(TranscriptView.VIEW_NAME);
		transcriptView.setFindAndReplaceVisible(true, showReplace);
	}

}
