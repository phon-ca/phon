package ca.phon.app.session.editor.view.ipa_lookup.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.view.ipa_lookup.IPALookupView;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;

public class ExportIPACommand extends IPALookupViewAction {

	private final static String CMD_NAME = "Export IPA";
	
	private final static String SHORT_DESC = "Export current IPA dictionary to CSV";
	
	// TODO icon
	
	// TODO keystroke
	
	public ExportIPACommand(IPALookupView view) {
		super(view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(getLookupView().getEditor());
		props.setRunAsync(false);
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.csvFilter);
		props.setTitle("Save IPA dictionary");
		final String saveFile = NativeDialogs.showSaveDialog(props);
		if(saveFile != null) {
			getLookupView().getLookupContext().exportData(saveFile);
		}
	}

}
