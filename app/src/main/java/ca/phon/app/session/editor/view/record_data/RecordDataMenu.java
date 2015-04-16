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
package ca.phon.app.session.editor.view.record_data;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ca.phon.app.session.editor.view.record_data.actions.DeleteGroupCommand;
import ca.phon.app.session.editor.view.record_data.actions.MergeAllGroupsCommand;
import ca.phon.app.session.editor.view.record_data.actions.MergeGroupCommand;
import ca.phon.app.session.editor.view.record_data.actions.NewGroupCommand;
import ca.phon.app.session.editor.view.record_data.actions.SplitGroupCommand;

/**
 * Menu for the Record Data editor view.
 *
 */
public class RecordDataMenu extends JMenu {

	private static final long serialVersionUID = -2095672949678390961L;

	private final RecordDataEditorView editor;
	
	public RecordDataMenu(RecordDataEditorView editor) {
		super();
		this.editor = editor;
		init();
	}
	
	private void init() {
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
	
}
