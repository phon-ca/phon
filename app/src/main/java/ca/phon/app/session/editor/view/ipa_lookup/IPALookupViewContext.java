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
