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

import javax.swing.SwingUtilities;

import ca.phon.app.log.LogUtil;
import ca.phon.app.media.TimeUIModel;
import ca.phon.app.media.TimeUIModel.Marker;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;

/**
 * Split current record interval.
 * 
 */
public class SplitRecordAction extends TimelineAction {
	
	public final static String TXT = "Split record";
	
	public final static String DESC = "Split record using mouse";
	
	public SplitRecordAction(TimelineView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final TimelineView timelineView = getView();
		final TimeUIModel timeModel = timelineView.getTimeModel();
		
		final Record record = timelineView.getEditor().currentRecord();
		if(record == null) return;
		
		final MediaSegment segment = record.getSegment().getGroup(0);
		if(segment == null) return;
		
		float segLength = segment.getEndValue() - segment.getStartValue();
		if(segLength <= 0.0f) return;
		
		float middleOfRecord = segment.getEndValue() - (segLength / 2.0f);
		final Marker splitMarker = timeModel.addMarker(middleOfRecord, Color.black);
		
		try {
			final Robot robot = new Robot();
			
			RecordSplitMouseHandler handler = new RecordSplitMouseHandler(robot, record);
			
			timelineView.addMouseListener(handler);
			timelineView.addMouseMotionListener(handler);
			
			handler.trapMouse();
		} catch (AWTException e) {
			LogUtil.severe(e);
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	private class RecordSplitMouseHandler extends MouseAdapter {
	
		private Robot robot;
		
		private Record record;
		
		private Rectangle2D trapRect;
		
		private int currentX;
		
		public RecordSplitMouseHandler(Robot robot, Record record) {
			super();
			
			this.robot = robot;
			this.record = record;
		}
		
		/**
		 * Trap mouse inside our trap rectangle.
		 * 
		 */
		public void trapMouse() {
			final TimelineView view = getView();
			
			MediaSegment seg = record.getSegment().getGroup(0);
			
			double startX = view.getTimeModel().xForTime( seg.getStartValue() / 1000.0f );
			double endX = view.getTimeModel().xForTime( seg.getEndValue() / 1000.0f );
			
			trapRect = new Rectangle2D.Double(startX, 0, endX - startX, view.getHeight());
			
			// place mouse in middle of trap rect
			Point centerPt = new Point((int)trapRect.getCenterX(), (int)trapRect.getCenterY());
			currentX = centerPt.x;
			SwingUtilities.convertPointToScreen(centerPt, view);
			
			robot.mouseMove(centerPt.x, centerPt.y);
		}
		
		public void mouseMoved(MouseEvent me) {
			System.out.println(me.getPoint());
		}
		
	}
	
}
