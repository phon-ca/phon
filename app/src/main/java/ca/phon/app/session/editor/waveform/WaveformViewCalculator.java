package ca.phon.app.session.editor.waveform;

import java.awt.geom.Rectangle2D;

/**
 * Interface for performing time <-> location caluations
 * for the wav display.  Used to keep everything aligned.
 */
public interface WaveformViewCalculator {
	
	/**
	 * Get the rectangle for the part of the left side of the display
	 * that is not used in the segment.
	 * 
	 * @return left inset rectangle
	 */
	public Rectangle2D getLeftInsetRect();

	/**
	 * Get the right inset rect for the display
	 * 
	 * @return right inset rectangle
	 */
	public Rectangle2D getRightInsetRect();
	
	/**
	 * Get the rectangle for the segment
	 * 
	 * @return segment rect
	 */
	public Rectangle2D getSegmentRect();
	
}
