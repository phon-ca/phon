package ca.phon.session.impl;

import ca.phon.session.TierDescription;

/**
 * Basic description of a tier.
 */
public class TierDescriptionImpl implements TierDescription {

	private final boolean isGrouped;
	
	private final String name;
	
	private final Class<?> declaredType;
	
	TierDescriptionImpl(String name, boolean grouped) {
		this(name, grouped, String.class);
	}
	
	TierDescriptionImpl(String name, boolean grouped, Class<?> declaredType) {
		super();
		this.isGrouped = grouped;
		this.name = name;
		this.declaredType = declaredType;
	}
	
	@Override
	public boolean isGrouped() {
		return isGrouped;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getDeclaredType() {
		return declaredType;
	}

}
