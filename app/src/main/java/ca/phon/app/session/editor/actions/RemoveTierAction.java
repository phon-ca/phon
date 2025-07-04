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

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RemoveTierEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.session.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RemoveTierAction extends SessionEditorAction {

	private TierDescription td;
	
	private TierViewItem tvi;

	private final static String TXT = "Remove tier";

	private final static ImageIcon ICON =
			IconManager.getInstance().getFontIcon("remove", IconSize.SMALL, UIManager.getColor("Button.foreground"));
	
	public RemoveTierAction(SessionEditor editor, TierDescription td, TierViewItem tvi) {
		this(editor.getSession(), editor.getEventManager(), editor.getUndoSupport(), td, tvi);
	}

	public RemoveTierAction(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, TierDescription td, TierViewItem tvi) {
		super(session, eventManager, undoSupport);
		this.td = td;
		this.tvi = tvi;

		putValue(NAME, TXT + " " + td.getName());
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setTitle("Remove tier");
		props.setHeader("Remove tier '" + td.getName() + "'");
		props.setMessage("Are you sure you want to remove tier '" + td.getName() + "'?");
		props.setOptions(MessageDialogProperties.yesNoOptions);
		props.setRunAsync(false);
		final int selection = NativeDialogs.showMessageDialog(props);
		if(selection != 0) return;

		final RemoveTierEdit edit = new RemoveTierEdit(getSession(), getEventManager(), td, tvi);
		getUndoSupport().postEdit(edit);
	}

}
