package ca.phon.app.query.analysis;

import java.io.File;

import ca.phon.util.PrefHelper;
import ca.phon.util.resources.ResourceLoader;

/**
 * 
 */
public class AssessmentLibrary {
	
	private final static String ASSESSMENT_LIST = "assessments.list";
	
	private final static String USER_FOLDER =
			PrefHelper.getUserDataFolder() + File.separator + "assessments";

	private ResourceLoader<Assessment> userAssessmentLoader;
	
	public AssessmentLibrary() {
		super();
		
		initLoaders();
	}
	
	private void initLoaders() {
		userAssessmentLoader = new ResourceLoader<>();
		userAssessmentLoader.addHandler(new FileAssessmentHandler(new File(USER_FOLDER)));
	}
	
	public ResourceLoader<Assessment> userAssessments() {
		return userAssessmentLoader;
	}
	
}
