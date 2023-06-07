package ca.phon.session.check;

import ca.phon.plugin.*;
import ca.phon.session.Record;
import ca.phon.session.*;

import java.util.Properties;

@Rank(100)
@PhonPlugin(name = "Aligned Word Count Check")
public class AlignedWordCountCheck implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

	private void reportWordAlignmentDifference(SessionValidator validator, Session session,
	                                           Record record, String tierName, int gIdx) {
	}

	@Override
	public boolean performCheckByDefault() {
		return false;
	}

	@Override
	public boolean checkSession(SessionValidator validator, Session session) {

		// not modified
		return false;
	}

	@Override
	public Properties getProperties() {
		return new Properties();
	}

	@Override
	public void loadProperties(Properties props) {
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionCheck.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheck> getFactory() {
		return (args) -> this;
	}

}
