package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * <p>Save the {@link Session} currently open in the {@link SessionEditor}.</p>
 */
public class SaveSessionAction extends SessionEditorAction {
	
	private static final long serialVersionUID = 1815240897720486382L;

	private final static Logger LOGGER = Logger
			.getLogger(SaveSessionAction.class.getName());
	
	private final static String CMD_NAME = "Save";
	
	private final static String SHORT_DESC = "Save session";
	
	private final static String ICON = "actions/filesave";

	private final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

	public SaveSessionAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		try {
			getEditor().saveData();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}
