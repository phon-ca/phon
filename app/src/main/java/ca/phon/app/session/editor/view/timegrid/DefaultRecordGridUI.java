package ca.phon.app.session.editor.view.timegrid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.teamdev.jxbrowser.chromium.swing.internal.SwingUtil;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.orthography.Orthography;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class DefaultRecordGridUI extends RecordGridUI {
	
	private final static int TOP_BOTTOM_MARGIN = 5;
	
	private final static int TEXT_MARGIN = 5;
	
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
			paintSegment(g2, r);
		}
	}
	
	protected void paintSegment(Graphics2D g2, Record r) {
		Rectangle2D segmentRect = getSegmentRect(r);
		RoundRectangle2D roundedRect = new RoundRectangle2D.Double(
				segmentRect.getX(), segmentRect.getY(), segmentRect.getWidth(), segmentRect.getHeight(), 5, 5);
		
		if(segmentRect.intersects(g2.getClipBounds())) {
			paintSegmentLabel(g2, r);
			
			g2.setColor(Color.DARK_GRAY);
			g2.draw(roundedRect);
		}
	}
	
	protected void paintSegmentLabel(Graphics2D g2, Record r) {
		Rectangle2D segmentRect = getSegmentRect(r);
		
		final Font font = recordGrid.getFont();
		final FontMetrics fm = g2.getFontMetrics(font);	
		g2.setFont(font);
		Rectangle2D ellipsisRect = fm.getStringBounds("\u2026", g2);
		
		JLabel renderer = new JLabel();
		renderer.setOpaque(false);
		renderer.setFont(font);
		renderer.setDoubleBuffered(false);
		
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("<html>");
//		for(Orthography ortho:r.getOrthography()) {
//			buffer.append("<span color='#cccccc'>[</span>");
//			buffer.append(ortho.toString());
//			buffer.append("<span color='#cccccc'>]</span>");
//		}
//		buffer.append("</html>");
//		String label = buffer.toString();
		renderer.setText(r.getOrthography().toString());
		
		g2.setColor(recordGrid.getForeground());
		
		int labelX = (int)segmentRect.getX() + TEXT_MARGIN;
		int labelY = (int)segmentRect.getY();
		int labelWidth = (int)segmentRect.getWidth() - (2 * TEXT_MARGIN);
		
//		if(labelWidth < renderer.getPreferredSize().getWidth()) {
//			labelWidth -= (int)ellipsisRect.getWidth();
//		}
		
		int labelHeight = (int)segmentRect.getHeight();
		
		SwingUtilities.paintComponent(g2, renderer, recordGrid, 
				labelX, labelY, labelWidth, labelHeight);
		
//		// paint ellipsis if necessary
//		if(labelWidth < renderer.getPreferredSize().getWidth()) {
//			renderer.setText("\u2026");
//			SwingUtilities.paintComponent(g2, renderer, recordGrid, 
//					labelX + labelWidth, labelY, (int)renderer.getPreferredSize().getWidth(), labelHeight);
//		}
	}

}
