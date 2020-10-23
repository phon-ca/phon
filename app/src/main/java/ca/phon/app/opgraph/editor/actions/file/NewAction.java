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
package ca.phon.app.opgraph.editor.actions.file;

import java.awt.event.*;
import java.io.*;

import ca.phon.app.log.*;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.*;
import ca.phon.app.opgraph.editor.actions.*;
import ca.phon.opgraph.*;
import ca.phon.ui.nativedialogs.*;

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
		OpGraph templateGraph = new OpGraph();
		try {
			templateGraph = instantiator.defaultTemplate();
		} catch (IOException e) {
			LogUtil.warning(e);
		}
		final OpgraphEditorModel model = instantiator.createModel(templateGraph);
		
		if(useCurrentWindow) {
			getEditor().setModel(model);
		//	SwingUtilities.invokeLater(() -> (new AutoLayoutAction(getEditor())).actionPerformed(arg0));
		} else {
			final OpgraphEditor editor = new OpgraphEditor(model);
			editor.pack();
			editor.setSize(1064, 768);
			editor.setLocationByPlatform(true);
			editor.setVisible(true);
		//	SwingUtilities.invokeLater(() -> (new AutoLayoutAction(editor)).actionPerformed(arg0));
		}
	}

}
