package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.media.sampled.PCMSegmentView;

public class StopAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = 4320391551003396362L;

	public final static String TXT = "Stop";
	
	public final static String DESC = "Stop playback";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	
	public StopAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getView().stop();
	}

}
