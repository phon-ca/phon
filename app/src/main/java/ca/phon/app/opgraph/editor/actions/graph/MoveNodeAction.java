/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.edits.graph.MoveNodesEdit;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class MoveNodeAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -4862017633502090234L;

	/**
	 * Get a textual representation of the given deltas. More specifically:
	 * <ul>
	 *   <li>if &Delta;x == 0 and &Delta;y > 0, "Up"</li>
	 *   <li>if &Delta;x == 0 and &Delta;y < 0, "Down"</li>
	 *   <li>if &Delta;y == 0 and &Delta;x > 0, "Right"</li>
	 *   <li>if &Delta;y == 0 and &Delta;x < 0, "Left"</li>
	 *   <li>otherwise, ""</li>
	 * </ul>
	 * 
	 * @param xDelta  the x-axis delta
	 * @param yDelta  the y-axis delta
	 * 
	 * @return a textual representation that best represents the action of
	 *         the specified deltas
	 */
	public static String getMoveString(int xDelta, int yDelta) {
		String ret = "";
		if(xDelta == 0 && yDelta > 0)
			ret = "Down";
		else if(xDelta == 0 && yDelta < 0)
			ret = "Up";
		else if(yDelta == 0 && xDelta > 0)
			ret = "Right";
		else if(yDelta == 0 && xDelta < 0)
			ret = "Left";
		return ret;
	}
	
	public static ImageIcon getIcon(int xDelta, int yDelta) {
		String iconName = null;
		switch(getMoveString(xDelta, yDelta)) {
		case "Down":
			iconName = "actions/draw-arrow-down";
			break;
			
		case "Up":
			iconName = "actions/draw-arrow-up";
			break;
			
		case "Right":
			iconName = "actions/draw-arrow-forward";
			break;
			
		case "Left":
			iconName = "actions/draw-arrow-back";
			break;
			
		default:
			break;
		}
		
		if(iconName != null) {
			return IconManager.getInstance().getIcon(iconName, IconSize.SMALL);
		} else {
			return null;
		}
	}

	/**
	 * Get a keystroke for the given deltas. More specifically:
	 * <ul>
	 *   <li>if &Delta;x == 0 and &Delta;y > 0, "Up"</li>
	 *   <li>if &Delta;x == 0 and &Delta;y < 0, "Down"</li>
	 *   <li>if &Delta;y == 0 and &Delta;x > 0, "Right"</li>
	 *   <li>if &Delta;y == 0 and &Delta;x < 0, "Left"</li>
	 *   <li>otherwise, <code>null</code></li>
	 * </ul>
	 * 
	 * @param xDelta  the x-axis delta
	 * @param yDelta  the y-axis delta
	 * 
	 * @return a keystroke for the specified deltas
	 */
	public static KeyStroke getMoveKeystroke(int xDelta, int yDelta) {
		KeyStroke ret = null;
		if(xDelta == 0 && yDelta > 0)
			ret = KeyStroke.getKeyStroke("shift DOWN");
		else if(xDelta == 0 && yDelta < 0)
			ret = KeyStroke.getKeyStroke("shift UP");
		else if(yDelta == 0 && xDelta > 0)
			ret = KeyStroke.getKeyStroke("shift RIGHT");
		else if(yDelta == 0 && xDelta < 0)
			ret = KeyStroke.getKeyStroke("shift LEFT");
		return ret;
	}

	/** The distance along the x-axis to move the node */
	private int deltaX;

	/** The distance along the y-axis to move the node */
	private int deltaY;

	/**
	 * Constructs a move command that moves the current node selection in the
	 * given graph canvas, with this edit posted in the given undo manager.
	 * 
	 * @param deltaX  the x-axis delta
	 * @param deltaY  the y-axis delta
	 */
	public MoveNodeAction(OpgraphEditor editor, int deltaX, int deltaY) {
		super(editor);
		
		this.deltaX = deltaX;
		this.deltaY = deltaY;

		final KeyStroke keystroke = getMoveKeystroke(deltaX, deltaY);
		if(keystroke != null)
			putValue(ACCELERATOR_KEY, keystroke);

		final String suffix = getMoveString(deltaX, deltaY);
		if(suffix.length() == 0)
			putValue(NAME, "Move");
		else
			putValue(NAME, "Move " + suffix);

		putValue(SMALL_ICON, getIcon(deltaX, deltaY));
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final GraphDocument document = getEditor().getModel().getDocument();
		if(document != null) {
			final Collection<OpNode> nodes = document.getSelectionModel().getSelectedNodes();
			document.getUndoSupport().postEdit( new MoveNodesEdit(nodes, deltaX, deltaY) );
		}
	}
}
