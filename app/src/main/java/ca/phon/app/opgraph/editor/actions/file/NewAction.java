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
package ca.phon.app.opgraph.editor.actions.file;

import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.EditorModelInstantiatorMenuInfo;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.app.opgraph.editor.actions.graph.AutoLayoutAction;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

/**
 * Show new graph dialog for the node editor
 */
public class NewAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -6534242210546640918L;
	
	public final static String TXT = "New...";
	
	public final static String DESC = "New graph";
	
	private final EditorModelInstantiator instantiator; 
	
	public NewAction(OpgraphEditor editor, EditorModelInstantiator instantiator) {
		super(editor);
		
		final EditorModelInstantiatorMenuInfo menuInfo =
				instantiator.getClass().getAnnotation(EditorModelInstantiatorMenuInfo.class);
		
		putValue(NAME, (menuInfo != null ? menuInfo.name() : TXT));
		putValue(SHORT_DESCRIPTION, (menuInfo != null ? menuInfo.tooltip() : DESC));
		
		this.instantiator = instantiator;
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		boolean useCurrentWindow = true;
		if(getEditor().getCurrentFile() != null
				|| getEditor().hasUnsavedChanges()) {
			// ask to use new window
			final String opts[] = new String[] { "Use current window", "Use new window" };
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getEditor());
			props.setOptions(opts);
			props.setTitle("Choose window");
			props.setHeader(props.getTitle());
			props.setMessage("Use current window for new document?");
			props.setRunAsync(false);
			
			final int ret = NativeDialogs.showMessageDialog(props);
			useCurrentWindow = (ret == 0);
		}
		
		if(useCurrentWindow && getEditor().hasUnsavedChanges()) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getEditor());
			props.setOptions(MessageDialogProperties.yesNoCancelOptions);
			props.setTitle("Save changes");
			props.setHeader(props.getTitle());
			props.setMessage("Save changes before starting a new graph?");
			props.setRunAsync(false);
			
			final int ret = NativeDialogs.showMessageDialog(props);
			if(ret == 0) /* yes */ {
				(new SaveAction(getEditor())).actionPerformed(arg0);
			} else if(ret == 1) /* no */ {
				// do nothing
			} else /* cancel */ {
				return;
			}
		}
		
		// create new model
		final OpgraphEditorModel model = instantiator.createModel(new OpGraph());
		
		if(useCurrentWindow) {
			getEditor().setModel(model);
			SwingUtilities.invokeLater(() -> (new AutoLayoutAction(getEditor())).actionPerformed(arg0));
		} else {
			final OpgraphEditor editor = new OpgraphEditor(model);
			editor.pack();
			editor.setSize(1064, 768);
			editor.setLocationByPlatform(true);
			editor.setVisible(true);
			SwingUtilities.invokeLater(() -> (new AutoLayoutAction(editor)).actionPerformed(arg0));
		}
	}

}
