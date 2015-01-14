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
