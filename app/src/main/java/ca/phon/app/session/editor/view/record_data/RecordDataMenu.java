/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.view.record_data;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.app.session.editor.view.record_data.actions.*;
import ca.phon.ui.action.*;
import ca.phon.util.icons.*;

/**
 * Menu for the Record Data editor view.
 *
 */
public class RecordDataMenu extends JMenu implements MenuListener {

	private static final long serialVersionUID = -2095672949678390961L;

	private final RecordDataEditorView editor;
	
	public RecordDataMenu(RecordDataEditorView editor) {
		super();
		this.editor = editor;
		addMenuListener(this);
	}

	@Override
	public void menuSelected(MenuEvent e) {
		removeAll();
		
		final PhonUIAction findAndReplaceAct = new PhonUIAction(editor, "onToggleFindAndReplace");
		findAndReplaceAct.putValue(PhonUIAction.NAME, "Find & Replace");
		findAndReplaceAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle Find & Replace UI");
		findAndReplaceAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/edit-find-replace", IconSize.SMALL));
		findAndReplaceAct.putValue(PhonUIAction.SELECTED_KEY, editor.isFindAndReplaceVisible());
		add(new JCheckBoxMenuItem(findAndReplaceAct));
		addSeparator();
		
		// new group action
		final NewGroupCommand newGroupCommand = new NewGroupCommand(editor);
		final JMenuItem newGroupItem = new JMenuItem(newGroupCommand);
		add(newGroupItem);
		
		// merge group action
		final MergeGroupCommand mergeGroupCommand = new MergeGroupCommand(editor);
		final JMenuItem mergeGroupItem = new JMenuItem(mergeGroupCommand);
		add(mergeGroupItem);
		
		// split group action
		final SplitGroupCommand splitGroupCommand = new SplitGroupCommand(editor);
		final JMenuItem splitGroupItem = new JMenuItem(splitGroupCommand);
		
		add(splitGroupItem);
		
		final DeleteGroupCommand delGroupCommand = new DeleteGroupCommand(editor);
		final JMenuItem delGroupItem = new JMenuItem(delGroupCommand);
		add(delGroupItem);
		
		addSeparator();
		add(new MergeAllGroupsCommand(editor));
	}

	@Override
	public void menuDeselected(MenuEvent e) {
	}

	@Override
	public void menuCanceled(MenuEvent e) {
	}
	
}
