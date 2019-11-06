package ca.phon.app.session.editor.view.timeline;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

import javax.swing.plaf.ComponentUI;

import ca.phon.app.media.TimeComponentUI;
import ca.phon.session.Participant;
import ca.phon.session.Record;

public abstract class RecordGridUI extends TimeComponentUI {

	public abstract Rectangle2D getSegmentRect(Record record);
	
	public abstract Rectangle2D getSpeakerTierRect(Participant participant);
	
	public abstract Participant getSpeakerAtPoint(Point pt);
	
	public abstract void paintOverlappingRecords(Record r);
	
	public abstract void paintOverlappingRecords(Rectangle2D segRect);
	
}
