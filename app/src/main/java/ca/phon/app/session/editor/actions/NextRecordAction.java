package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Move {@link SessionEditor} to next record.
 */
public class NextRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = 1663717136256443134L;

	private final static String CMD_NAME = "Next record";
	
	private final static String SHORT_DESC = "Go to next record";
	
	private final static String ICON = "actions/go-next";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
	
	public NextRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final int newIndex = 
				(getEditor().getCurrentRecordIndex() == getEditor().getDataModel().getRecordCount()-1 ? 
						getEditor().getCurrentRecordIndex() : getEditor().getCurrentRecordIndex()+1);
		getEditor().setCurrentRecordIndex(newIndex);
	}
	
}
