package ca.phon.app.session.editor.media;

import javax.swing.ImageIcon;
import javax.swing.JMenu;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.SessionEditor;

public class SegmentationEditorView extends EditorView {

	private static final long serialVersionUID = -3058669055923770822L;

	private final static String VIEW_NAME = "Segmentation";
	
	public SegmentationEditorView(SessionEditor editor) {
		super(editor);
	}

	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

}
