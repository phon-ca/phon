/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.editor.actions.debug;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class StopAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -3449043672596585549L;

	public static final String TXT = "Stop";
	
	public static final String DESC = "Stop debugger";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/media-playback-stop-7", IconSize.SMALL);
	
	public StopAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		if(document != null) {
			document.setProcessingContext(null);
			document.updateDebugState(null);
			getEditor().getModel().getCanvas().updateDebugState(null);
		}
	}

}
