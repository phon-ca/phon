package ca.phon.app.project;

import ca.phon.app.workspace.Workspace;
import ca.phon.project.Project;
import ca.phon.project.ProjectPaths;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.FileSelectionButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A button for selecting {@link Project} folders.
 * Provides a list of workspace and (optional) open projects.
 *
 */
public class ProjectSelectionButton extends FileSelectionButton {

	private boolean showOpenProjects = true;

	public ProjectSelectionButton() {
		super();

		setSelectFile(false);
		setSelectFolder(true);

		getTopLabel().setText("Project folder (click select a project folder)");

		List<File> projectFiles = new ArrayList<>();
		for(Project p:Workspace.userWorkspace().getProjects()) {
			final ProjectPaths projectPaths = p.getExtension(ProjectPaths.class);
			if(projectPaths != null) {
				projectFiles.add(new File(projectPaths.getLocation()));
			}
		}
		setFiles(projectFiles);
	}

	public boolean isShowOpenProjects() {
		return showOpenProjects;
	}

	public void setShowOpenProjects(boolean showOpenProjects) {
		this.showOpenProjects = showOpenProjects;
	}

	public void onShowFiles(PhonActionEvent pae) {
		if(this.getFiles() == null) return;

		Iterable<File> workspaceProjects = getFiles();

		JPopupMenu menu = new JPopupMenu();
		MenuBuilder builder = new MenuBuilder(menu);


		if(this.showOpenProjects) {
			JMenu openProjectsMenu = builder.addMenu(".", "Open projects");
			for(Project project: CommonModuleFrame.getProjectWindows().keySet()) {
				final ProjectPaths projectPaths = project.getExtension(ProjectPaths.class);
				if(projectPaths != null) {
					PhonUIAction<File> selectFileAct = PhonUIAction.consumer(this::setSelection, new File(projectPaths.getLocation()));
					selectFileAct.putValue(PhonUIAction.NAME, projectPaths.getLocation());
					selectFileAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select " + projectPaths.getLocation());
					openProjectsMenu.add(selectFileAct);
				}
			}
		}

		JMenu workspaceMenu = builder.addMenu(".", "Workspace projects");
		for(File f:workspaceProjects) {
			PhonUIAction<File> selectFileAct = PhonUIAction.consumer(this::setSelection, f);
			selectFileAct.putValue(PhonUIAction.NAME, f.getAbsolutePath());
			selectFileAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select " + f.getAbsolutePath());
			workspaceMenu.add(selectFileAct);
		}

		builder.addSeparator(".", "history");
		builder.addItem(".", createBrowseAction());

		menu.show(this, 0, getHeight());
	}

}
