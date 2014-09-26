package ca.phon.app.session.editor.view.record_data;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ca.phon.app.session.editor.view.record_data.actions.DeleteGroupCommand;
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
	}
	
}
