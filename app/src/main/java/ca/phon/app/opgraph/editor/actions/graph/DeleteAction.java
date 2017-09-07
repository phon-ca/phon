/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.opgraph.editor.actions.graph;

import java.awt.event.*;
import java.util.Collection;

import javax.swing.*;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.edits.graph.DeleteNodesEdit;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.*;

public class DeleteAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -2540261955364824184L;
	
	public final static String TXT = "Delete nodes";
	
	public final static String DESC = "Delete selected nodes";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/format-remove-node", IconSize.SMALL);

	public DeleteAction(OpgraphEditor editor) {
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
			final Collection<OpNode> nodes = document.getSelectionModel().getSelectedNodes();
			document.getUndoSupport().postEdit(new DeleteNodesEdit(document.getGraph(), nodes));
		}
	}

}
