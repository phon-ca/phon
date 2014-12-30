package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.media.sampled.PCMSegmentView;

public class SelectSegmentAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = -8882931703864142319L;

	public final static String TXT = "Select segment";
	public final static String DESC = "Change segment to current selection";
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	
	public SelectSegmentAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final PCMSegmentView view = getView();
		
		final float selectionStart = view.getSelectionStart();
		final float selectionLength = view.getSelectionLength();
		
		if(selectionStart > 0 && selectionLength > 0) {
			// turn off UI updates
			view.setValuesAdusting(true);
			view.setSelectionStart(0.0f);
			view.setSelectionLength(0.0f);
			view.setSegmentStart(selectionStart);
			
			// turn on UI updates for last change
			view.setValuesAdusting(false);
			view.setSegmentLength(selectionLength);
		}
	}

}
