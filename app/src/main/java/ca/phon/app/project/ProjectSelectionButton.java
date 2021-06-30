package ca.phon.app.project;

import ca.phon.app.workspace.Workspace;
import ca.phon.project.Project;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.MenuBuilder;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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

		List<File> projectFiles = Workspace.userWorkspace().getProjects()
				.stream().map( p -> new File(p.getLocation()) ).collect(Collectors.toList());
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
				PhonUIAction selectFileAct = new PhonUIAction(this, "setSelection", new File(project.getLocation()));
				selectFileAct.putValue(PhonUIAction.NAME, project.getLocation());
				selectFileAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select " + project.getLocation());
				openProjectsMenu.add(selectFileAct);
			}
		}

		JMenu workspaceMenu = builder.addMenu(".", "Workspace projects");
		for(File f:workspaceProjects) {
			PhonUIAction selectFileAct = new PhonUIAction(this, "setSelection", f);
			selectFileAct.putValue(PhonUIAction.NAME, f.getAbsolutePath());
			selectFileAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select " + f.getAbsolutePath());
			workspaceMenu.add(selectFileAct);
		}

		builder.addSeparator(".", "history");
		builder.addItem(".", createBrowseAction());

		menu.show(this, 0, getHeight());
	}

}
