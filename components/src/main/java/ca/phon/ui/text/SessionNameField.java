package ca.phon.ui.text;

import ca.phon.project.Project;

/**
 * {@link PromptedTextField} for session names.
 */
public class SessionNameField extends CorpusNameField {

	private static final long serialVersionUID = -4011963535518289390L;

	public SessionNameField() {
		this(null);
	}

	public SessionNameField(Project project) {
		super(project);

		setPrompt("Session name");
	}

	@Override
	protected void setupAutocomplete() {
		super.setupAutocomplete();

		if(getProject() != null) {
			for(String corpus:getProject().getCorpora()) {
				for(String sessionName:getProject().getCorpusSessions(corpus)) {
					getCompleterModel().addCompletion( sessionName, sessionName );
				}
			}
		}
	}

}
