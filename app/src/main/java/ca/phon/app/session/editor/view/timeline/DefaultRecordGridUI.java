package ca.phon.app.session.editor.view.timeline;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.painter.effects.GlowPathEffect;
import org.jdesktop.swingx.painter.effects.InnerGlowPathEffect;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.teamdev.jxbrowser.chromium.internal.ipc.message.GetTitleMessage;

import ca.phon.app.media.TimeUIModel;
import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.TierViewItem;
import ca.phon.session.io.xml.v12.ObjectFactory;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class DefaultRecordGridUI extends RecordGridUI {
	
	private final static int TOP_BOTTOM_MARGIN = 5;
	
	private final static int TEXT_MARGIN = 5;
	
	private final static int TIER_GAP = 5;
	
	private RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> recordTree;
	
	private RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> markerTree;
	
	private RTree<String, com.github.davidmoten.rtree.geometry.Rectangle> messageTree;
	
	private RTree<Action, com.github.davidmoten.rtree.geometry.Rectangle> actionsTree;
	
	private RecordGrid recordGrid;
	
	private JLabel renderer;
	
	public DefaultRecordGridUI() {
		super();
		
		recordTree = RTree.create();
		markerTree = RTree.create();
		messageTree = RTree.create();
		actionsTree = RTree.create();
		
		renderer = new JLabel();
		renderer.setOpaque(false);
		renderer.setDoubleBuffered(false);
	}
	
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
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
		
		c.addPropertyChangeListener(propListener);
		c.addMouseListener(mouseListener);
		c.addMouseMotionListener(mouseListener);
		recordGrid.getTimeModel().addPropertyChangeListener(propListener);
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		
		c.removePropertyChangeListener(propListener);
		c.removeMouseListener(mouseListener);
		c.removeMouseMotionListener(mouseListener);
		
		recordGrid.getTimeModel().removePropertyChangeListener(propListener);
	}
		
	private boolean isRecordPressed(int recordIndex) {
		return (recordIndex == mouseListener.pressedRecordIdx);
	}
	
	private boolean isRecordEntered(int recordIndex) {
		return (recordIndex == mouseListener.enteredRecordIdx);
	}
	
	private int getSpeakerLabelHeight() {
		renderer.setFont(recordGrid.getFont().deriveFont(Font.BOLD));
		renderer.setIcon(IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		renderer.setText("Testy McTester");
		
		return renderer.getPreferredSize().height;
	}
	
	private Font getTierFont(String tierName) {
		Optional<TierViewItem> tvi = 
				recordGrid.getSession().getTierView()
				.stream()
				.filter( (vi) -> vi.getTierName().equals(tierName) )
				.findAny();
		String fontInfo = "default";
		if(tvi.isPresent()) {
			fontInfo = tvi.get().getTierFont();
		}
		
		Font tierFont = ("default".equals(fontInfo)) ? FontPreferences.getTierFont() : Font.decode(fontInfo);
		return tierFont;
	}
	
	private int getTierHeight(String tierName) {
		JLabel testLabel = new JLabel();
		testLabel.setDoubleBuffered(false);
		
		Font tierFont = getTierFont(tierName);
		testLabel.setFont(tierFont);
		testLabel.setText("ABCDEF");
		
		Insets insets = recordGrid.getTierInsets();
		testLabel.setBorder(BorderFactory.createEmptyBorder(
				insets.top, insets.left, insets.bottom, insets.right));
		
		return testLabel.getPreferredSize().height;
	}
	
	private int getSpeakerTierHeight() {
		int tierHeight = 0;
		for(String tierName:recordGrid.getTiers()) {
			tierHeight += getTierHeight(tierName);
		}
		return getSpeakerLabelHeight() + tierHeight;
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
		
		int tierHeight = 0;
		for(String tierName:recordGrid.getTiers()) {
			tierHeight += getTierHeight(tierName);
		}
		
		return new Rectangle2D.Double(x1, y, x2-x1, tierHeight );
	}
	
	@Override
	public Rectangle2D getSpeakerTierRect(Participant participant) {
		int speakerIdx = recordGrid.getSpeakers().indexOf(participant);
		
		int tierHeight = getSpeakerTierHeight();
		int y = TOP_BOTTOM_MARGIN + (speakerIdx * (tierHeight + TIER_GAP));
		int x = 0;
		
		return new Rectangle2D.Double(x, y, recordGrid.getWidth(), tierHeight);
	}
	
	@Override
	public Participant getSpeakerAtPoint(Point pt) {
		Participant retVal = null;
		
		for(Participant p:recordGrid.getSpeakers()) {
			if(getSpeakerTierRect(p).contains(pt)) {
				retVal = p;
				break;
			}
		}
		
		return retVal;
	}
	
	@Override
	public Dimension getPreferredSize(JComponent comp) {
		Dimension prefSize = super.getPreferredSize(comp);
		
		int prefHeight = TOP_BOTTOM_MARGIN * 2 + 
				recordGrid.getSpeakers().size() * (getSpeakerTierHeight() + TIER_GAP);
		
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
		recordTree = RTree.create();
		markerTree = RTree.create();
		messageTree = RTree.create();
		actionsTree = RTree.create();
		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			Record r = session.getRecord(rIdx);
			if(recordGrid.getCurrentRecord() == r && recordGrid.isSplitMode()) {
				paintSegment(g2, rIdx, recordGrid.getLeftRecordSplit());
				paintSegment(g2, (rIdx+2) * -1, recordGrid.getRightRecordSplit());
				
				continue;
			}
			
			Rectangle2D segRect = paintSegment(g2, rIdx, r);
//			System.out.println((rIdx+1) + ":" + segRect);
			recordTree = recordTree.add(rIdx, Geometries.rectangle((float)segRect.getX(), (float)segRect.getY(), 
					(float)(segRect.getX()+segRect.getWidth()), (float)(segRect.getY()+segRect.getHeight())));
			
			// setup 'marker' rectangles 
			
			// start marker
			markerTree = markerTree.add(rIdx + 1, Geometries.rectangle((float)segRect.getX() - MARKER_PADDING, (float)segRect.getY(),
					(float)segRect.getX() + MARKER_PADDING, (float)(segRect.getY()+segRect.getHeight())));
			
			// end marker
			markerTree = markerTree.add(-(rIdx + 1), Geometries.rectangle((float)segRect.getMaxX() - 1, (float)segRect.getY(),
					(float)segRect.getMaxX() + 1, (float)(segRect.getY()+segRect.getHeight())));
		}
		
		for(var interval:recordGrid.getTimeModel().getIntervals()) {
			paintInterval(g2, interval);
		}
		
		for(var marker:recordGrid.getTimeModel().getMarkers()) {
			paintMarker(g2, marker);
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
		
		renderer.setHorizontalTextPosition(SwingConstants.RIGHT);
		renderer.setForeground(recordGrid.getForeground());
		renderer.setFont(recordGrid.getFont().deriveFont(Font.BOLD));
		renderer.setText(speaker.toString());
		renderer.setIcon(IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		SwingUtilities.paintComponent(g2, renderer, recordGrid, speakerLabelRect);
	}
	
	protected Rectangle2D paintSegment(Graphics2D g2, int recordIndex, Record r) {
		Rectangle2D segmentRect = getSegmentRect(r);
		RoundRectangle2D roundedRect = new RoundRectangle2D.Double(
				segmentRect.getX(), segmentRect.getY(), segmentRect.getWidth(), segmentRect.getHeight(), 5, 5);
		
		if(segmentRect.intersects(g2.getClipBounds())) {
			int heightOffset = 0;
			for(String tier:recordGrid.getTiers()) {
				int tierHeight = getTierHeight(tier);
				Rectangle2D labelRect = new Rectangle2D.Double(
						segmentRect.getX(), segmentRect.getY() + heightOffset, segmentRect.getWidth(), tierHeight);
				paintSegmentLabel(g2, r, tier, labelRect);
				heightOffset += tierHeight;
			}
			
			Icon recordIcon = null;
			Color recordLblColor = Color.lightGray;
			
			if(recordGrid.getCurrentRecordIndex() == recordIndex) {
				g2.setColor(Color.BLUE);
				g2.draw(roundedRect);
				
				if(recordGrid.hasFocus()) {
					InnerGlowPathEffect gpe = new InnerGlowPathEffect();
					gpe.setBrushColor(Color.blue);
					gpe.setEffectWidth(5);
					gpe.apply(g2, roundedRect, 5, 5);

					recordLblColor = Color.black;
				}
			} else {
				if(isRecordPressed(recordIndex)) {
					g2.setColor(Color.GRAY);
				} else {
					g2.setColor(Color.LIGHT_GRAY);
				}
				g2.draw(roundedRect);
				if(!isRecordPressed(recordIndex) && isRecordEntered(recordIndex)) {
					InnerGlowPathEffect gpe = new InnerGlowPathEffect();
					gpe.setBrushColor(Color.GRAY);
					gpe.setEffectWidth(5);
					gpe.apply(g2, roundedRect, 5, 5);
				}
			}
			
			String warnings = null;
			// check to see if record overlaps other records for speaker
			var overlapEntries = recordTree.search(Geometries.rectangle(segmentRect.getX(), segmentRect.getY(), 
					segmentRect.getMaxX(), segmentRect.getMaxY()));
			AtomicBoolean overlapRef = new AtomicBoolean(false);
			overlapEntries.map( entry -> entry.value() ).forEach( i -> {
				overlapRef.getAndSet(true);
			});
			
			if(overlapRef.get()) {
				warnings = "Overlapping segments";
				recordIcon = IconManager.getInstance().getIcon("emblems/flag-red", IconSize.XSMALL);
			}
			
			// check to see if record is outside of media bounds			
			float recordEndTime = recordGrid.timeAtX(segmentRect.getMaxX());
			if(recordGrid.getTimeModel().getMediaEndTime() > 0.0f && recordEndTime > recordGrid.getTimeModel().getMediaEndTime()) {
				warnings = (warnings != null ? warnings + "\n" : "" ) + "Segment out of bounds";
				recordIcon = IconManager.getInstance().getIcon("emblems/flag-red", IconSize.XSMALL);
			}
			
			if(recordIndex >= 0) {	
				Rectangle2D lblRect = paintRecordNumberLabel(g2, recordIndex, recordIcon, recordLblColor, segmentRect);
				recordTree = recordTree.add(recordIndex, Geometries.rectangle((float)lblRect.getX(), (float)lblRect.getY(), 
						(float)lblRect.getMaxX(), (float)(lblRect.getMaxY() - 0.1f)));
		
				if(warnings != null) {
					// add warning to UI
					messageTree = messageTree.add(warnings, Geometries.rectangle(lblRect.getX(), lblRect.getY(),
							lblRect.getMaxX(), lblRect.getMaxY()));
				}
			} else {
				// split mode actions
				int recordNum = Math.abs(recordIndex);
				recordLblColor = Color.blue;
				Rectangle2D lblRect = paintRecordNumberLabel(g2, recordNum-1, recordIcon, recordLblColor, segmentRect);
				
				ImageIcon acceptIcon = IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);
				Rectangle2D acceptRect = new Rectangle2D.Double(lblRect.getMaxX() + 2, lblRect.getY(),
						acceptIcon.getIconWidth(), acceptIcon.getIconHeight());
				g2.drawImage(acceptIcon.getImage(), (int)acceptRect.getX(), (int)acceptRect.getY(), recordGrid);
				
				final PhonUIAction acceptAct = new PhonUIAction(this, "endSplitMode", true);
				var acceptRect2 = Geometries.rectangle(acceptRect.getX(), acceptRect.getY(), acceptRect.getMaxX(), acceptRect.getMaxY());
				actionsTree = actionsTree.add(acceptAct, acceptRect2);
				messageTree = messageTree.add("Accept split", acceptRect2);
				
				ImageIcon cancelIcon = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.XSMALL);
				Rectangle2D cancelRect = new Rectangle2D.Double(acceptRect.getMaxX() + 2, acceptRect.getY(),
						cancelIcon.getIconWidth(), cancelIcon.getIconHeight());
				g2.drawImage(cancelIcon.getImage(), (int)cancelRect.getX(), (int)cancelRect.getY(), recordGrid);
				
				final PhonUIAction endAct = new PhonUIAction(this, "endSplitMode", false);
				var cancelRect2 = Geometries.rectangle(cancelRect.getX(), cancelRect.getY(), cancelRect.getMaxX(), cancelRect.getMaxY());
				actionsTree = actionsTree.add(endAct, cancelRect2);
				messageTree = messageTree.add("Exit split mode", cancelRect2);
			}
		}
		return segmentRect;
	}
	
	public void endSplitMode(PhonActionEvent pae) {
		recordGrid.setSplitModeAccept((boolean)pae.getData());
		recordGrid.setSplitMode(false);
	}
	
	private Rectangle2D paintRecordNumberLabel(Graphics2D g2, int recordIndex,
			Icon icon, Color color, Rectangle2D segmentRect) {
		final Font font = recordGrid.getFont();
		if(font != null) {
			renderer.setFont(font);
		}
		renderer.setHorizontalTextPosition(SwingConstants.LEFT);
		renderer.setIcon(icon);
		
		String labelText = String.format("#%d", (recordIndex+1));
		renderer.setText(labelText);
		
		renderer.setForeground(color);
		
		Dimension prefSize = renderer.getPreferredSize();
		
		int labelX = (int)segmentRect.getX();
		int labelY = (int)(segmentRect.getY() - prefSize.getHeight());
		
		SwingUtilities.paintComponent(g2, renderer, recordGrid, 
				labelX, labelY, prefSize.width, prefSize.height);
		
		return new Rectangle2D.Double(labelX, labelY, prefSize.getWidth(), prefSize.getHeight());
	}
	
	protected void paintSegmentLabel(Graphics2D g2, Record r, String tierName, Rectangle2D labelRect) {
		final Font font = getTierFont(tierName);

		Border oldBorder = renderer.getBorder();
		
		Insets insets = recordGrid.getTierInsets();
		
		renderer.setBorder(BorderFactory.createEmptyBorder(
			insets.top, insets.left, insets.bottom, insets.right));
		renderer.setIcon(null);
		renderer.setFont(font);
		renderer.setForeground(recordGrid.getForeground());
		
		String labelText = r.getTier(tierName).toString();
		renderer.setText(labelText);
		
		int labelX = (int)labelRect.getX() + TEXT_MARGIN;
		int labelY = (int)labelRect.getY();
		int labelWidth = (int)labelRect.getWidth() - (2 * TEXT_MARGIN);
		int labelHeight = (int)labelRect.getHeight();
		
		SwingUtilities.paintComponent(g2, renderer, recordGrid, 
				labelX, labelY, labelWidth, labelHeight);
	
		if(labelWidth < renderer.getPreferredSize().width) {
			messageTree = messageTree.add(labelText, 
					Geometries.rectangle(labelRect.getX(), labelRect.getY(), labelRect.getMaxX(), labelRect.getMaxY()));
		}
		
		renderer.setBorder(oldBorder);
	}

	private final PropertyChangeListener propListener = (e) -> {
		if("speakerCount".equals(e.getPropertyName())
				|| "tierCount".equals(e.getPropertyName())
				|| "tierInsets".equals(e.getPropertyName())) {
			recordGrid.revalidate();
		} else if("currentRecordIndex".equals(e.getPropertyName())) {
			recordGrid.repaint(recordGrid.getVisibleRect());
		}
	};
	
	private final RecordGridMouseAdapter mouseListener = new RecordGridMouseAdapter();
	
	private class RecordGridMouseAdapter extends MouseInputAdapter {
		
		private int pressedRecordIdx = -1;
				
		private int enteredRecordIdx = -1;
	
		private RecordGrid.GhostMarker currentMouseOverMarker = null;

		@Override
		public void mousePressed(MouseEvent e) {
			recordGrid.requestFocusInWindow();
			
			var actionEntries = actionsTree.search(Geometries.point(e.getX(), e.getY()));
			actionEntries.map( entry -> entry.value() ).forEach( action -> {
				action.actionPerformed(new ActionEvent(recordGrid, -1, ""));
			});
			
			AtomicReference<Integer> markerRecordRef = new AtomicReference<Integer>(0);
			var markerEntries = markerTree.search(Geometries.point(e.getX(), e.getY()));
			markerEntries.map( entry -> entry.value() ).forEach( i -> {
				markerRecordRef.set(i);
			});

			
			if(markerRecordRef.get() == 0) {
				var entries = recordTree.search(Geometries.point(e.getX(), e.getY()));
				entries.map( entry -> entry.value() ).forEach( i -> {
					pressedRecordIdx = i;
					recordGrid.fireRecordPressed(i, e);
					recordGrid.repaint();
				});
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			var entries = recordTree.search(Geometries.point(e.getX(), e.getY()));
			entries.map( entry -> entry.value() ).forEach( i -> {
				recordGrid.fireRecordClicked(i, e);
				recordGrid.repaint();
			});
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(pressedRecordIdx >= 0) {
				recordGrid.fireRecordReleased(pressedRecordIdx, e);
				pressedRecordIdx = -1;
				recordGrid.repaint();
			}
			pressedRecordIdx = -1;
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			AtomicInteger intersectedRecord = new AtomicInteger(-1);
			var entries = recordTree.search(Geometries.point(e.getX(), e.getY()));
			entries.map( entry -> entry.value() ).forEach( i -> {
				intersectedRecord.set(i);
			});
			
			if(intersectedRecord.get() >= 0 && enteredRecordIdx != intersectedRecord.get()) {
				if(enteredRecordIdx >= 0) {
					recordGrid.fireRecordExited(enteredRecordIdx, e);
				}
				enteredRecordIdx = intersectedRecord.get();
				recordGrid.fireRecordEntered(enteredRecordIdx, e);
				recordGrid.repaint();
			} else if(intersectedRecord.get() < 0 && enteredRecordIdx >= 0) {
				recordGrid.fireRecordExited(enteredRecordIdx, e);
				enteredRecordIdx = -1;
				recordGrid.repaint();
			}
			
			// check to see if we are 'hovering' over a ghost marker
			AtomicReference<Integer> intersectedMarker = new AtomicReference<Integer>(null);
			var markerEntries = markerTree.search(Geometries.point(e.getX(), e.getY()));
			markerEntries.map ( entry -> entry.value() ).forEach( i -> {
				intersectedMarker.set(i);
			});
			
			if(intersectedMarker.get() != null) {
				int currentMouseOverMarkerRecordIdx = Math.abs(intersectedMarker.get()) - 1;
						
				Record record = recordGrid.getSession().getRecord(currentMouseOverMarkerRecordIdx);
				
				if(record != recordGrid.getCurrentRecord()) {
					MediaSegment seg = record.getSegment().getGroup(0);
					
					float markerTime = intersectedMarker.get() > 0 ? 
					seg.getStartValue() / 1000.0f : seg.getEndValue() / 1000.0f;
					
					if(currentMouseOverMarker == null || markerTime != currentMouseOverMarker.getTime()) {
						if(currentMouseOverMarker != null)
							recordGrid.getTimeModel().removeMarker(currentMouseOverMarker);
						currentMouseOverMarker = new RecordGrid.GhostMarker(markerTime);
						currentMouseOverMarker.setStart(intersectedMarker.get() > 0);
						currentMouseOverMarker.addPropertyChangeListener("valueAdjusting", (evt) -> {
							if(currentMouseOverMarker.isValueAdjusting()) {
								// set current record
								recordGrid.fireRecordClicked(currentMouseOverMarkerRecordIdx, e);
							}
						});
						
						recordGrid.getTimeModel().addMarker(currentMouseOverMarker);
					}
			
					recordGrid.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				}
			} else {
				if(currentMouseOverMarker != null) {
					recordGrid.getTimeModel().removeMarker(currentMouseOverMarker);
					currentMouseOverMarker = null;
				}
			}
			
			// finally see if we should display a message
			AtomicReference<String> messageRef = new AtomicReference<String>(null);
			var messageEntries = messageTree.search(Geometries.point(e.getX(), e.getY()));
			messageEntries.map ( entry -> entry.value() ).forEach( v -> {
				messageRef.set(v);
			});
			
			if(messageRef.get() != null) {
				recordGrid.setToolTipText(messageRef.get());
			} else {
				recordGrid.setToolTipText(null);
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent me) {
			if(pressedRecordIdx < 0) return;
			recordGrid.fireRecordDragged(pressedRecordIdx, me);
		}

	};

}
