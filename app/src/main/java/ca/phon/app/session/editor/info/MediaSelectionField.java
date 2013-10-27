package ca.phon.app.session.editor.info;

import java.io.File;

import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
import ca.phon.ui.FileSelectionField;
import ca.phon.util.PathExpander;

public class MediaSelectionField extends FileSelectionField {
	
	private static final long serialVersionUID = 5171333221664140205L;
	
	private Project project;
	
	public MediaSelectionField() {
		super();
	}

	public MediaSelectionField(Project project) {
		super();
		this.project = project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	public Project getProject() {
		return this.project;
	}
	
	@Override
	public File getSelectedFile() {
		final String txt = super.getText();
		File retVal = null;
		
		final PathExpander pe = new PathExpander();
		
		if(getState() == FieldState.INPUT && txt.length() > 0) {
			File mediaLocatorFile = MediaLocator.findMediaFile(txt, project, null);
			if(mediaLocatorFile != null) {
				retVal = mediaLocatorFile;
			} else {
				retVal = new File(pe.expandPath(txt));
			}
		}
		
		return retVal;
	}
	
}
