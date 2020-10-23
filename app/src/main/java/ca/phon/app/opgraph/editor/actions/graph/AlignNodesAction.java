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
package ca.phon.app.opgraph.editor.actions.graph;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.editor.actions.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.edits.graph.*;
import ca.phon.util.icons.*;

public class AlignNodesAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 7052293033950635497L;

	private final int side;
	
	public AlignNodesAction(OpgraphEditor editor, int side) {
		super(editor);
		
		this.side = side;
		
		putValue(NAME, getName());
		putValue(ACCELERATOR_KEY, getKeystroke());
		putValue(SMALL_ICON, getIcon());
	}
	
	public KeyStroke getKeystroke() {
		int key = 0;
		switch(side) {
		case SwingConstants.TOP:
			key = KeyEvent.VK_UP;
			break;
			
		case SwingConstants.BOTTOM:
			key = KeyEvent.VK_DOWN;
			break;
			
		case SwingConstants.LEFT:
			key = KeyEvent.VK_LEFT;
			break;
			
		case SwingConstants.RIGHT:
			key = KeyEvent.VK_RIGHT;
			break;
		}
		return KeyStroke.getKeyStroke(key, KeyEvent.ALT_MASK);
	}
	
	public ImageIcon getIcon() {
		ImageIcon retVal = null;
		switch(side) {
		case SwingConstants.TOP:
			retVal = IconManager.getInstance().getIcon("actions/align-vertical-top-2", IconSize.SMALL);
			break;
			
		case SwingConstants.BOTTOM:
			retVal = IconManager.getInstance().getIcon("actions/align-vertical-bottom-2", IconSize.SMALL);
			break;
			
		case SwingConstants.LEFT:
			retVal = IconManager.getInstance().getIcon("actions/align-horizontal-left", IconSize.SMALL);
			break;
			
		case SwingConstants.RIGHT:
			retVal = IconManager.getInstance().getIcon("actions/align-horizontal-right-2", IconSize.SMALL);
			break;
		}
		return retVal;
	}
	
	public String getName() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Align ");
		switch(side) {
		case SwingConstants.TOP:
			sb.append("top");
			break;
			
		case SwingConstants.BOTTOM:
			sb.append("bottom");
			break;
			
		case SwingConstants.LEFT:
			sb.append("left");
			break;
			
		case SwingConstants.RIGHT:
			sb.append("right");
			break;
			
		default:
			break;
		}
		return sb.toString();
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		if(document != null) {
			final Collection<OpNode> selectedNodes = document.getSelectionModel().getSelectedNodes();
			if(selectedNodes.size() > 1) {
				document.getUndoSupport().postEdit(new AlignNodesEdit(selectedNodes, side));
			}
		}
	}

}
