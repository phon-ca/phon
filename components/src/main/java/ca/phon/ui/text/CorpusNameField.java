package ca.phon.ui.text;

import ca.phon.project.Project;

/**
 * {@link PromptedTextField} for entering a corpus name.
 * If a project is provided, an autocomplete list of
 * current corpora is provided.
 */
public class CorpusNameField extends PromptedTextField {

	private static final long serialVersionUID = -1217905871664832312L;

	private Project project;

	private DefaultTextCompleterModel completerModel;

	public CorpusNameField() {
		this(null);
	}

	public CorpusNameField(Project project) {
		super();

		setPrompt("Corpus name");
		setProject(project);
	}

	public void setProject(Project project) {
		this.project = project;
		setupAutocomplete();
	}

	public Project getProject() {
		return this.project;
	}

	public DefaultTextCompleterModel getCompleterModel() {
		return this.completerModel;
	}

	protected void setupAutocomplete() {
		if(this.completerModel == null) {
			this.completerModel = new DefaultTextCompleterModel();

			final TextCompleter completer = new TextCompleter(completerModel);
			completer.setUseDataForCompletion(true);
			completer.install(this);
		}
		if(getProject() != null) {
			getProject().getCorpora().forEach( (corpus) -> completerModel.addCompletion(corpus, corpus) );
		}
	}

}
