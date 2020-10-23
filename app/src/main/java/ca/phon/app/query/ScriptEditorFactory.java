/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.query;

import javax.swing.event.*;
import javax.swing.text.*;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

import ca.phon.app.log.*;
import ca.phon.script.*;

public class ScriptEditorFactory {
	
	/**
	 * Create editor for the given script.
	 * 
	 * @param script
	 * @param editScriptDirectly - if <code>true</code> changes made in editor will automatically
	 *  apply to text of given script object.  If <code>false</code> script object will need to 
	 *  be updated when user manually
	 * @return new editor
	 */
	public static RSyntaxTextArea createEditorForScript(PhonScript script, boolean editScriptDirectly) {
		RSyntaxTextArea scriptEditor = new RSyntaxTextArea();
		scriptEditor.setText(script.getScript());
		scriptEditor.setColumns(80);
		scriptEditor.setRows(40);
		scriptEditor.setCaretPosition(0);
		scriptEditor.setSyntaxEditingStyle("text/javascript");
		if(editScriptDirectly)
			scriptEditor.getDocument().addDocumentListener(new ScriptDocumentListener(script));
	
		return scriptEditor;
	}
	
	/**
	 * Create editor for the given script.
	 * 
	 * @param script
	 * @return new editor
	 */
	public static RSyntaxTextArea createEditorForScript(PhonScript script) {
		return createEditorForScript(script, true);
	}
	
	/**
	 * Create editor for script including appropriate scroll pane.
	 * 
	 * @param script
	 * @return
	 */
	public static RTextScrollPane createEditorComponentForScript(PhonScript script) {
		return createEditorComponentForScript(script, true);
	}
	
	/**
	 * Create editor for script including appropriate scroll pane.
	 * 
	 * @param script
	 * @return
	 */
	public static RTextScrollPane createEditorComponentForScript(PhonScript script, boolean editScriptDirectly) {
		return new RTextScrollPane(createEditorForScript(script), editScriptDirectly);
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
