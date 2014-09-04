package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import ca.phon.app.session.RecordTransferable;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.session.Record;

/**
 * Paste record from system clipboard.
 */
public class PasteRecordAction extends SessionEditorAction {
	
	private final static Logger LOGGER = Logger
			.getLogger(PasteRecordAction.class.getName());

	private static final long serialVersionUID = 4581031841588720169L;

	private final static String CMD_NAME = "Paste record";
	
	private final static String SHORT_DESC = "Paste record from clipboard after current record";
	
	private final static String ICON = "";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_V,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);
	
	public PasteRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e1) {
		final Transferable clipboardContents = 
				Toolkit.getDefaultToolkit().getSystemClipboard().getContents(getEditor());
		if(clipboardContents == null) return;
		Object obj = null;
		try {
			obj = clipboardContents.getTransferData(RecordTransferable.FLAVOR);
		} catch (UnsupportedFlavorException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		final RecordTransferable recTrans = (obj != null ? (RecordTransferable)obj : null);
		if(recTrans != null) {
			final Record record = recTrans.getRecord();
			
			final int index = getEditor().getCurrentRecordIndex() + 1;
			
			final AddRecordEdit edit = new AddRecordEdit(getEditor(), record, index);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}
