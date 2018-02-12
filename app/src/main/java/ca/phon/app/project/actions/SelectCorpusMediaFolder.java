package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.SwingUtilities;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

public class SelectCorpusMediaFolder extends ProjectWindowAction {

	private final static String TXT = "Select corpus media folder...";

	private final static String DESC = "Select corpus media folder.";

	public SelectCorpusMediaFolder(ProjectWindow projectWindow) {
		super(projectWindow);

		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project project = getWindow()	.getProject();
		final String corpus = getWindow().getSelectedCorpus();
		if(corpus == null) return;

		final String defaultMediaFolder = project.getProjectMediaFolder();
		final String currentMediaFolder = project.getCorpusMediaFolder(corpus);
		if(currentMediaFolder.equals(defaultMediaFolder)) {
			browseForMediaFolder();
		} else {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getWindow());
			props.setRunAsync(true);
			final String[] options = {"Cancel", "Reset to project default", "Browse for folder..."};
			props.setOptions(options);
			props.setDefaultOption(options[0]);
			props.setMessage("Select media folder for corpus '" + corpus + "'");
			props.setHeader("Select corpus media folder");
			props.setListener( (e) -> {
				int result = e.getDialogResult();
				if(result == 0) {
					return;
				} else if(result == 1) {
					project.setCorpusMediaFolder(corpus, null);
				} else if(result == 2) {
					SwingUtilities.invokeLater( this::browseForMediaFolder );
				}
			});
			NativeDialogs.showMessageDialog(props);
		}
	}

	private void browseForMediaFolder() {
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
			if(e.getDialogData() == null) return;

			final String selectedFolder = e.getDialogData().toString();
			project.setCorpusMediaFolder(corpus, selectedFolder);
		});
		NativeDialogs.showOpenDialog(props);
	}

}
