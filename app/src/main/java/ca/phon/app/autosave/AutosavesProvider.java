package ca.phon.app.autosave;

import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.project.Project;

@Extension(Project.class)
public class AutosavesProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		final Project project = Project.class.cast(obj);
		final Autosaves autosaves = new Autosaves(project);
		project.putExtension(Autosaves.class, autosaves);
	}

}
