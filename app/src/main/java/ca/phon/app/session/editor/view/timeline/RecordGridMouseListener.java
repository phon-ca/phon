package ca.phon.app.session.editor.view.timeline;

import java.awt.event.*;

/**
 * Interface for handling various mouse events for records in the {@link RecordGrid}
 */
public interface RecordGridMouseListener {

	public void recordClicked(int recordIndex, MouseEvent me);
	
	public void recordPressed(int recordIndex, MouseEvent me);
	
	public void recordReleased(int recordIndex, MouseEvent me);
	
	public void recordEntered(int recordIndex, MouseEvent me);
	
	public void recordExited(int recordIndex, MouseEvent me);
	
	public void recordDragged(int recordIndex, MouseEvent me);
	
}
