package ca.phon.app.session.editor.view.timeline;

import java.awt.*;
import java.awt.geom.*;

import ca.phon.media.*;
import ca.phon.session.*;
import ca.phon.session.Record;

public abstract class RecordGridUI extends TimeComponentUI {

	public abstract Rectangle2D getSegmentRect(Record record);
	
	public abstract Rectangle2D getSpeakerTierRect(Participant participant);
	
	public abstract Participant getSpeakerAtPoint(Point pt);
	
	public abstract void repaintOverlappingRecords(Record r);
	
	public abstract void repaintOverlappingRecords(Rectangle2D segRect);
	
}
