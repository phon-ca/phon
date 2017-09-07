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

import java.awt.Toolkit;
import java.awt.event.*;

import javax.swing.*;

import ca.gedge.opgraph.app.*;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.*;

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
