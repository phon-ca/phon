/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.session.editor.view.timeline;

import java.awt.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.tools.Tool;

import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.*;

import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.*;
import ca.phon.ui.menu.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import org.w3c.dom.css.Rect;

public class DefaultRecordGridUI extends RecordGridUI {
	
	private final static int TOP_BOTTOM_MARGIN = 5;
	
	private final static int TEXT_MARGIN = 6;
	
	private final static int TIER_GAP = 5;
	
	private RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> recordTree;
	
	private RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> markerTree;
	
	private RTree<String, com.github.davidmoten.rtree.geometry.Rectangle> messageTree;
	
	private RTree<Action, com.github.davidmoten.rtree.geometry.Rectangle> actionsTree;
	
	private Map<Integer, Rectangle2D> visibleRecords = Collections.synchronizedMap(new HashMap<>());
	
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
		this.recordGrid.setDoubleBuffered(true);
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

		this.recordGrid.getSelectionModel().addListSelectionListener( e -> recordGrid.repaintRecord(recordGrid.getSelectionModel().getAnchorSelectionIndex()) );

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
		return (2 * getSpeakerLabelHeight()) + tierHeight;
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
	
	public void showSpeakerMenu(PhonActionEvent pae) {
		@SuppressWarnings("unchecked")
		Tuple<Participant, Rectangle> tpl = (Tuple<Participant, Rectangle>)pae.getData();
		
		JPopupMenu popupMenu = new JPopupMenu();
		MenuBuilder builder = new MenuBuilder(popupMenu);
		recordGrid.setupParticipantMenu(tpl.getObj1(), builder);
		
		popupMenu.show(recordGrid, tpl.getObj2().x, 
				tpl.getObj2().y + tpl.getObj2().height);
	}
	
	@Override
	public void repaintOverlappingRecords(Record record) {
		Rectangle2D rect = getSegmentRect(record);
		repaintOverlappingRecords(rect);
	}
	
	@Override
	public void repaintOverlappingRecords(Rectangle2D segRect) {
		visibleRecords.values().parallelStream()
			.filter( (r) -> r.intersects(segRect) )
			.forEach( (r) -> recordGrid.repaint(r.getBounds()) );
	}
	
	@Override
	public Rectangle2D getSegmentRect(Record record) {
		final MediaSegment seg = record.getSegment().getGroup(0);
		
		double x1 = recordGrid.getTimeModel().xForTime(seg.getStartValue() / 1000.0f);
		double x2 = recordGrid.getTimeModel().xForTime(seg.getEndValue() / 1000.0f);
		
		int y = TOP_BOTTOM_MARGIN + (2*getSpeakerLabelHeight()) +
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
		
		Session session = recordGrid.getSession();
		recordTree = RTree.create();
		markerTree = RTree.create();
		messageTree = RTree.create();
		actionsTree = RTree.create();

		if(recordGrid.isOpaque()) {
			g2.setColor(recordGrid.getBackground());
			g2.fill(g.getClipBounds());
		}
		
		paintStripes(g2);
		//recordGrid.getSpeakers().forEach( (s) -> paintSpeakerLabel(g2, s) );
		for(Participant speaker:recordGrid.getSpeakers()) {
			Rectangle speakerLblRect = paintSpeakerLabel(g2, speaker);
			final PhonUIAction showSpeakerMenuAct = new PhonUIAction(this, "showSpeakerMenu", new Tuple<Participant, Rectangle>(speaker, speakerLblRect));
			actionsTree = actionsTree.add(showSpeakerMenuAct, Geometries.rectangle(speakerLblRect.getX(), speakerLblRect.getY(), 
					speakerLblRect.getMaxX(), speakerLblRect.getMaxY()));
		}
		
		Map<Participant, Integer> ymap = new HashMap<>();
		Rectangle2D templateRect = (session.getRecordCount() > 0 ? getSegmentRect(session.getRecord(0)) : new Rectangle2D.Double());		
		int speakerTierHeight = getSpeakerTierHeight();
		
		// keep track of segment rects which are visible
		// used to paint segment labels and actions later
		// this ensures that warnings are shown correctly
		visibleRecords.clear();
		
		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			Record r = session.getRecord(rIdx);
			
			MediaSegment seg = r.getSegment().getGroup(0);
			
			// update segment rect location
			int segY = 0;
			if(ymap.containsKey(r.getSpeaker())) {
				segY = ymap.get(r.getSpeaker());
			} else {
				segY = TOP_BOTTOM_MARGIN + 
						( recordGrid.getSpeakers().indexOf(r.getSpeaker()) * (speakerTierHeight + TIER_GAP) );
				segY += 2 * getSpeakerLabelHeight();
				ymap.put(r.getSpeaker(), segY);
			}
			
			var segXmin = recordGrid.xForTime(seg.getStartValue() / 1000.0f);
			var segXmax = recordGrid.xForTime(seg.getEndValue() / 1000.0f);
			
			Rectangle2D segRect = new Rectangle2D.Double(segXmin, segY, segXmax-segXmin, templateRect.getHeight());
			if(!recordGrid.isSplitMode())
				recordTree = recordTree.add(rIdx, Geometries.rectangle((float)segRect.getX(), (float)segRect.getY(), 
						(float)(segRect.getX()+segRect.getWidth()), (float)(segRect.getY()+segRect.getHeight())));
		
			if(segRect.getWidth() > 0 && !g2.getClipBounds().intersects(segRect)) continue;
			if(segRect.getWidth() == 0 && !g2.getClipBounds().contains(new Point((int)segRect.getX(), (int)segRect.getY()))) continue;
			
			if(recordGrid.getCurrentRecord() == r && recordGrid.isSplitMode()) {
				Record leftRecord = recordGrid.getLeftRecordSplit();
				MediaSegment leftRecordSeg = leftRecord.getSegment().getGroup(0);
				segRect.setFrame(segRect.getX(), segRect.getY(), 
						recordGrid.xForTime(leftRecordSeg.getEndValue() / 1000.0f) - segRect.getX(), segRect.getHeight());
				paintSegment(g2, rIdx, recordGrid.getLeftRecordSplit(), segRect);
				paintSegmentLabelAndActions(g2, rIdx, recordGrid.getLeftRecordSplit(), segRect);
				
				Record rightRecord = recordGrid.getRightRecordSplit();
				MediaSegment rightRecordSeg = rightRecord.getSegment().getGroup(0);
				segRect.setFrame(recordGrid.xForTime(rightRecordSeg.getStartValue() / 1000.0f), segRect.getY(), 
						recordGrid.xForTime(rightRecordSeg.getEndValue() / 1000.0f) - recordGrid.xForTime(rightRecordSeg.getStartValue()/1000.0f), segRect.getHeight());
				paintSegment(g2, (rIdx+2) * -1, recordGrid.getRightRecordSplit(), segRect);
				paintSegmentLabelAndActions(g2, (rIdx+2) * -1, recordGrid.getRightRecordSplit(), segRect);
				
				continue;
			}
			
			if(segRect.getWidth() > 0) {
				paintSegment(g2, rIdx, r, segRect);
				visibleRecords.put(rIdx, segRect);
			} else {
				paintZeroLengthSegment(g2, rIdx, r, segRect);
			}
			
			// setup 'marker' rectangles 
			
			// start marker
			markerTree = markerTree.add(rIdx + 1, Geometries.rectangle((float)segRect.getX() - MARKER_PADDING, (float)segRect.getY(),
					(float)segRect.getX() + MARKER_PADDING, (float)(segRect.getY()+segRect.getHeight())));
			
			// end marker
			markerTree = markerTree.add(-(rIdx + 1), Geometries.rectangle((float)segRect.getMaxX() - 1, (float)segRect.getY(),
					(float)segRect.getMaxX() + 1, (float)(segRect.getY()+segRect.getHeight())));
		}
	
		// paint segment labels
		for(Integer rIdx:visibleRecords.keySet()) {
			Rectangle2D segRect = visibleRecords.get(rIdx);
			paintSegmentLabelAndActions(g2, rIdx, session.getRecord(rIdx), segRect);
		}
				
		for(var interval:recordGrid.getTimeModel().getIntervals()) {
			paintInterval(g2, interval, false);
		}
		
		for(var marker:recordGrid.getTimeModel().getMarkers()) {
			paintMarker(g2, marker);
		}

		// paint selection rectangle (if any)
		if(mouseListener.isDraggingSelection) {
			painSelectionRect(g2);
		}
	}

	protected void painSelectionRect(Graphics2D g2) {
		Rectangle selectionRect = mouseListener.getSelectionRect();
		g2.setColor(UIManager.getColor(TimelineViewColors.SELECTION_RECTANGLE));
		g2.fill(selectionRect);


		g2.setColor(Color.lightGray);
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
	
	protected Rectangle paintSpeakerLabel(Graphics g2, Participant speaker) {		
		Rectangle speakerLabelRect = getSpeakerLabelRect(speaker);
		speakerLabelRect.x += recordGrid.getVisibleRect().x;

		DropDownIcon dropDownIcon = new DropDownIcon(IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL),
			0, SwingConstants.BOTTOM);

		renderer.setHorizontalTextPosition(SwingConstants.RIGHT);
		renderer.setForeground(recordGrid.getForeground());
		renderer.setFont(recordGrid.getFont().deriveFont(Font.BOLD));
		renderer.setText(speaker.toString());
		renderer.setIcon(dropDownIcon);
		SwingUtilities.paintComponent(g2, renderer, recordGrid, speakerLabelRect);
		
		return speakerLabelRect;
	}
	
	protected void paintZeroLengthSegment(Graphics2D g2, int recordIndex, Record r, Rectangle2D segmentRect) {
		if(g2.getClipBounds().contains(new Point((int)segmentRect.getX(), (int)segmentRect.getY()))) {
			Icon recordIcon = null;
			Color recordLblColor = Color.lightGray;
			
			Line2D recordLine = new Line2D.Double(segmentRect.getX(), segmentRect.getY(), segmentRect.getX(), segmentRect.getY()+segmentRect.getHeight());
			
			if(recordGrid.getCurrentRecordIndex() == recordIndex) {
				g2.setColor(Color.BLUE);
				g2.draw(recordLine);
				
				if(recordGrid.hasFocus()) {
					recordLblColor = Color.black;
				}
			} else {
				if(isRecordPressed(recordIndex)) {
					g2.setColor(Color.GRAY);
				} else {
					g2.setColor(Color.LIGHT_GRAY);
				}
				if(recordGrid.getSelectionModel().isSelectedIndex(recordIndex)
						|| (mouseListener.isDraggingSelection && mouseListener.getSelectionRect() != null && mouseListener.getSelectionRect().intersects(segmentRect)) ) {
					g2.setColor(Color.BLUE);
				}
				g2.draw(recordLine);
			}
			
			String warnings = null;
			// check to see if record overlaps other records for speaker
			var overlapEntries = recordTree.search(Geometries.rectangle(segmentRect.getX(), segmentRect.getY(), 
					segmentRect.getMaxX(), segmentRect.getMaxY()));
			AtomicBoolean overlapRef = new AtomicBoolean(false);
			overlapEntries.map( entry -> entry.value() )
				.filter( (v) -> recordIndex != v )
				.forEach( i -> {
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
			
			Rectangle2D lblRect = paintRecordNumberLabel(g2, recordIndex, recordIcon, recordLblColor, segmentRect);
			recordTree = recordTree.add(recordIndex, Geometries.rectangle((float)lblRect.getX(), (float)lblRect.getY(), 
					(float)lblRect.getMaxX(), (float)(lblRect.getMaxY() - 0.1f)));
	
			if(warnings != null) {
				// add warning to UI
				messageTree = messageTree.add(warnings, Geometries.rectangle(lblRect.getX(), lblRect.getY(),
						lblRect.getMaxX(), lblRect.getMaxY()));
			}
		}
	}
	
	protected void paintSegmentLabelAndActions(Graphics2D g2, int recordIndex, Record r, Rectangle2D segmentRect) {
		Icon recordIcon = null;
		Color recordLblColor = (recordGrid.getSelectionModel().isSelectedIndex(recordIndex) ? Color.black : Color.lightGray);
		
		// don't paint overlap warning if record is at 0 and has zero-length segment
		boolean checkForOverlap = true;
		final MediaSegment mediaSeg = r.getSegment().getGroup(0);
		if(mediaSeg.getStartValue() == 0.0f
				&& mediaSeg.getEndValue() - mediaSeg.getStartValue() == 0.0f) {
			checkForOverlap = false;
		}
		
		String warnings = null;
		if(checkForOverlap) {
			// check to see if record overlaps other records for speaker
			var overlapEntries = recordTree.search(Geometries.rectangle(segmentRect.getX(), segmentRect.getY(), 
					segmentRect.getMaxX(), segmentRect.getMaxY()));
			List<Integer> overlappingRecordsList = new ArrayList<Integer>();
			List<Integer> potentialOverlaps = new ArrayList<Integer>();
			overlapEntries.map( entry -> entry.value() ).filter( v -> v != recordIndex).forEach(potentialOverlaps::add);
			
			for(int rIdx:potentialOverlaps) {
				
				Record r2 = recordGrid.getSession().getRecord(rIdx);
				MediaSegment seg2 = r2.getSegment().getGroup(0);

				boolean isZeroAtZero = (seg2.getStartValue() == 0.0f) && (seg2.getEndValue() - seg2.getStartValue() == 0.0f);
				boolean isContiguous = (mediaSeg.getStartValue() == seg2.getEndValue() || seg2.getStartValue() == mediaSeg.getEndValue());
				
				if(isZeroAtZero || isContiguous) continue;
				
				overlappingRecordsList.add(rIdx);
			}
			
			if(overlappingRecordsList.size() > 0) {
				warnings = "Overlapping segments ("
						+ overlappingRecordsList.stream().map(rIdx-> String.format("#%d", rIdx+1)).collect(Collectors.joining(","))
						+ ")";
				recordIcon = IconManager.getInstance().getIcon("emblems/flag-red", IconSize.XSMALL);
			}
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
	
	protected Rectangle2D paintSegment(Graphics2D g2, int recordIndex, Record r, Rectangle2D segmentRect) {
		RoundRectangle2D roundedRect = new RoundRectangle2D.Double(
				segmentRect.getX(), segmentRect.getY(), segmentRect.getWidth(), segmentRect.getHeight(), 5, 5);
		
		int heightOffset = 0;
		for(String tier:recordGrid.getTiers()) {
			int tierHeight = getTierHeight(tier);
			Rectangle2D labelRect = new Rectangle2D.Double(
					segmentRect.getX(), segmentRect.getY() + heightOffset, segmentRect.getWidth(), tierHeight);
			paintSegmentLabel(g2, r, tier, labelRect);
			heightOffset += tierHeight;
		}
		
		Stroke oldStroke = g2.getStroke();
		if(recordGrid.getCurrentRecordIndex() == recordIndex) {
			Stroke stroke = new BasicStroke(1.5f);
			g2.setStroke(stroke);
			g2.setColor(Color.BLUE);
			g2.draw(roundedRect);
			
			if(recordGrid.hasFocus()) {
				final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
				g2.setStroke(dashed);
				g2.setColor(Color.GRAY);
				
				RoundRectangle2D foucsRect = new RoundRectangle2D.Double(
						roundedRect.getX() + (TEXT_MARGIN/2), roundedRect.getY() + (TEXT_MARGIN/2),
						roundedRect.getWidth() - TEXT_MARGIN, roundedRect.getHeight() - TEXT_MARGIN,
						roundedRect.getArcWidth(), roundedRect.getArcHeight());
				g2.draw(foucsRect);
			}
		} else {
//			Stroke stroke = new BasicStroke(1.0f);
			if(isRecordPressed(recordIndex)) {
				g2.setColor(Color.GRAY);
			} else {
				g2.setColor(Color.LIGHT_GRAY);
			}
			if(!isRecordPressed(recordIndex) && isRecordEntered(recordIndex)) {
				g2.setColor(Color.GRAY);
			}
			if(recordGrid.getSelectionModel().isSelectedIndex(recordIndex)
				|| (mouseListener.isDraggingSelection && mouseListener.getSelectionRect() != null && mouseListener.getSelectionRect().intersects(segmentRect)) ) {
				g2.setColor(Color.BLUE);
			}

//			g2.setStroke(stroke);
			g2.draw(roundedRect);
		}
		g2.setStroke(oldStroke);
			
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
		
		Tier<?> tier = r.getTier(tierName);
		if(tier != null) {
			String labelText = tier.toString();
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
		}
		
		renderer.setBorder(oldBorder);
	}

	private final PropertyChangeListener propListener = (e) -> {
		if("speakerCount".equals(e.getPropertyName())
				|| "tierCount".equals(e.getPropertyName())
				|| "tierInsets".equals(e.getPropertyName())
				|| "pixelsPerSecond".equals(e.getPropertyName())) {
			recordGrid.revalidate();
			recordGrid.repaint();
		} else if("currentRecordIndex".equals(e.getPropertyName())) {
			recordGrid.repaint(recordGrid.getVisibleRect());
		}
	};
	
	/* Hit tests */
	private <T> Optional<T> hitTest(RTree<T, com.github.davidmoten.rtree.geometry.Rectangle> tree, com.github.davidmoten.rtree.geometry.Point p) {
		var entries = tree.search(p);
		List<Tuple<com.github.davidmoten.rtree.geometry.Rectangle, T>> tupleList = new ArrayList<>();
		entries
			.map( entry -> new Tuple<com.github.davidmoten.rtree.geometry.Rectangle, T>(entry.geometry(), entry.value()))
			.subscribe(tupleList::add);
		
		if(tupleList.size() > 0) {
			double dist = Double.MAX_VALUE;
			Tuple<com.github.davidmoten.rtree.geometry.Rectangle, T> currentTuple = null;
			for(var tuple:tupleList) {
				double d = p.distance(tuple.getObj1());
				
				if(d < dist) {
					dist = d;
					currentTuple = tuple;
				}
			}
			return Optional.of(currentTuple.getObj2());
		} else {
			return Optional.empty();
		}
	}

	private <T> List<T> overlapTest(RTree<T, com.github.davidmoten.rtree.geometry.Rectangle> tree, com.github.davidmoten.rtree.geometry.Rectangle r) {
		var entries = tree.search(r);
		List<Tuple<com.github.davidmoten.rtree.geometry.Rectangle, T>> tupleList = new ArrayList<>();
		entries
				.map( entry -> new Tuple<com.github.davidmoten.rtree.geometry.Rectangle, T>(entry.geometry(), entry.value()))
				.subscribe(tupleList::add);

		List<T> retVal = new ArrayList<>();
		for(var currentTuple:tupleList) {
			retVal.add(currentTuple.getObj2());
		}
		return retVal;
	}
	
	/**
	 * Return the closest action to the given point.
	 * 
	 * @param p
	 * @return
	 */
	private Optional<Action> actionHitTest(com.github.davidmoten.rtree.geometry.Point p) {
		return hitTest(actionsTree, p);
	}
	
	/**
	 * Return the closest marker to the given point
	 * @param p
	 * @return
	 */
	private Optional<Integer> markerHitTest(com.github.davidmoten.rtree.geometry.Point p) {
		return hitTest(markerTree, p);
	}
	
	/**
	 * Return the best record (if any) for the given point.
	 * TODO: Define 'best'
	 * 
	 * @param p
	 * @return
	 */
	private Optional<Integer> recordHitTest(com.github.davidmoten.rtree.geometry.Point p) {
		return hitTest(recordTree, p);
	}
	
	private Optional<String> messageHitTest(com.github.davidmoten.rtree.geometry.Point p) {
		return hitTest(messageTree, p);
	}
	
	private final RecordGridMouseHandler mouseListener = new RecordGridMouseHandler();
	
	private class RecordGridMouseHandler extends MouseInputAdapter {
		
		private int pressedRecordIdx = -1;
				
		private int enteredRecordIdx = -1;
	
		private RecordGrid.GhostMarker currentMouseOverMarker = null;

		private boolean isDraggingSelection = false;
		private Point selectionPoint1 = null;
		private Point selectionPoint2 = null;

		private void beginSelectionDrag(MouseEvent me) {
			Point p = me.getPoint();
			this.isDraggingSelection = true;
			this.selectionPoint1 = p;
			this.selectionPoint2 = p;
			recordGrid.repaint(recordGrid.getVisibleRect());
		}

		private void updateSelectionDrag(MouseEvent me) {
			Point p = me.getPoint();
			this.selectionPoint2 = p;
			recordGrid.repaint(recordGrid.getVisibleRect());
		}

		private void endSelectionDrag(MouseEvent me) {
			Rectangle selectionRect = getSelectionRect();
			com.github.davidmoten.rtree.geometry.Rectangle r =
					Geometries.rectangle(selectionRect.x, selectionRect.y, selectionRect.getMaxX(), selectionRect.getMaxY());
			List<Integer> recordsToSelect = overlapTest(recordTree, r);
			Collections.sort(recordsToSelect);
			Set<Integer> recordSet = new LinkedHashSet<>(recordsToSelect);

			if(recordSet.size() > 0) {
				boolean addToSelection =
						(me.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) == Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
				if (!addToSelection) {
					recordGrid.getSelectionModel().clearSelection();
				}
				for (int reocrdIndex : recordSet)
					recordGrid.getSelectionModel().addSelectionInterval(reocrdIndex, reocrdIndex);
				if (!recordGrid.getSelectionModel().isSelectedIndex(recordGrid.getCurrentRecordIndex())) {
					recordGrid.setCurrentRecordIndex(recordSet.iterator().next());
				}
			}

			this.isDraggingSelection = false;
			recordGrid.repaint(recordGrid.getVisibleRect());
		}

		private Rectangle getSelectionRect() {
			if(selectionPoint1 == null || selectionPoint2 == null) return null;
			Rectangle r1 = new Rectangle(selectionPoint1);
			r1.add(selectionPoint2);
			return r1;
		}

		@Override
		public void mousePressed(MouseEvent e) {			
			if(getCurrentlyDraggedMarker() != null) return;

			if(recordGrid.isFocusable())
				recordGrid.requestFocusInWindow();

			// only handle primary button events here
			if(e.getButton() != MouseEvent.BUTTON1) return;
			
			com.github.davidmoten.rtree.geometry.Point p = 
					Geometries.point(e.getX(), e.getY());
			Optional<Action> actionOpt = actionHitTest(p);
			if(actionOpt.isPresent()) {
				actionOpt.get().actionPerformed(new ActionEvent(recordGrid, -1, ""));
				return;
			}
			
			Optional<Integer> markerOpt = markerHitTest(p);
			if(markerOpt.isEmpty()) {
				Optional<Integer> recordOpt = recordHitTest(p);
				if(recordOpt.isPresent()) {
					var i = recordOpt.get();
					pressedRecordIdx = i;
					recordGrid.fireRecordPressed(i, e);
				} else {
					if(recordGrid.getSelectionModel().getSelectedItemsCount() > 1) {
						if((e.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
							recordGrid.getSelectionModel().setSelectionInterval(recordGrid.getCurrentRecordIndex(), recordGrid.getCurrentRecordIndex());
							recordGrid.repaint(recordGrid.getVisibleRect());
						}
					}
					beginSelectionDrag(e);
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			Optional<Integer> recordOpt = recordHitTest(Geometries.point(e.getX(), e.getY()));
			if(recordOpt.isPresent()) {
				int recordIndex = recordOpt.get().intValue();
				if(e.getButton() == MouseEvent.BUTTON1) {
					if ((e.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) == Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
						if (recordGrid.getSelectionModel().isSelectedIndex(recordIndex))
							recordGrid.getSelectionModel().removeSelectionInterval(recordIndex, recordIndex);
						else
							recordGrid.getSelectionModel().addSelectionInterval(recordIndex, recordIndex);
					} else if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
						recordGrid.getSelectionModel().addSelectionInterval(recordGrid.getSelectionModel().getLeadSelectionIndex(), recordIndex);
					} else if (e.getModifiersEx() == 0) {
						recordGrid.setCurrentRecordIndex(recordIndex);
						recordGrid.getSelectionModel().setSelectionInterval(recordIndex, recordIndex);
					}
				} else if(e.getButton() == MouseEvent.BUTTON3) {
					if(!recordGrid.getSelectionModel().isSelectedIndex(recordIndex)) {
						if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
							recordGrid.getSelectionModel().addSelectionInterval(recordGrid.getSelectionModel().getLeadSelectionIndex(), recordIndex);
						} else {
							recordGrid.setCurrentRecordIndex(recordIndex);
							recordGrid.getSelectionModel().setSelectionInterval(recordIndex, recordIndex);
						}
					}
				}
				recordGrid.fireRecordClicked(recordOpt.get(), e);
			}
			recordGrid.repaint(recordGrid.getVisibleRect());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(pressedRecordIdx >= 0) {
				recordGrid.fireRecordReleased(pressedRecordIdx, e);
				pressedRecordIdx = -1;
			}
			pressedRecordIdx = -1;

			if(isDraggingSelection) {
				endSelectionDrag(e);
			}
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			var p = Geometries.point(e.getX(), e.getY());
			Optional<Integer> intersectedRecord = recordHitTest(p);
			if(intersectedRecord.isPresent()) {
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
			}
			
			// check to see if we are 'hovering' over a ghost marker
			Optional<Integer> intersectedMarker = markerHitTest(p);
			
			if(intersectedMarker.isPresent()) {
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
			Optional<String> messageRef = messageHitTest(p);
			
			if(messageRef.isPresent()) {
				recordGrid.setToolTipText(messageRef.get());
			} else {
				recordGrid.setToolTipText(null);
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent me) {
			if(pressedRecordIdx >= 0)
				recordGrid.fireRecordDragged(pressedRecordIdx, me);

			if(currentMouseOverMarker != null) {
				if(getCurrentlyDraggedMarker() != currentMouseOverMarker) {
					recordGrid.getTimeModel().removeMarker(currentMouseOverMarker);
				}
			}

			if(pressedRecordIdx < 0 && isDraggingSelection) {
				updateSelectionDrag(me);
			}
		}

	};

}
