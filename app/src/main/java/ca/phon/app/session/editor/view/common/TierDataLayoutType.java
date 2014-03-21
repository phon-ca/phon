package ca.phon.app.session.editor.view.common;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enumeration of availabe tier data layout types.
 *
 */
public enum TierDataLayoutType {
	ALIGN_GROUPS(AlignGroupsLayoutProvider.class),
	WRAP_GROUPS(WrapGroupsLayoutProvider.class);
	
	private static final Logger LOGGER = Logger
			.getLogger(TierDataLayoutType.class.getName());
	
	private Class<? extends TierDataLayoutProvider> providerClass;
	
	private TierDataLayoutType(Class<? extends TierDataLayoutProvider> providerClass) {
		this.providerClass = providerClass;
	}
	
	public TierDataLayoutProvider createLayoutProvider() {
		TierDataLayoutProvider retVal = null;
		try {
			retVal = providerClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return retVal;
	}
	
}
