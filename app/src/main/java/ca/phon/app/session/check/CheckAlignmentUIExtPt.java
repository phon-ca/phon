package ca.phon.app.session.check;

import ca.phon.plugin.*;
import ca.phon.session.check.*;

@SessionCheckTarget(CheckAlignment.class)
public class CheckAlignmentUIExtPt implements IPluginExtensionPoint<SessionCheckUI>, IPluginExtensionFactory<SessionCheckUI> {

	@Override
	public SessionCheckUI createObject(Object... args) {
		if(args.length != 1
				|| args[0].getClass() != CheckAlignment.class) {
			throw new IllegalArgumentException();
		}
		return new CheckAlignmentUI((CheckAlignment)args[0]);
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
