package ca.phon.app.session.editor;

import java.util.*;

public interface EditorViewListener extends EventListener {

	public void onOpened(EditorView view);
	
	public void onClosed(EditorView view);
	
	public void onFocused(EditorView view);
	
}
