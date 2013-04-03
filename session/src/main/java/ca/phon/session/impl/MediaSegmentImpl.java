package ca.phon.session.impl;

import ca.phon.session.MediaSegment;
import ca.phon.session.MediaUnit;

/**
 * Media segment
 */
public class MediaSegmentImpl implements MediaSegment {
	
	MediaSegmentImpl() {
		super();
	}
	
	MediaSegmentImpl(float start, float end, MediaUnit unit) {
		super();
		this.startValue = start;
		this.endValue = end;
		this.unit = unit;
	}
	
	/**
	 * Start value
	 */
	private float startValue = 0.0f;
	
	/**
	 * End value
	 */
	private float endValue = 0.0f;
	
	/**
	 * Unit (default:ms)
	 */
	private MediaUnit unit = MediaUnit.Millisecond;

	@Override
	public float getStartValue() {
		return startValue;
	}

	@Override
	public void setStartValue(float start) {
		this.startValue = start;
	}

	@Override
	public float getEndValue() {
		return endValue;
	}

	@Override
	public void setEndValue(float end) {
		this.endValue = end;
	}

	@Override
	public MediaUnit getUnitType() {
		return unit;
	}

	@Override
	public void setUnitType(MediaUnit type) {
		this.unit = type;
	}

}
