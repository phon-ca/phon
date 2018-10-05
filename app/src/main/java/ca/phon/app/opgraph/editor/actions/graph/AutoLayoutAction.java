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
package ca.phon.app.opgraph.editor.actions.graph;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.app.AutoLayoutManager;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class AutoLayoutAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -2540261955364824184L;
	
	public final static String TXT = "Layout nodes";
	
	public final static String DESC = "Automatically layout nodes in graph";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_L, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/distribute-horizontal-margin", IconSize.SMALL);

	public AutoLayoutAction(OpgraphEditor editor) {
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
			final AutoLayoutManager layoutManager = new AutoLayoutManager();
			final JComponent canvasView = getEditor().getModel().getView("Canvas");
			layoutManager.setPreferredWidth(canvasView.getSize().width);
			layoutManager.layoutGraph(document.getGraph());
			document.getUndoSupport().postEdit(layoutManager.getUndoableEdit());
		}
	}

}
