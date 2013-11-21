package ca.phon.app.session.editor.undo;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;

public class RemoveTierEdit extends AddTierEdit {

	private static final long serialVersionUID = 5829729907299422281L;

	public RemoveTierEdit(SessionEditor editor, TierDescription tierDesc,
			TierViewItem tvi) {
		super(editor, tierDesc, tvi);
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo delete tier " + tierDescription.getName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo delete tier " + tierDescription.getName();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.doIt();
	}

	@Override
	public void doIt() {
		super.undo();
	}
	
}
