package ca.phon.app.session.editor.view.ipa_lookup.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import ca.phon.app.session.editor.view.ipa_lookup.IPALookupView;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

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
