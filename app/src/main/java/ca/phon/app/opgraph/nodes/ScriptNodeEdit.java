package ca.phon.app.opgraph.nodes;

import javax.swing.undo.*;

public class ScriptNodeEdit extends AbstractUndoableEdit {

	private final ScriptNodeEditor editor;

	private final String prevScript;

	private final String newScript;

	public ScriptNodeEdit(ScriptNodeEditor editor) {
		super();
		this.editor = editor;
		this.prevScript = this.editor.getScriptNode().getScript().getScript();
		this.newScript = this.editor.getText();

		this.editor.updateScript();
	}

	@Override
	public void undo() throws CannotUndoException {
		this.editor.setText(this.prevScript);
		this.editor.updateScript();
	}

	@Override
	public void redo() throws CannotRedoException {
		this.editor.setText(this.newScript);
		this.editor.updateScript();
	}

	@Override
	public String getPresentationName() {
		return "Update script";
	}
}
