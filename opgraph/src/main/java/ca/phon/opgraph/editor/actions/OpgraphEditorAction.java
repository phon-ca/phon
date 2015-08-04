package ca.phon.opgraph.editor.actions;

import java.lang.ref.WeakReference;

import ca.gedge.opgraph.app.commands.HookableCommand;
import ca.phon.opgraph.editor.OpgraphEditor;

public abstract class OpgraphEditorAction extends HookableCommand {
	
	private static final long serialVersionUID = 2331592911456671778L;

	private WeakReference<OpgraphEditor> editorRef;
	
	public OpgraphEditorAction(OpgraphEditor editor) {
		super();
		
		this.editorRef = new WeakReference<OpgraphEditor>(editor);
	}

	public OpgraphEditor getEditor() {
		return this.editorRef.get();
	}
	
}
