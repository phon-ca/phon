package ca.phon.session.spi;

public interface TierViewItemSPI {

	/**
	 * Tier name
	 * 
	 */
	public String getTierName();
	
	/**
	 * Tier visibility
	 */
	public boolean isVisible();
	
	/**
	 * Get the font.  The string should be parsable
	 * by the standard awt.Font class.
 	 */
	public String getTierFont();
	
	/**
	 * Get is locked
	 */
	public boolean isTierLocked();
	
}
