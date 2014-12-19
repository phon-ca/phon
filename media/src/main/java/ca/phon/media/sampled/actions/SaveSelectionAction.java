package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;

import ca.phon.media.sampled.PCMSegmentView;

public class SaveSelectionAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = -6391208251945921100L;

	public static final String TXT = "Save selection...";
	
	public static final String DESC = "Save selection to file";
	
	public SaveSelectionAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(getView().hasSelection())
			getView().saveSelection();
		
	}

}
