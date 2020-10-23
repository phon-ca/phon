package ca.phon.app.session.check;

import ca.phon.plugin.*;
import ca.phon.session.check.*;

@SessionCheckTarget(CheckTranscripts.class)
public class CheckTranscriptsUIExtPt implements IPluginExtensionPoint<SessionCheckUI>, IPluginExtensionFactory<SessionCheckUI> {

	@Override
	public SessionCheckUI createObject(Object... args) {
		if(args.length != 1
				|| args[0].getClass() != CheckTranscripts.class) {
			throw new IllegalArgumentException();
		}
		
		return new CheckTranscriptsUI((CheckTranscripts)args[0]);
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionCheckUI.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheckUI> getFactory() {
		return this;
	}

}
