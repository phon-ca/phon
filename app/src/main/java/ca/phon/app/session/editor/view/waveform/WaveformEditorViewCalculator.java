package ca.phon.app.session.editor.view.waveform;

import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;

import ca.phon.media.wavdisplay.TimeBar;
import ca.phon.media.wavdisplay.WavDisplay;

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
	
	@Override
	public Rectangle2D getSelectionRect() {
		Rectangle2D retVal = new Rectangle2D.Double();
		
		final WavDisplay wavDisplay = timeBarRef.get().getWavDisplay();
		if(wavDisplay.get_selectionStart() >= 0
				&& wavDisplay.get_selectionEnd() >= 0) {
			double msPerPixel = (wavDisplay.get_timeBar().getEndMs() - wavDisplay.get_timeBar().getStartMs()) / 
					(double)(wavDisplay.getWidth() - 2 * WavDisplay._TIME_INSETS_);
			
			// convert time values to x positions
			double startXPos = wavDisplay.get_selectionStart() / msPerPixel;
			double endXPos = wavDisplay.get_selectionEnd() / msPerPixel;
			double xPos = 
				Math.min(startXPos, endXPos) + WavDisplay._TIME_INSETS_;
			double rectLen = 
				Math.abs(endXPos - startXPos);
			
			retVal =
				new Rectangle2D.Double(xPos, 0,
						rectLen, wavDisplay.getHeight());
		}
		
		return retVal;
	}

}
