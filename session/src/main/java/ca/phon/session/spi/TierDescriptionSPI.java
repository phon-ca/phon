package ca.phon.session.spi;

import ca.phon.session.*;

public interface TierDescriptionSPI {

	/**
	 * Is the tier grouped, if tier is not grouped,
	 * {@link Tier#numberOfGroups()} will always return 1.
	 * 
	 * @return <code>true</code> if the tier is grouped, <code>false</code>
	 *  otherwise
	 */
	public boolean isGrouped();
	
	/**
	 * Get the name of the tier.
	 * 
	 * @return name of the tier
	 */
	public String getName();
	
	/**
	 * Get declared type of the tier
	 * 
	 * 
	 */
	public Class<?> getDeclaredType();
	
}
