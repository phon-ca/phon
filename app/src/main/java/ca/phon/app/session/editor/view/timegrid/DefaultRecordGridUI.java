package ca.phon.app.session.editor.view.timegrid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class DefaultRecordGridUI extends RecordGridUI {
	
	private final static int TOP_BOTTOM_MARGIN = 5;
	
	private RecordGrid recordGrid;
	
	@Override
	public void installUI(JComponent c) {
		if(!(c instanceof RecordGrid)) throw new IllegalArgumentException("Invalid class");
		this.recordGrid = (RecordGrid)c;
	}

	@Override
	public void uninstallUI(JComponent c) {
	}

	@Override
	public Rectangle2D getSegmentRect(Record record) {
		final MediaSegment seg = record.getSegment().getGroup(0);
		
		double x1 = recordGrid.getTimeModel().xForTime(seg.getStartValue() / 1000.0f);
		double x2 = recordGrid.getTimeModel().xForTime(seg.getEndValue() / 1000.0f);
		
		int y = TOP_BOTTOM_MARGIN + recordGrid.getSpeakers().indexOf(record.getSpeaker()) * recordGrid.getTierHeight();
		
		return new Rectangle2D.Double(x1, y, x2-x1, recordGrid.getTierHeight());
	}
	
	@Override
	public Dimension getPreferredSize(JComponent comp) {
		Dimension prefSize = super.getPreferredSize(comp);
		
		int prefHeight = TOP_BOTTOM_MARGIN * 2 + 
				recordGrid.getSpeakers().size() * recordGrid.getTiers().size() * recordGrid.getTierHeight();
		
		return new Dimension(prefSize.width, prefHeight);
	}
	
	@Override
	public void paint(Graphics g, JComponent c) {
		final Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		if(recordGrid.isOpaque()) {
			g2.setColor(recordGrid.getBackground());
			g2.fill(g.getClipBounds());
		}
		
		Session session = recordGrid.getSession();
		for(Record r:session.getRecords()) {
			Rectangle2D segmentRect = getSegmentRect(r);
			RoundRectangle2D roundedRect = new RoundRectangle2D.Double(
					segmentRect.getX(), segmentRect.getY(), segmentRect.getWidth(), segmentRect.getHeight(), 5, 5);
			
			if(segmentRect.intersects(g.getClipBounds())) {
//				g2.setColor(Color.lightGray);
//				g2.fill(roundedRect);
				g2.setColor(Color.DARK_GRAY);
				g2.draw(roundedRect);
			}
		}
	}
	
	protected void paintSegment(Graphics2D g2, Record r) {
		
	}

}
