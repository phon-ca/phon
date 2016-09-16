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
package ca.phon.app.opgraph.editor.actions.view;

import java.awt.event.ActionEvent;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;

public class ToggleViewAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 2681118382414781020L;

	public final static String DESC = "Toggle view";
	
	public ToggleViewAction(OpgraphEditor editor, String viewName) {
		super(editor);
		
		putValue(NAME, viewName);
		putValue(SHORT_DESCRIPTION, DESC + " " + viewName);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final String viewName = (String)getValue(NAME);
		if(getEditor().isViewVisible(viewName)) {
			getEditor().hideView(viewName);
		} else {
			getEditor().showView(viewName);
		}
	}

}
