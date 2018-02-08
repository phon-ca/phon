package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

public class BrowseForCorpusMediaFolder extends ProjectWindowAction {

	private final static String TXT = "Select corpus media folder...";

	private final static String DESC = "Browse for corpus media folder.";

	public BrowseForCorpusMediaFolder(ProjectWindow projectWindow) {
		super(projectWindow);

		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project project = getWindow()	.getProject();
		final String corpus = getWindow().getSelectedCorpus();
		if(corpus == null) return;

		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(getWindow());
		props.setRunAsync(true);
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(true);
		props.setAllowMultipleSelection(false);
		final String currentPath = project.getCorpusMediaFolder(corpus);
		if(currentPath != null) {
			File currentFolder = new File(currentPath);
			if(!currentFolder.isAbsolute()) {
				currentFolder = new File(project.getLocation(), currentPath);
			}
			props.setInitialFolder(currentFolder.getAbsolutePath());
		}
		props.setPrompt("Select Folder");
		props.setTitle("Corpus Media Folder");
		props.setListener( (e) -> {
			System.out.println(e.getDialogData());
		});
		NativeDialogs.showOpenDialog(props);
	}

}
