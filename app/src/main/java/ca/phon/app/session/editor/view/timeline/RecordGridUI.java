package ca.phon.app.session.editor.view.timeline;

import java.awt.geom.Rectangle2D;

import javax.swing.plaf.ComponentUI;

import ca.phon.app.media.TimeComponentUI;
import ca.phon.session.Record;

public abstract class RecordGridUI extends TimeComponentUI {

	public abstract Rectangle2D getSegmentRect(Record record);
	
}
