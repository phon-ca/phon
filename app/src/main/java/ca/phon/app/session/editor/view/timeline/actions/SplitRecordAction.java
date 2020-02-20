package ca.phon.app.session.editor.view.timeline.actions;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.view.timeline.TimelineRecordTier.SplitMarker;
import ca.phon.media.TimeUIModel;
import ca.phon.media.TimeUIModel.Interval;
import ca.phon.media.TimeUIModel.Marker;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Participants;
import ca.phon.session.Record;
import ca.phon.session.Records;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SessionMetadata;
import ca.phon.session.TierDescription;
import ca.phon.session.TierDescriptions;
import ca.phon.session.TierViewItem;
import ca.phon.session.Transcriber;
import ca.phon.session.Transcribers;
import ca.phon.util.Tuple;

/**
 * Split current record interval.
 * 
 */
public class SplitRecordAction extends TimelineAction {
	
	public final static String TXT = "Split record";
	
	public final static String DESC = "Split record into two";
	
	public SplitRecordAction(TimelineView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getView().getRecordTier().beginSplitMode();
	}
	
}
