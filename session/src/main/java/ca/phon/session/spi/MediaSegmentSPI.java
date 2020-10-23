package ca.phon.session.spi;

import ca.phon.session.*;

public interface MediaSegmentSPI {

	/**
	 * Get the start value
	 * @return float
	 */
	public float getStartValue();
	
	/**
	 * Set the start value
	 * @param start
	 */
	public void setStartValue(float start);
	
	/**
	 * Get the end value
	 * @return float
	 */
	public float getEndValue();
	
	/**
	 * Set the end value
	 * @param end
	 */
	public void setEndValue(float end);
	
	/**
	 * Get the unit type.
	 * @return MediaUnitType
	 */
	public MediaUnit getUnitType();
	
	/**
	 * Set the unit type
	 * @param type
	 */
	public void setUnitType(MediaUnit type);
	
}
