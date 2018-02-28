package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.SwingUtilities;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;
import ca.phon.ui.nativedialogs.*;
import ca.phon.worker.PhonWorker;

public class SelectProjectMediaFolder extends ProjectWindowAction {

	private final static String TXT = "Select project media folder...";

	private final static String DESC = "Select project media folder.";

	public SelectProjectMediaFolder(ProjectWindow projectWindow) {
		super(projectWindow);

		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project project = getWindow()	.getProject();

		final String defaultMediaFolder = project.getResourceLocation() + File.separator + "media";
		if(project.getProjectMediaFolder().equals(defaultMediaFolder)) {
			browseForMediaFolder();
		} else {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getWindow());
			props.setRunAsync(true);
			final String[] options = {"Cancel", "Reset to default (__res/media)", "Browse for folder..."};
			props.setOptions(options);
			props.setDefaultOption(options[0]);
			props.setMessage("Select media folder for project");
			props.setHeader("Select media folder");
			props.setListener( (e) -> {
				int result = e.getDialogResult();
				if(result == 0) {
					return;
				} else if(result == 1) {
					PhonWorker.getInstance().invokeLater( () -> project.setProjectMediaFolder(null) );
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
		props.setTitle("Project media folder");
		props.setListener( (e) -> {
			if(e.getDialogData() == null) return;

			final String selectedFolder = e.getDialogData().toString();
			
			PhonWorker.getInstance().invokeLater( () -> project.setProjectMediaFolder(selectedFolder) );
		});
		NativeDialogs.showOpenDialog(props);
	}

}
