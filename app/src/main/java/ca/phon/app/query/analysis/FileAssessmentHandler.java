package ca.phon.app.query.analysis;

import java.io.File;
import java.io.IOException;

import ca.phon.util.resources.FolderHandler;

public class FileAssessmentHandler extends FolderHandler<Assessment> {
	
	public FileAssessmentHandler(File folder) {
		super(folder);
		
		setFileFilter( (f) -> { return 
				!f.isHidden() && !f.isDirectory() && f.getName().endsWith(".xml"); } );
	}

	@Override
	public Assessment loadFromFile(File f) throws IOException {
		Assessment retVal = new Assessment(f);
		retVal.setName(f.getName());
		return retVal;
	}

}
