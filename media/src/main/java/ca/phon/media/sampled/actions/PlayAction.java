package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.media.sampled.PCMSegmentView;

public class PlayAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = 3592378313027362147L;
	
	public final static String TXT = "Play segment/selection";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
	
	public PlayAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getView().play();
	}

}
