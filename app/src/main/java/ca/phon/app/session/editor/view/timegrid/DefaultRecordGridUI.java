package ca.phon.app.session.editor.view.timegrid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.painter.effects.GlowPathEffect;
import org.jdesktop.swingx.painter.effects.InnerGlowPathEffect;

import ca.phon.app.media.TimeUIModel;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class DefaultRecordGridUI extends RecordGridUI {
	
	private final static int TOP_BOTTOM_MARGIN = 5;
	
	private final static int TEXT_MARGIN = 5;
	
	private final static int TIER_GAP = 5;
	
	private RecordGrid recordGrid;
	
	private JLabel renderer;
	
	public DefaultRecordGridUI() {
		super();
		
		renderer = new JLabel();
		renderer.setOpaque(false);
		renderer.setDoubleBuffered(false);
	}
	
	@Override
	public void installUI(JComponent c) {
		if(!(c instanceof RecordGrid)) throw new IllegalArgumentException("Invalid class");
		this.recordGrid = (RecordGrid)c;
		this.recordGrid.setFocusable(true);
		
		this.recordGrid.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				recordGrid.repaint();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				recordGrid.repaint();
			}
		});
		
		this.recordGrid.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				recordGrid.requestFocus();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		c.addPropertyChangeListener(propListener);
	}

	@Override
	public void uninstallUI(JComponent c) {
		c.removePropertyChangeListener(propListener);
	}
	
	private int getSpeakerLabelHeight() {
		renderer.setFont(recordGrid.getFont().deriveFont(Font.BOLD));
		renderer.setIcon(IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		renderer.setText("Testy McTester");
		
		return renderer.getPreferredSize().height;
	}
	
	private int getSpeakerTierHeight() {
		return getSpeakerLabelHeight() + (recordGrid.getTiers().size() * recordGrid.getTierHeight());
	}

	/*
	 * Return the y location of the speaker tier
	 */
	private Rectangle getSpeakerLabelRect(Participant speaker) {
		renderer.setFont(recordGrid.getFont().deriveFont(Font.BOLD));
		renderer.setIcon(IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		renderer.setText(speaker.getName());
		
		int y = TOP_BOTTOM_MARGIN + 
				( recordGrid.getSpeakers().indexOf(speaker) * (getSpeakerTierHeight() + TIER_GAP) );
		int x = TEXT_MARGIN;
		int w = renderer.getPreferredSize().width;
		int h = renderer.getPreferredSize().height;
		
		return new Rectangle(x, y, w, h);
	}
	
	@Override
	public Rectangle2D getSegmentRect(Record record) {
		final MediaSegment seg = record.getSegment().getGroup(0);
		
		double x1 = recordGrid.getTimeModel().xForTime(seg.getStartValue() / 1000.0f);
		double x2 = recordGrid.getTimeModel().xForTime(seg.getEndValue() / 1000.0f);
		
		int y = TOP_BOTTOM_MARGIN + getSpeakerLabelHeight() +
				( recordGrid.getSpeakers().indexOf(record.getSpeaker()) * (getSpeakerTierHeight() + TIER_GAP));
		
		return new Rectangle2D.Double(x1, y, x2-x1, recordGrid.getTierHeight());
	}
	
	@Override
	public Dimension getPreferredSize(JComponent comp) {
		Dimension prefSize = super.getPreferredSize(comp);
		
		int prefHeight = TOP_BOTTOM_MARGIN * 2 + 
				recordGrid.getSpeakers().size() * recordGrid.getTiers().size() * ( recordGrid.getTierHeight() + 25 );
		
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
		
		paintStripes(g2);
		recordGrid.getSpeakers().forEach( (s) -> paintSpeakerLabel(g2, s) );
		
		Session session = recordGrid.getSession();
		for(Record r:session.getRecords()) {
			paintSegment(g2, r);
		}
		
		for(var interval:recordGrid.getTimeModel().getIntervals()) {
			paintInterval(g2, interval);
		}
	}
	
	protected void paintStripes(Graphics2D g2) {
		g2.setColor(PhonGuiConstants.PHON_UI_STRIP_COLOR);
		int speakerTierHeight = getSpeakerTierHeight();
		for(int i = 1; i < recordGrid.getSpeakers().size(); i += 2) {
			int x = recordGrid.getVisibleRect().x;
			int y = TOP_BOTTOM_MARGIN + ( i * (getSpeakerTierHeight() + TIER_GAP));
			int w = recordGrid.getVisibleRect().width;
			int h = speakerTierHeight + TIER_GAP;
			
			g2.fillRect(x, y, w, h);
		}
	}
	
	protected void paintSpeakerLabel(Graphics g2, Participant speaker) {		
		Rectangle speakerLabelRect = getSpeakerLabelRect(speaker);
		speakerLabelRect.x += recordGrid.getVisibleRect().x;
		
		renderer.setFont(recordGrid.getFont().deriveFont(Font.BOLD));
		renderer.setText(speaker.getName());
		renderer.setIcon(IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		SwingUtilities.paintComponent(g2, renderer, recordGrid, speakerLabelRect);
	}
	
	protected void paintSegment(Graphics2D g2, Record r) {
		Rectangle2D segmentRect = getSegmentRect(r);
		RoundRectangle2D roundedRect = new RoundRectangle2D.Double(
				segmentRect.getX(), segmentRect.getY(), segmentRect.getWidth(), segmentRect.getHeight(), 5, 5);
		
		if(segmentRect.intersects(g2.getClipBounds())) {
			paintSegmentLabel(g2, r);
			
			if(recordGrid.getCurrentRecord() == r) {
				g2.setColor(Color.BLUE);
				g2.draw(roundedRect);
				
				if(recordGrid.hasFocus()) {
					InnerGlowPathEffect gpe = new InnerGlowPathEffect();
					gpe.setBrushColor(Color.blue);
					gpe.setEffectWidth(5);
					gpe.apply(g2, roundedRect, 5, 5);
				}
				
			} else {
				g2.setColor(Color.LIGHT_GRAY);
				g2.draw(roundedRect);
			}
		}
	}
	
	protected void paintSegmentLabel(Graphics2D g2, Record r) {
		Rectangle2D segmentRect = getSegmentRect(r);
		
		final Font font = recordGrid.getFont();
		g2.setFont(font);
		
		renderer.setIcon(null);
		renderer.setFont(font);
		
		renderer.setText(r.getOrthography().toString());
		
		g2.setColor(recordGrid.getForeground());
		
		int labelX = (int)segmentRect.getX() + TEXT_MARGIN;
		int labelY = (int)segmentRect.getY();
		int labelWidth = (int)segmentRect.getWidth() - (2 * TEXT_MARGIN);
		int labelHeight = (int)segmentRect.getHeight();
		
		SwingUtilities.paintComponent(g2, renderer, recordGrid, 
				labelX, labelY, labelWidth, labelHeight);
	}

	private final PropertyChangeListener propListener = (e) -> {
		if("speakerCount".equals(e.getPropertyName())) {
			recordGrid.revalidate();
		} else if("currentRecord".equals(e.getPropertyName())) {
			recordGrid.repaint();
		}
	};
	
}
