package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Move {@link SessionEditor} to previous record.
 */
public class PreviousRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = 1663717136256443134L;

	private final static String CMD_NAME = "Previous record";
	
	private final static String SHORT_DESC = "Go to previous record";
	
	private final static String ICON = "actions/go-previous";
	
	private final static KeyStroke KS =
			KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_MASK );
	
	public PreviousRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final int newIndex = 
				(getEditor().getCurrentRecordIndex() == 0 ? 0 : getEditor().getCurrentRecordIndex()-1);
		getEditor().setCurrentRecordIndex(newIndex);
	}
	
}
