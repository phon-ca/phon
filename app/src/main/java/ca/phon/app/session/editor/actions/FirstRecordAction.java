package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Move {@link SessionEditor} to first record.
 *
 */
public class FirstRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = 3517301960659867141L;
	
	private final static String CMD_NAME = "First record";
	
	private final static String SHORT_DESC = "Go to first record";
	
	private final static String ICON = "actions/go-first";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK);

	public FirstRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getEditor().setCurrentRecordIndex(0);
	}

}
