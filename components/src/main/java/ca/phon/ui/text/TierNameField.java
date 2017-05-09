package ca.phon.ui.text;

import java.util.Arrays;

import ca.phon.project.Project;
import ca.phon.session.SystemTierType;

/**
 * {@link PromptedTextField} for a tier name.  If a project is given,
 * tier names from sessions may be added to the autocompletion model.
 */
public class TierNameField extends PromptedTextField {

	private static final long serialVersionUID = 7768945922139068933L;

	private Project project;

	private DefaultTextCompleterModel completerModel;

	public TierNameField() {
		this(null);
	}

	public TierNameField(Project project) {
		super();

		setProject(project);
	}

	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
		setupAutocomplete();
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
		Arrays.stream(SystemTierType.values()).forEach( (tier) -> completerModel.addCompletion( tier.getName(), tier.getName() ) );

		if(getProject() != null) {
			// TODO setup tier names
		}
	}

}
