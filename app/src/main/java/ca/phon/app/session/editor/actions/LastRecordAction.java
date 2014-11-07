package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Move {@link SessionEditor} to last record.
 *
 */
public class LastRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = -5103061811540777442L;
	
	private final static String CMD_NAME = "Last record";
	
	private final static String SHORT_DESC = "Go to last record";
	
	private final static String ICON = "actions/go-last";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK );

	public LastRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getEditor().setCurrentRecordIndex(getEditor().getDataModel().getRecordCount()-1);
	}

}
