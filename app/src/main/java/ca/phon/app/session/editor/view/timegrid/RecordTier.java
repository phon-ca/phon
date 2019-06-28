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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.phon.app.media.Timebar;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;

public class RecordTier extends TimeGridTier {
	
	private RecordGrid recordGrid;
	
	public RecordTier(TimeGridView parent) {
		super(parent);
	
		init();
	}

	private void init() {
		Session session = getParentView().getEditor().getSession();
		recordGrid = new RecordGrid(getTimeModel(), session);
		
		recordGrid.addSpeaker(Participant.UNKNOWN);
		session.getParticipants().forEach( recordGrid::addSpeaker );
		recordGrid.addTier(SystemTierType.Orthography.getName());
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(recordGrid, BorderLayout.CENTER);
	}
	
	private class SegmentPanel extends JPanel {
		
		public SegmentPanel() {
			super();
			
			setBackground(Color.white);
			setOpaque(true);
			
			setLayout(null);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			
		}
		
	}
	
	public boolean isResizeable() {
		return false;
	}
	
}
