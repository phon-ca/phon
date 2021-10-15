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
package ca.phon.media;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;

import ca.phon.media.TimeUIModel.*;

public class TimeComponentUI extends ComponentUI {
	
	/*
	 * Padding added to left and right of markers (px) to make
	 * them easier to drag.
	 */
	public final static int MARKER_PADDING = 5;
	
	private TimeComponent timeComp;
	
	private final MarkerMouseListener markerListener = new MarkerMouseListener();
	
	private float cancelDragTime = -1.0f;
	
	@Override
	public void installUI(JComponent c) {
		if(!(c instanceof TimeComponent))
			throw new IllegalArgumentException("Wrong class");
		super.installUI(c);
		
		this.timeComp = (TimeComponent)c;

		c.addMouseListener(markerListener);
		c.addMouseMotionListener(markerListener);
		timeComp.getTimeModel().addTimeUIModelListener(timeModelListener);
		
		for(var interval:timeComp.getTimeModel().getIntervals()) {
			interval.addPropertyChangeListener(intervalTimeListener);
		}
		for(var marker:timeComp.getTimeModel().getMarkers()) {
			marker.addPropertyChangeListener(markerTimeListener);
		}
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		
		c.removeMouseListener(markerListener);
		c.removeMouseMotionListener(markerListener);
		timeComp.getTimeModel().removeTimeUIModelListener(timeModelListener);

		for(Interval interval:timeComp.getTimeModel().getIntervals()) {
			interval.removePropertyChangeListener(intervalTimeListener);
		}

		for(Marker marker:timeComp.getTimeModel().getMarkers()) {
			marker.removePropertyChangeListener(markerTimeListener);
		}
		
		timeComp = null;
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		if(!(c instanceof TimeComponent))
			throw new IllegalArgumentException("Wrong  class");
		int prefHeight = 1;
		int prefWidth = timeComp.getTimeModel().getPreferredWidth();
		
		return new Dimension(prefWidth, prefHeight);
	}
	
	public TimeComponent getTimeComponent() {
		return this.timeComp;
	}
	
	public void paintMarker(Graphics2D g2, Marker marker) {
		if(!timeComp.isRepaintAll() && marker.getOwner() != null && marker.getOwner() != timeComp) return;
		if(marker.getTime() < 0) return;
		final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
		
		final TimeUIModel timeModel = timeComp.getTimeModel();
		var x = timeModel.xForTime(marker.getTime());
		
		var line = new Line2D.Double(x, 0, x, timeComp.getHeight());
		
		g2.setStroke(dashed);
		g2.setColor(marker.getColor());
		g2.draw(line);
	}
	
	public void paintInterval(Graphics2D g2, Interval interval, boolean paintBackground) {
		if(!timeComp.isRepaintAll() && interval.getOwner() != null && interval.getOwner() != timeComp) return;
		if(!interval.isVisible()) return;

		if(paintBackground) {
			g2.setColor(interval.getColor());
			
			double xmin = timeComp.xForTime(interval.getStartMarker().getTime());
			double xmax = timeComp.xForTime(interval.getEndMarker().getTime());
			
			Rectangle2D intervalRect = new Rectangle2D.Double(xmin, 0, xmax-xmin, timeComp.getHeight());
			g2.fill(intervalRect);
		}
		
		paintMarker(g2, interval.getStartMarker());
		paintMarker(g2, interval.getEndMarker());
	}
	
	/* Marker dragging */
	private TimeUIModel.Interval currentlyDraggedInterval = null;
	
	private TimeUIModel.Marker currentlyDraggedMarker = null;
	
	/**
	 * Begin drag with given {@link Marker}
	 * 
	 * @param marker
	 */
	public void beginDrag(Marker marker) {
		Toolkit.getDefaultToolkit().addAWTEventListener(dragCancelListener, AWTEvent.KEY_EVENT_MASK);
		currentlyDraggedMarker = marker;
		cancelDragTime = currentlyDraggedMarker.getTime();
		currentlyDraggedMarker.setValueAdjusting(true);
	}
	
	/**
	 * Begin drag with given {@link Interval} and {@link Marker}
	 * 
	 * @param interval
	 * @param marker
	 */
	public void beginDrag(Interval interval, Marker marker) {
		if(interval.getStartMarker() != marker && interval.getEndMarker() != marker)
			throw new IllegalArgumentException("Marker must have given interval as parent");
		currentlyDraggedInterval = interval;
		beginDrag(marker);
		currentlyDraggedInterval.setValueAdjusting(true);
	}
	
	/**
	 * Swap dragged marker for currently dragged interval.
	 * Has no effect if no interval is currently being dragged.
	 * 
	 *
	 */
	public void beginDragOtherIntervalMarker() {
		if(currentlyDraggedInterval == null) return;
		
		Marker currentMarker = this.currentlyDraggedMarker;
		Marker otherMarker = (currentMarker == currentlyDraggedInterval.getStartMarker() ? 
				currentlyDraggedInterval.getEndMarker() : currentlyDraggedInterval.getStartMarker());
		
		beginDrag(otherMarker);
		currentMarker.setTime(otherMarker.getTime());
	}
	
	/**
	 * End current drag
	 * 
	 */
	public void endDrag() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(dragCancelListener);
		if(currentlyDraggedInterval != null)
			currentlyDraggedInterval.setValueAdjusting(false);
		if(currentlyDraggedMarker != null)
			currentlyDraggedMarker.setValueAdjusting(false);
		currentlyDraggedInterval = null;
		currentlyDraggedMarker = null;
	}
	
	public void cancelDrag() {
		if(currentlyDraggedMarker != null && cancelDragTime >= 0) {
			currentlyDraggedMarker.setTime(cancelDragTime);
		}
		endDrag();
	}
	
	public Interval getCurrentlyDraggedInterval() {
		return this.currentlyDraggedInterval;
	}
	
	public Marker getCurrentlyDraggedMarker() {
		return this.currentlyDraggedMarker;
	}
	
	private final AWTEventListener dragCancelListener = new AWTEventListener() {
		
		@Override
		public void eventDispatched(AWTEvent event) {
			if(event instanceof KeyEvent) {
				KeyEvent ke = (KeyEvent)event;
				if(ke.getID() == KeyEvent.KEY_PRESSED && 
						ke.getKeyChar() == KeyEvent.VK_ESCAPE) {
					cancelDrag();
					((KeyEvent) event).consume();
				}
			}
		}
		
	};
	
	private TimeUIModelListener timeModelListener = new TimeUIModelListener() {

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			
		}

		@Override
		public void intervalAdded(Interval interval) {
			interval.addPropertyChangeListener(intervalTimeListener);
			timeComp.repaintInterval(interval);			
		}

		@Override
		public void intervalRemoved(Interval interval) {
			interval.removePropertyChangeListener(intervalTimeListener);
			timeComp.repaintInterval(interval);
		}

		@Override
		public void markerAdded(Marker marker) {
			marker.addPropertyChangeListener(markerTimeListener);
			timeComp.repaintMarker(marker);
		}

		@Override
		public void markerRemoved(Marker marker) {
			marker.removePropertyChangeListener(markerTimeListener);
			timeComp.repaintMarker(marker);
		}
		
	};
		
	private PropertyChangeListener intervalTimeListener = (e) -> {
		final Interval interval = (Interval)e.getSource();
		if(!timeComp.isRepaintAll() && interval.getOwner() != null && interval.getOwner() != timeComp) return;
		if(!interval.isRepaintOnTimeChange()) return;
		
		synchronized(interval) {
			if(e.getPropertyName().endsWith(".time")) {
				boolean movingStartMarker = e.getPropertyName().startsWith("startMarker");
				
				float oldTime = (float)e.getOldValue();
				float newTime = (float)e.getNewValue();
				
				if(interval.isRepaintEntireInterval()) {
					int oldStartX = movingStartMarker
							? (int)Math.round(timeComp.getTimeModel().xForTime(oldTime))
							: (int)Math.round(timeComp.getTimeModel().xForTime(interval.getStartMarker().getTime()));
							
					int newStartX = movingStartMarker
							? (int)Math.round(timeComp.getTimeModel().xForTime(newTime)) 
							: oldStartX;
							
					int oldEndX = movingStartMarker
							? (int)Math.round(timeComp.getTimeModel().xForTime(interval.getEndMarker().getTime()))
							: (int)Math.round(timeComp.getTimeModel().xForTime(oldTime));
							
					int newEndX = movingStartMarker
							? oldEndX
							: (int)Math.round(timeComp.getTimeModel().xForTime(newTime));
					
					Rectangle oldIntervalRect = new Rectangle(
							Math.max(0, oldStartX-2), 0, oldEndX-oldStartX + 4, timeComp.getHeight());
					Rectangle newIntervalRect = new Rectangle(
							Math.max(0, newStartX-2), 0, newEndX-newStartX + 4, timeComp.getHeight());
					Rectangle clipRect = oldIntervalRect.union(newIntervalRect);
					timeComp.repaint(clipRect);
				} else {
					timeComp.repaint(Math.min(oldTime, newTime), Math.max(oldTime, newTime));
				}
			} else if(e.getPropertyName().contentEquals("color")) {
				timeComp.repaintInterval(interval);
			}
		}
	};
	
	private PropertyChangeListener markerTimeListener = (e) -> {
		Marker marker = (Marker)e.getSource();
		if(!timeComp.isRepaintAll() && marker.getOwner() != null && marker.getOwner() != timeComp) return;
		if(!marker.isRepaintOnTimeChange()) return;
		
		if("time".equals(e.getPropertyName()) || e.getPropertyName().endsWith(".time")) {
			float oldTime = (float)e.getOldValue();
			float newTime = (float)e.getNewValue();
			
			var oldX = (int)Math.round(timeComp.getTimeModel().xForTime(oldTime));
			var newX = (int)Math.round(timeComp.getTimeModel().xForTime(newTime));
			
			Rectangle clipRect = new Rectangle(
					(newTime < oldTime ? newX : oldX )-1,
					0,
					(newTime < oldTime ? oldX - newX : newX - oldX)+2,
					timeComp.getHeight());
			timeComp.repaint(clipRect);
		}
	};
	
	private class MarkerMouseListener extends MouseInputAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			var p = e.getPoint();
			
			if(getCurrentlyDraggedInterval() != null
					|| getCurrentlyDraggedMarker() != null) {
				endDrag();
			}
			
			for(Interval interval:timeComp.getTimeModel().getIntervals()) {
				if(!timeComp.isRepaintAll() && interval.getOwner() != null && interval.getOwner() != timeComp) continue;
				int startX = (int)Math.round(timeComp.xForTime(interval.getStartMarker().getTime()));
				int endX = (int)Math.round(timeComp.xForTime(interval.getEndMarker().getTime()));
	
				boolean insideStartMarker = interval.getStartMarker().isDraggable() && (p.x >= startX - MARKER_PADDING && p.x <= startX + MARKER_PADDING);
				boolean insideEndMarker = interval.getEndMarker().isDraggable() && (p.x >= endX - MARKER_PADDING && p.x <= endX + MARKER_PADDING);
						
				if(insideStartMarker && !insideEndMarker) {
					beginDrag(interval, interval.getStartMarker());
				} else if(!insideStartMarker && insideEndMarker) {
					beginDrag(interval, interval.getEndMarker());
				} else if(insideStartMarker && insideEndMarker) {
					// choose the closest
					var ds = Math.abs(p.x - startX);
					var de = Math.abs(p.x - endX);
					if(ds <= de) {
						beginDrag(interval, interval.getStartMarker());
					} else {
						beginDrag(interval, interval.getEndMarker());
					}
				}
			}
			
			if(getCurrentlyDraggedMarker() == null) {
				for(Marker marker:timeComp.getTimeModel().getMarkers()) {
					if(!timeComp.isRepaintAll() && marker.getOwner() != null && marker.getOwner() != timeComp) continue;
					int x = (int)Math.round(timeComp.xForTime(marker.getTime()));
	
					if(marker.isDraggable() && p.x >= x - MARKER_PADDING && p.x <= x + MARKER_PADDING) {
						beginDrag(marker);
					}
				}
			}
		}
	
		@Override
		public void mouseReleased(MouseEvent e) {
			endDrag();
		}
	
		@Override
		public void mouseDragged(MouseEvent e) {
			if(currentlyDraggedMarker != null) {
				float newTime = timeComp.timeAtX(e.getX());
				float oldTime = currentlyDraggedMarker.getTime();
				int oldX = (int)Math.round(timeComp.xForTime(oldTime));
				
				if(currentlyDraggedInterval != null) {
					// ensure time values are within interval range
					if(currentlyDraggedInterval.getStartMarker() == currentlyDraggedMarker) {
						newTime = Math.min(newTime, currentlyDraggedInterval.getEndMarker().getTime());
						
						if(newTime >= currentlyDraggedInterval.getEndMarker().getTime()
								&& (newTime - oldTime >= 0)) {
							beginDragOtherIntervalMarker();
						}
					} else if(currentlyDraggedInterval.getEndMarker() == currentlyDraggedMarker) {
						newTime = Math.max(newTime, currentlyDraggedInterval.getStartMarker().getTime());
						
						if(newTime <= currentlyDraggedInterval.getStartMarker().getTime()
								&& (newTime - oldTime <= 0) ) {
							beginDragOtherIntervalMarker();
						}
					}
				}
				
				currentlyDraggedMarker.setTime(newTime);
				
				Rectangle clipRect = new Rectangle(
						(newTime < oldTime ? e.getX() : oldX )-1,
						0,
						(newTime < oldTime ? oldX - e.getX() : e.getX() - oldX)+2,
						timeComp.getHeight());
				timeComp.repaint(clipRect);
			}
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			var p = e.getPoint();
			
			for(Interval interval:timeComp.getTimeModel().getIntervals()) {
				if(!timeComp.isRepaintAll() && interval.getOwner() != null && interval.getOwner() != timeComp) continue;
				int startX = (int)Math.round(timeComp.xForTime(interval.getStartMarker().getTime()));
				int endX = (int)Math.round(timeComp.xForTime(interval.getEndMarker().getTime()));
	
				if(interval.getStartMarker().isDraggable() && (p.x >= startX - MARKER_PADDING && p.x <= startX + MARKER_PADDING) || 
						interval.getEndMarker().isDraggable() && (p.x >= endX - MARKER_PADDING && p.x <= endX + MARKER_PADDING)) {
					timeComp.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				} else {
					if(timeComp.getCursor() != timeComp.getDefaultCursor()) {
						timeComp.setCursor(timeComp.getDefaultCursor());
					}
				}
			}
			
			for(Marker marker:timeComp.getTimeModel().getMarkers()) {
				if(!timeComp.isRepaintAll() && marker.getOwner() != null && marker.getOwner() != timeComp) continue;
				int x = (int)Math.round(timeComp.xForTime(marker.getTime()));
	
				if(marker.isDraggable() && p.x >= x - MARKER_PADDING && p.x <= x + MARKER_PADDING) {
					timeComp.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				}
			}
		}
		
	}

}
