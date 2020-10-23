package ca.phon.session.check;

import java.util.*;

import ca.phon.plugin.*;
import ca.phon.session.*;

public class RatifyRecordTiers implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

	@Override
	public boolean checkSession(SessionValidator validator, Session session) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Properties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadProperties(Properties props) {
		// TODO Auto-generated method stub
		
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
