package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

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
		final List<String> corpora = getWindow().getSelectedCorpora();
		if(corpora.size() == 0) return;

		if(corpora.size() == 1) {
			final String corpus = corpora.get(0);
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
		} else {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getWindow());
			props.setRunAsync(true);
			final String[] options = {"Cancel", "Reset to project default", "Browse for folder..."};
			props.setOptions(options);
			props.setDefaultOption(options[0]);
			props.setMessage("Select media folder for corproa");
			props.setHeader("Select media folder for corpora");
			props.setListener( (e) -> {
				int result = e.getDialogResult();
				if(result == 0) {
					return;
				} else if(result == 1) {
					for(String corpus:corpora) {
						project.setCorpusMediaFolder(corpus, null);
					}
				} else if(result == 2) {
					SwingUtilities.invokeLater( this::browseForMediaFolder );
				}
			});
			NativeDialogs.showMessageDialog(props);
		}
	}

	private void browseForMediaFolder() {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(getWindow());
		props.setRunAsync(true);
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(true);
		props.setAllowMultipleSelection(false);
		props.setPrompt("Select Folder");
		props.setTitle("Corpus Media Folder");
		props.setListener( (e) -> {
			if(e.getDialogData() == null) return;

			final Project project = getWindow().getProject();
			for(String corpus:getWindow().getSelectedCorpora()) {
				final String selectedFolder = e.getDialogData().toString();
				project.setCorpusMediaFolder(corpus, selectedFolder);
			}
		});
		NativeDialogs.showOpenDialog(props);
	}

}
