package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.TierDescription;

public class SegmentTier extends TimeGridTier {

	private static final long serialVersionUID = 1L;

	private Participant speaker;
	
	private String tierName;
	
	public SegmentTier(TimeGridView parent, Participant speaker, String tierName) {
		super(parent);
		
		this.speaker = speaker;
		this.tierName = tierName;
		
		segmentPanel.setPreferredSize(new Dimension(0, 20));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(segmentPanel, BorderLayout.CENTER);
	}
	
	public Participant getSpeaker() {
		return this.speaker;
	}
	
	public String getTierDescription() {
		return this.tierName;
	}

	final JPanel segmentPanel = new JPanel() {
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			// draw a box for every speaker record
			final Graphics2D g2 = (Graphics2D)g;
			
			final Rectangle2D.Double segmentRect = new Rectangle2D.Double();
			
			final Session session = getParentView().getEditor().getSession();
			for(Record r:session.getRecords()) {
				if(r.getSpeaker() == speaker) {
					final MediaSegment segment = r.getSegment().getGroup(0);
					if(segment != null) {
						float startTime = segment.getStartValue() / 1000.0f;
						double startX = xForTime(startTime);
						float endTime = segment.getEndValue() / 1000.0f;
						double endX = xForTime(endTime);
						
						segmentRect.setRect(startX, 1, endX-startX, getHeight()-2);
						g2.draw(segmentRect);
					}
				}
			}
		}
	};

}
