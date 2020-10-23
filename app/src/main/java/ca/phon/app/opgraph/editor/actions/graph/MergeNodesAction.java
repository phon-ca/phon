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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.editor.actions.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.components.canvas.*;
import ca.phon.opgraph.nodes.menu.edits.*;
import ca.phon.util.icons.*;

/**
 * Create a new macro node from selection
 *
 */
public class MergeNodesAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -3332016622776612070L;
	
	private final static String TXT = "Merge nodes";
	
	private final static String DESC = "Merge selected nodes into new macro node";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_MASK);
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/format-join-node", IconSize.SMALL);

	public MergeNodesAction(OpgraphEditor editor) {
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
			final GraphCanvasSelectionModel selectionModel = document.getSelectionModel();
			final Collection<OpNode> selectedNodes = selectionModel.getSelectedNodes();
			document.getUndoSupport().postEdit(new CreateMacroEdit(document.getGraph(), selectedNodes));
		}
	}

}
