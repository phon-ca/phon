/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.media.sampled;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer.Info;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MouseInputAdapter;

import ca.phon.media.sampled.actions.PlayAction;
import ca.phon.media.sampled.actions.PlaySegmentAction;
import ca.phon.media.sampled.actions.PlaySelectionAction;
import ca.phon.media.sampled.actions.SaveSegmentAction;
import ca.phon.media.sampled.actions.SaveSelectionAction;
import ca.phon.media.sampled.actions.SelectMixerAction;
import ca.phon.media.sampled.actions.SelectSegmentAction;
import ca.phon.media.sampled.actions.StopAction;
import ca.phon.media.sampled.actions.ToggleChannelVisible;
import ca.phon.media.sampled.actions.ToggleLoop;
import ca.phon.ui.nativedialogs.OSInfo;
import ca.phon.util.MsFormatter;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;


/**
 * Default UI for PCM segment view.
 *
 */
public class DefaultPCMSegmentViewUI extends PCMSegmentViewUI {
	
	private final static int DEFAULT_CHANNEL_HEIGHT = 60;
	
	private final static int DEFAULT_PIXEL_PER_SEC = 100;
	
	private final static int DEFAULT_TIMEBAR_HEIGHT = 30;
	
	private PCMSegmentView view;
	
	private Rectangle2D contentRect;
	
	private double timeBarHeight = 0.0;
	
	private Map<Channel, SampledPainter> channelPainters = new HashMap<Channel, SampledPainter>();
	
	private Cursor magnifyCursor;
	
	public DefaultPCMSegmentViewUI(PCMSegmentView view) {
		super();
		this.view = view;
		
		final ImageIcon magnifyIcon = IconManager.getInstance().getIcon("actions/magnifier", IconSize.LARGE);
		magnifyCursor = Toolkit.getDefaultToolkit().createCustomCursor(magnifyIcon.getImage(), new Point(0,0), "magnify");
		
		install(view);
	}

	private void install(PCMSegmentView view) {
		setupKeymap(view);
		
		view.addKeyListener(keyListener);
		
		view.addMouseListener(mouseHandler);
		view.addMouseMotionListener(mouseHandler);
		view.addMouseWheelListener(mouseHandler);
		
		view.addPropertyChangeListener(PCMSegmentView.SAMPLED_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.SEGMENT_START_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.SELECTION_LENGTH_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.SEGMENT_LENGTH_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.WINDOW_START_PROT, propListener);
		view.addPropertyChangeListener(PCMSegmentView.WINDOW_LENGTH_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.CURSOR_LOCATION_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.CHANNEL_VISIBLITY_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.CHANNEL_COLOR_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.PLAYBACK_MARKER_PROP, propListener);
		
		view.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	private void setupKeymap(PCMSegmentView view) {
		final ActionMap actionMap = view.getActionMap();
		final InputMap inputMap = view.getInputMap(JComponent.WHEN_FOCUSED);
		
		inputMap.put(SelectSegmentAction.KS, SelectSegmentAction.TXT);
		actionMap.put(SelectSegmentAction.TXT, new SelectSegmentAction(view));
		
		inputMap.put(PlayAction.KS, PlayAction.TXT);
		actionMap.put(PlayAction.TXT, new PlayAction(view));
		
		inputMap.put(StopAction.KS, StopAction.TXT);
		actionMap.put(StopAction.TXT, new StopAction(view));
		
		view.setActionMap(actionMap);
		view.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
	}
	
	private Rectangle2D calculateTimeRect(Graphics g) {
		if(g == null) {
			return new Rectangle2D.Double(0, 0, 0, DEFAULT_TIMEBAR_HEIGHT);
		} else {
			final FontMetrics fm = g.getFontMetrics(view.getFont());
			final String testString = "000:00.000";
			return fm.getStringBounds(testString, g);
		}
	}
	
	private double calculateTimeBarHeight(Graphics g) {
		final Rectangle2D bounds = calculateTimeRect(g);
		
		return bounds.getHeight() + 
				 2 * (1 + 2); // border plus spacing
	}
	
	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension retVal = new Dimension();
		
		double timeBarHeight = calculateTimeBarHeight(view.getGraphics());
		double channels = 
				numberOfVisibleChannels() * DEFAULT_CHANNEL_HEIGHT;
		
		retVal.setSize(DEFAULT_PIXEL_PER_SEC * view.getWindowLength(), timeBarHeight + channels);
		
		return retVal;
	}
	
	public int numberOfVisibleChannels() {
		int numVisibleChannels = 0;
		if(view.getSampled() != null) {
			numVisibleChannels = view.getSampled().getNumberOfChannels();
			for(int i = 0; i < view.getSampled().getNumberOfChannels(); i++) {
				if(!view.isChannelVisible(Channel.values()[i])) --numVisibleChannels;
			}
		}
		return numVisibleChannels;
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		// draw background
		if(view.isOpaque()) {
			final Color bg = view.getBackground();
			g.setColor(bg);
			g.fillRect(0, 0, view.getWidth(), view.getHeight());
		}
		
		if(view.getSampled() == null) return;
		
		final Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		
		if(timeBarHeight == 0.0) {
			timeBarHeight = calculateTimeBarHeight(g2);
		}
		final Rectangle2D timeBarRect = new Rectangle2D.Double(0.0, 1.0, view.getWidth(), timeBarHeight);
		contentRect = new Rectangle2D.Double(0.0, timeBarHeight + 2, view.getWidth(),
				view.getHeight() - timeBarHeight);
		
		final Line2D topLine = new Line2D.Double(timeBarRect.getX(), timeBarRect.getY(),
				timeBarRect.getX() + timeBarRect.getWidth(), timeBarRect.getY());
		final Line2D btmLine = new Line2D.Double(timeBarRect.getX(), timeBarRect.getY() + timeBarRect.getHeight(),
				timeBarRect.getX() + timeBarRect.getWidth(), timeBarRect.getY() + timeBarRect.getHeight());
		
		final FontMetrics fm = g2.getFontMetrics(view.getFont());
		g2.setFont(view.getFont());
		
		final Color borderColor = view.getForeground();
		g2.setColor(borderColor);
		g2.draw(topLine);
		g2.draw(btmLine);

		int numVisibleChannels = numberOfVisibleChannels();
		
		// draw excluded ranges
		final double pixelsPerSecond = (double)view.getWidth() / (double)view.getWindowLength();
		final float segStart = view.getSegmentStart();
		final float segLen = view.getSegmentLength();
		final float segEnd = segStart + segLen;
		
		final Stroke origStroke = g2.getStroke();
		final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
		g2.setStroke(dashed);
		if(view.hasSegment()) {
			// left excluded rect
			if(segStart > view.getWindowStart()) {
				final double x = 0.0;
				final double y = contentRect.getY();
				final double height = view.getHeight();
				final double width = (view.getSegmentStart() - view.getWindowStart()) * pixelsPerSecond;
				
				final Rectangle2D excludedRect = new Rectangle2D.Double(x, y, width, height);
				g2.setColor(view.getExcludedColor());
				g2.fill(excludedRect);
				
				final Line2D excludedBoundary = new Line2D.Double(x+width, y, x+width, height);
				g2.setColor(view.getExcludedColor().darker());
				g2.draw(excludedBoundary);
			}
			
			// right excluded rect
			if(segEnd < view.getWindowStart() + view.getWindowLength()) {
				final double height = view.getHeight();
				final double width = ((view.getWindowStart() + view.getWindowLength()) - segEnd) * pixelsPerSecond;
				final double x = view.getWidth() - width;
				final double y = contentRect.getY();
				
				final Rectangle2D excludedRect = new Rectangle2D.Double(x, y, width, height);
				g2.setColor(view.getExcludedColor());
				g2.fill(excludedRect);
				
				final Line2D excludedBoundary = new Line2D.Double(x, y, x, height);
				g2.setColor(view.getExcludedColor().darker());
				g2.draw(excludedBoundary);
			}
		}
		
		// draw selection
		if(view.hasSelection()) {
			final double startX = view.modelToView(view.getSelectionStart());
			final double endX = view.modelToView(view.getSelectionStart() + view.getSelectionLength());
			
			final Rectangle2D selRect = new Rectangle2D.Double(startX, contentRect.getY(),
					endX - startX, contentRect.getHeight());
			g2.setColor(view.getSelectionColor());
			g2.fill(selRect);
			
			
			// draw selection lines
			final Line2D startLine = new Line2D.Double(startX,
					contentRect.getY(), startX, contentRect.getY() + contentRect.getHeight()
					);
			final Line2D endLine = new Line2D.Double(endX, contentRect.getY(),
					endX, contentRect.getY() + contentRect.getHeight());
			g2.setColor(view.getSelectionColor().darker());
			g2.draw(startLine);
			g2.draw(endLine);
		}
		
		// draw channels
		final double channelHeight = contentRect.getHeight() / (double)numVisibleChannels;
		for(int i = 0, displayed = 0; i < view.getSampled().getNumberOfChannels(); i++) {
			if(!view.isChannelVisible(Channel.values()[i])) continue;
			
			final double y = contentRect.getY() + (displayed * channelHeight);
			final Rectangle2D channelRect = new Rectangle2D.Double(
					0, y, (double)view.getWidth(), channelHeight);
			SampledPainter painter = channelPainters.get(Channel.values()[i]);
			if(painter == null) {
				painter = new SampledPainter(i, view.getChannelColor(Channel.values()[i]));
				channelPainters.put(Channel.values()[i], painter);
			}
			painter.setWindowStart(view.getWindowStart());
			painter.setWindowLength(view.getWindowLength());
			painter.paint(view.getSampled(), g2, channelRect);
			
			displayed++;
		}
		
		// cursor
		if(!view.isPlaying() && view.getCursorPosition() >= 0) {
			g2.setStroke(dashed);
			final Line2D cursorLine = new Line2D.Double(
					view.getCursorPosition(), contentRect.getY(),
					view.getCursorPosition(), contentRect.getY() + contentRect.getHeight());
			g2.draw(cursorLine);
		}
		
		if(view.isPlaying() && view.getPlaybackMarker() > view.getWindowStart()
				&& view.getPlaybackMarker() < (view.getWindowStart() + view.getWindowLength())) {
			g2.setStroke(dashed);
			double playbackPos = view.modelToView(view.getPlaybackMarker());
			final Line2D playbackLine = new Line2D.Double(
					playbackPos, contentRect.getY(),
					playbackPos, contentRect.getY() + contentRect.getHeight());
			g2.draw(playbackLine);
		}
		
		// draw segement time value
		final double txtBaseY = timeBarHeight - 2;

		final String startTimeTxt = MsFormatter.msToDisplayString(
				Math.round(view.getSegmentStart() * 1000.0f));
		final Rectangle2D startTimeRect = fm.getStringBounds(startTimeTxt, g2);
		final double startTimeX = view.modelToView(view.getSegmentStart()) - startTimeRect.getCenterX();
		startTimeRect.setRect(startTimeX, txtBaseY - startTimeRect.getHeight(), 
				startTimeRect.getWidth(), startTimeRect.getHeight());
		
		final String endTimeTxt = MsFormatter.msToDisplayString(
				Math.round((view.getSegmentStart() + view.getSegmentLength()) * 1000.0f));
		final Rectangle2D endTimeRect = fm.getStringBounds(endTimeTxt, g2);
		final double endTimeX = view.modelToView(view.getSegmentStart() + view.getSegmentLength())
				- endTimeRect.getCenterX();
		endTimeRect.setRect(endTimeX, txtBaseY - endTimeRect.getHeight(),
				endTimeRect.getWidth(), endTimeRect.getHeight());

		final String selStartTxt = MsFormatter.msToDisplayString(
				Math.round(view.getSelectionStart() * 1000.0f));
		final Rectangle2D selStartRect = fm.getStringBounds(selStartTxt, g2);
		final double selTimeX = view.modelToView(view.getSelectionStart()) - selStartRect.getCenterX();
		selStartRect.setRect(selTimeX, txtBaseY - selStartRect.getHeight(), 
				selStartRect.getWidth(), selStartRect.getHeight());
		
		final String selEndTxt = MsFormatter.msToDisplayString(
				Math.round((view.getSelectionStart() + view.getSelectionLength()) * 1000.0f));
		final Rectangle2D selEndRect = fm.getStringBounds(selEndTxt, g2);
		final double selEndX = view.modelToView(view.getSelectionStart() + view.getSelectionLength())
				- selEndRect.getCenterX();
		selEndRect.setRect(selEndX, txtBaseY - selEndRect.getHeight(),
				selEndRect.getWidth(), selEndRect.getHeight());
		
		final Rectangle2D selRect = new Rectangle2D.Double();
		Rectangle2D.Double.union(selStartRect, selEndRect, selRect);
		
		final String lengthTxt = MsFormatter.msToDisplayString(
				Math.round(view.getSelectionLength() * 1000.0f));
		final Rectangle2D lengthRect = fm.getStringBounds(lengthTxt, g2);
		final double lengthX = selRect.getCenterX() - lengthRect.getCenterX();
		lengthRect.setRect(lengthX, txtBaseY- lengthRect.getHeight(),
				lengthRect.getWidth(), lengthRect.getHeight());
		
		final String cursorTxt = MsFormatter.msToDisplayString(
				view.getCursorPosition() > 0 ? 
				Math.round(view.viewToModel(view.getCursorPosition()) * 1000.0f)
				: 0L);
		boolean drawCursor = view.getCursorPosition() > 0;
		final Rectangle2D cursorRect = fm.getStringBounds(cursorTxt, g2);
		final double cursorX = view.getCursorPosition() - cursorRect.getCenterX();
		cursorRect.setRect(cursorX, txtBaseY-cursorRect.getHeight(),
				cursorRect.getWidth(), cursorRect.getHeight());
		
		g2.setColor(view.getForeground());
		
		boolean drawStartTime = true;
		if(view.hasSelection() && startTimeRect.intersects(selRect))
			drawStartTime = false;
		if(drawStartTime) {
			g2.drawString(startTimeTxt, (float)startTimeX, (float)(txtBaseY - fm.getDescent()));
		}

		boolean drawEndTime = true;
		if(view.hasSelection() && endTimeRect.intersects(selRect))
			drawEndTime = false;
		if(drawEndTime)
			g2.drawString(endTimeTxt, (float)endTimeX, (float)(txtBaseY - fm.getDescent()));
		
		if(view.hasSelection()) {
			g2.drawString(selStartTxt, (float)selTimeX, (float)(txtBaseY - fm.getDescent()));
			g2.drawString(selEndTxt, (float)selEndX, (float)(txtBaseY - fm.getDescent()));
			if(!lengthRect.intersects(selStartRect) && !lengthRect.intersects(selEndRect)) {
				g2.setColor(view.getSelectionColor());
				g2.drawString(lengthTxt, (float)lengthX, (float)(txtBaseY - fm.getDescent()));
			}
		}
		
		g2.setColor(view.getForeground());
		if(drawCursor)
			g2.drawString(cursorTxt, (float)cursorX, (float)(txtBaseY-fm.getDescent()));
	}
	
//	private void showContextMenu(Point p) {
//		final JMenu menu = new JMenu();
//		addContextMenuItems(menu);
//		menu.getPopupMenu().show(view, p.x, p.y);
//	}
	
	@Override
	public void addContextMenuItems(JMenu menu) {
		for(int i = 0; i < view.getSampled().getNumberOfChannels(); i++) {
			final Channel ch = Channel.values()[i];
			final ToggleChannelVisible toggleAct = new ToggleChannelVisible(view, ch);
			final JCheckBoxMenuItem menuItm = new JCheckBoxMenuItem(toggleAct);
			menu.add(menuItm);
		}

		menu.addSeparator();
		if(!view.isPlaying()) {
			final JMenuItem playSegmentItem = new JMenuItem(new PlaySegmentAction(view));
			playSegmentItem.setEnabled(view.hasSegment());
			menu.add(playSegmentItem);
			final JMenuItem playSelectionItem = new JMenuItem(new PlaySelectionAction(view));
			playSelectionItem.setEnabled(view.hasSelection());
			menu.add(playSelectionItem);
			final JCheckBoxMenuItem loopItem = new JCheckBoxMenuItem(new ToggleLoop(view));
			menu.add(loopItem);
			// output device selection
			final JMenu mixerMenu = new JMenu("Output Device");
			final Info[] mixers = AudioSystem.getMixerInfo();
			for(Info mixerInfo:mixers) {
				// if we have no source lines, we can't use this device
				if(AudioSystem.getMixer(mixerInfo).getSourceLineInfo().length == 0) continue;
				final SelectMixerAction mixerAct = new SelectMixerAction(view, mixerInfo);
				mixerAct.putValue(SelectMixerAction.SELECTED_KEY,
						view.getMixerInfo() == mixerInfo);
				mixerMenu.add(new JCheckBoxMenuItem(mixerAct));
			}
			menu.add(mixerMenu);
		} else {
			menu.add(new StopAction(view));
		}
		
		menu.addSeparator();
		final JMenuItem saveSegmentItem = new JMenuItem(new SaveSegmentAction(view));
		saveSegmentItem.setEnabled(view.hasSegment());
		menu.add(saveSegmentItem);
		final JMenuItem saveSelectionItem = new JMenuItem(new SaveSelectionAction(view));
		saveSelectionItem.setEnabled(view.hasSelection());
		menu.add(saveSelectionItem);
	}
	
	private final PropertyChangeListener propListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(!view.isValuesAdjusting()) {
				if(evt.getPropertyName().equals(PCMSegmentView.CURSOR_LOCATION_PROP)) {
					final int oldVal = (Integer)evt.getOldValue();
					final int newVal = (Integer)evt.getNewValue();
					final Rectangle2D txtRect = calculateTimeRect(view.getGraphics());
					
					final double x = Math.min(oldVal, newVal) - (txtRect.getWidth()/2.0) - 2;
					final double xmax = Math.max(oldVal, newVal) + (txtRect.getWidth()/2.0) + 2;
					final Rectangle clipRect = 
							new Rectangle((int)Math.round(x), 0, (int)Math.round(xmax-x), view.getHeight());
					view.repaint(clipRect);
				} else if(evt.getPropertyName().equals(PCMSegmentView.PLAYBACK_MARKER_PROP)) {
					final float oldVal = (Float)evt.getOldValue();
					final float newVal = (Float)evt.getNewValue();
					
					final double oldX = Math.max(0, view.modelToView(oldVal) - 1);
					final double newX = Math.min(view.getWidth(), view.modelToView(newVal)+1);
					final Rectangle clipRect = 
							new Rectangle((int)oldX, 0, (int)newX, view.getHeight());
					view.repaint(clipRect);
				} else if(evt.getPropertyName().equals(PCMSegmentView.SAMPLED_PROP)) {
					channelPainters.clear();
					view.repaint();
				} else {
					view.repaint();
				}
			}
		}
		
	};
	
	private final KeyListener keyListener = new KeyListener() {
		
		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
				view.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
				view.setCursor(magnifyCursor);
			}
		}
	};
	
	private final MouseInputAdapter mouseHandler = new MouseInputAdapter() {
		
		private boolean isDraggingSelection = false;
		
		private boolean isDraggingPosition = false;
		private int dragStartX = 0;
		
		@Override
		public void mouseExited(MouseEvent e) {
			// clear cursor position
			view.setCursorPosition(-1);
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			if(!isDraggingSelection) {
				view.setCursorPosition(e.getX());
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			view.requestFocus();
//			if(OSInfo.isMacOs() && e.isPopupTrigger())
//				showContextMenu(e.getPoint());
			if(e.getButton() != MouseEvent.BUTTON1) return;
			dragStartX = e.getX();
			if(e.getY() > contentRect.getY()) {
				final int x = e.getX();
				final float time = view.viewToModel(x);
				view.setValuesAdusting(true);
				view.setSelectionStart(time);
				view.setValuesAdusting(false);
				view.setSelectionLength(0.0f);
				view.setCursorPosition(-1);
				isDraggingSelection = true;
			} else {
				isDraggingPosition = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(!OSInfo.isMacOs() && e.isPopupTrigger()) {
//				showContextMenu(e.getPoint());
			} else {
				isDraggingSelection = false;
				isDraggingPosition = false;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(isDraggingSelection) {
				final int x = e.getX();
				final float time = view.viewToModel(x);
				final float dragStartTime = view.viewToModel(dragStartX);
				
				final float startValue = 
						Math.min(time, dragStartTime);
				final float endValue = 
						Math.max(time, dragStartTime);
				final float len = endValue - startValue;
				
				view.setValuesAdusting(true);
				view.setSelectionStart(startValue);
				view.setValuesAdusting(false);
				view.setSelectionLength(len);
			} else if(isDraggingPosition) {
				final int x = e.getX();
				final int diff = x - dragStartX;
				final float change = diff * 0.1f;
				dragStartX = x;
				
				float newStart = view.getWindowStart() + change;
				if((newStart + view.getWindowLength()) > (view.getSampled().getStartTime() + view.getSampled().getLength())) {
					newStart = (view.getSampled().getStartTime()
							 + view.getSampled().getLength()) - view.getWindowLength();
				}
				if(newStart < view.getSampled().getStartTime()) newStart = view.getSampled().getStartTime();
				view.setWindowStart(newStart);
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if((e.getModifiers() & KeyEvent.CTRL_MASK) > 0) {
				final int numTicks = e.getWheelRotation();
				
				final float zoomAmount = 0.25f * numTicks;
				float windowLength = view.getWindowLength() + zoomAmount;
				
				if(windowLength < 0.1f) {
					windowLength = 0.1f;
				} else if(windowLength > view.getSampled().getLength()) {
					windowLength = view.getSampled().getLength();
				}
				
				if((view.getWindowStart()+windowLength) > (view.getSampled().getStartTime() + view.getSampled().getLength())) {
					float newStart = (view.getSampled().getStartTime()+view.getSampled().getLength()) - windowLength;
					
					if(newStart < view.getSampled().getStartTime()) {
						newStart = view.getSampled().getStartTime();
						windowLength = view.getSampled().getLength();
					}
					view.setWindowStart(newStart);
				}
				
				view.setWindowLength(windowLength);
			} else {
				// send wheel scroll to parent container
				if(e.getSource() instanceof Component) {
					final Component comp = (Component)e.getSource();
					if(comp.getParent() != null) {
						comp.getParent().dispatchEvent(e);
					}
				}
			}
		}
		
	};
	
}
