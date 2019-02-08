package ca.phon.app.query;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.phon.app.log.LogUtil;
import ca.phon.script.PhonScript;

public class ScriptEditorFactory {
	
	/**
	 * Create editor for the given script.
	 * 
	 * @param script
	 * @return new editor
	 */
	public static RSyntaxTextArea createEditorForScript(PhonScript script) {
		RSyntaxTextArea scriptEditor = new RSyntaxTextArea();
		scriptEditor.setText(script.getScript());
		scriptEditor.setColumns(80);
		scriptEditor.setRows(40);
		scriptEditor.setCaretPosition(0);
		scriptEditor.setSyntaxEditingStyle("text/javascript");
		scriptEditor.getDocument().addDocumentListener(new ScriptDocumentListener(script));
	
		return scriptEditor;
	}
	
	/**
	 * Create editor for script including appropriate scroll pane.
	 * 
	 * @param script
	 * @return
	 */
	public static RTextScrollPane createEditorComponentForScript(PhonScript script) {
		return new RTextScrollPane(createEditorForScript(script));
	}
	
	private static class ScriptDocumentListener implements DocumentListener {
		
		private final PhonScript script;
		
		public ScriptDocumentListener(PhonScript script) {
			super();
			this.script = script;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			try {
				String insertedText = e.getDocument().getText(e.getOffset(), e.getLength());
				script.insert(e.getOffset(), insertedText);
			} catch (BadLocationException e1) {
				LogUtil.warning(e1);
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			script.delete(e.getOffset(), e.getOffset()+e.getLength());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			
		}
		
	}

}
