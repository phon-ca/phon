package ca.phon.app.project;

import ca.phon.app.workspace.Workspace;
import ca.phon.ui.FileSelectionButton;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectSelectionButton extends FileSelectionButton {

	public ProjectSelectionButton() {
		super();

		setSelectFile(false);
		setSelectFolder(true);

		getTopLabel().setText("Select Project (click to show workspace project list)");

		List<File> projectFiles = Workspace.userWorkspace().getProjects()
				.stream().map( p -> new File(p.getLocation()) ).collect(Collectors.toList());
		setFiles(projectFiles);
	}

}
