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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class StepIntoAction extends OpgraphDebugAction {

	private static final long serialVersionUID = -8824991521914808262L;

	public final static String TXT = "Step into";
	
	public final static String DESC = "Step into subgraph";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/debug-step-into", IconSize.SMALL);
	
	public StepIntoAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		final Runnable inBg = () -> {
			if(document != null) {
				final Processor context = getProcessor(document);
				if(context.hasNext()) {
					context.stepInto();
					
					SwingUtilities.invokeLater( () -> {
						document.updateDebugState(context);
						getEditor().getModel().getCanvas().updateDebugState(context);
					});
				}
			}
		};
		getOpgraphThread().invokeLater(inBg);
	}

}
