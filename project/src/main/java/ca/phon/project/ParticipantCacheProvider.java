/*
 * 
 */
package ca.phon.project;

import ca.phon.extensions.*;

@Extension(Project.class)
public class ParticipantCacheProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		final Project project = Project.class.cast(obj);
		final ParticipantCache cache = new ParticipantCache(project);
		project.putExtension(ParticipantCache.class, cache);
	}

}
