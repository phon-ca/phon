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
import ca.phon.app.session.editor.undo.RemoveTierEdit;
import ca.phon.session.*;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RemoveTierAction extends SessionEditorAction {

	private TierDescription td;
	
	private TierViewItem tvi;

	private final static String NAME = "Remove tier";

	private final static ImageIcon ICON =
			IconManager.getInstance().getFontIcon("remove", IconSize.SMALL, UIManager.getColor("Button.foreground"));
	
	public RemoveTierAction(SessionEditor editor, TierDescription td, TierViewItem tvi) {
		super(editor);
		this.td = td;
		this.tvi = tvi;
		
		putValue(NAME, NAME + " " + td.getName());
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final RemoveTierEdit edit = new RemoveTierEdit(getEditor(), td, tvi);
		getEditor().getUndoSupport().postEdit(edit);
	}

}
