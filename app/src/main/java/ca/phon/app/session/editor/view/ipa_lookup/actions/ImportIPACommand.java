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
package ca.phon.app.session.editor.view.ipa_lookup.actions;

import ca.phon.app.session.editor.view.ipa_lookup.IPALookupView;
import ca.phon.ui.nativedialogs.*;

import java.awt.event.ActionEvent;
import java.util.List;

public class ImportIPACommand extends IPALookupViewAction {
	
	private final static String CMD_NAME = "Import IPA";
	
	private final static String SHORT_DESC = "Import IPA from CSV file into current dictionary";

	public ImportIPACommand(IPALookupView view) {
		super(view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(getLookupView().getEditor());
		props.setRunAsync(false);
		props.setTitle("Select CSV file with IPA entries");
		props.setFileFilter(FileFilter.csvFilter);
		props.setCanChooseFiles(true);
		props.setCanChooseDirectories(false);
		props.setAllowMultipleSelection(false);
		final List<String> selectedFiles = NativeDialogs.showOpenDialog(props);
		if(selectedFiles != null && selectedFiles.size() == 1) {
			final String selectedFile = selectedFiles.get(0);
			
			getLookupView().getLookupContext().importData(selectedFile);
		}
	}

}
