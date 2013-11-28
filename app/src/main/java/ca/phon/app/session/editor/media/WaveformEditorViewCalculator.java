package ca.phon.app.session.editor.media;

import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;

import ca.phon.media.wavdisplay.TimeBar;

public class WaveformEditorViewCalculator implements WaveformViewCalculator {

	/**
	 * The time bar
	 */
	private final WeakReference<TimeBar> timeBarRef;
	
	public WaveformEditorViewCalculator(TimeBar tb) {
		super();
		
		this.timeBarRef = new WeakReference<TimeBar>(tb);
	}
	
	public TimeBar timebar() {
		return timeBarRef.get();
	}
	
	@Override
	public Rectangle2D getLeftInsetRect() {
		return timebar().getSegStartRect();
	}

	@Override
	public Rectangle2D getRightInsetRect() {
		return timebar().getSegEndRect();
	}

	@Override
	public Rectangle2D getSegmentRect() {
		final Rectangle2D startRect = getLeftInsetRect();
		final Rectangle2D endRect = getRightInsetRect();
		final Rectangle2D retVal = 
				new Rectangle2D.Double(startRect.getX() + startRect.getWidth(), startRect.getY(),
						endRect.getX() - (startRect.getX() + startRect.getWidth()), endRect.getHeight());
		return retVal;
	}

}
