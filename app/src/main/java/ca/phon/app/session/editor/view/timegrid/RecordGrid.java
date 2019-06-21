package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import ca.phon.app.media.Timebar;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;

public class RecordGrid extends TimeGridTier {
	
	private final static int DEFAULT_TIER_HEIGHT = 50;
	
	private int tierHeight = DEFAULT_TIER_HEIGHT;
	
	private List<Participant> speakerList = new ArrayList<>();
	
	private List<String> includedTiers = new ArrayList<>();
	
	private SegmentPanel segmentPanel;
	
	public RecordGrid(TimeGridView parent) {
		super(parent);
		
		speakerList.add(Participant.UNKNOWN);
		parent.getEditor().getSession().getParticipants().forEach( speakerList::add );
		includedTiers.add(SystemTierType.Orthography.getName());
	
		init();
	}

	private void init() {
		segmentPanel = new SegmentPanel();
		setContentPane(segmentPanel);
	}
	
	public int getTierHeight() {
		return this.tierHeight;
	}
	
	public void setTierHeight(int height) {
		var oldVal = this.tierHeight;
		this.tierHeight = height;
		super.firePropertyChange("tierHeight", oldVal, height);
	}
	
	public Rectangle2D getSegmentRect(Record record) {
		final MediaSegment seg = record.getSegment().getGroup(0);
		
		double x1 = xForTime(seg.getStartValue() / 1000.0f);
		double x2 = xForTime(seg.getEndValue() / 1000.0f);
		
		int y = speakerList.indexOf(record.getSpeaker()) * getTierHeight();
		
		return new Rectangle2D.Double(x1, y, x2-x1, tierHeight);
	}
	
	private class SegmentPanel extends JPanel {
		
		public SegmentPanel() {
			super();
			
			setBackground(Color.white);
			setOpaque(true);
			
			setLayout(null);
		}
		
		@Override
		public Dimension getPreferredSize() {
			Dimension prefSize = super.getPreferredSize();
			
			int prefHeight = 
					speakerList.size() * includedTiers.size() * getTierHeight();
			
			return new Dimension(prefSize.width, prefHeight);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			final Graphics2D g2 = (Graphics2D)g;
			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			
			if(isOpaque()) {
				g2.setColor(getBackground());
				g2.fill(g.getClipBounds());
			}
			
			SessionEditor editor = getParentView().getEditor();
			Session session = editor.getSession();
			for(Record r:session.getRecords()) {
				Rectangle2D segmentRect = getSegmentRect(r);
				RoundRectangle2D roundedRect = new RoundRectangle2D.Double(
						segmentRect.getX(), segmentRect.getY(), segmentRect.getWidth(), segmentRect.getHeight(), 5, 5);
				
				if(segmentRect.intersects(g.getClipBounds())) {
//					g2.setColor(Color.lightGray);
//					g2.fill(roundedRect);
					g2.setColor(Color.DARK_GRAY);
					g2.draw(roundedRect);
				}
			}
		}
		
	}
	
}
