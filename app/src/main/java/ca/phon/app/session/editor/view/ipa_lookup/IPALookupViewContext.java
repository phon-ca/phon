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
package ca.phon.app.session.editor.view.ipa_lookup;

import ca.phon.ipadictionary.ui.IPALookupContext;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

/**
 * IPA Lookup context for app.
 *
 */
public class IPALookupViewContext extends IPALookupContext {

	@Override
	public void dropDictionary(String lang) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setTitle("Drop IPA Database");
		props.setHeader("Drop IPA database " + lang + "?");
		props.setMessage("This will remove all user-defined entries for this database.");
		props.setOptions(MessageDialogProperties.okCancelOptions);
		props.setRunAsync(false);
		int retVal = NativeDialogs.showMessageDialog(props);
		if(retVal == 1) return;
		
		super.dropDictionary(lang);
	}
	
}
