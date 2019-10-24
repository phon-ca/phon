package ca.phon.app.media;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;

import ca.phon.app.media.TimeUIModel.Interval;
import ca.phon.app.media.TimeUIModel.Marker;
import groovy.transform.Synchronized;

public class TimeComponentUI extends ComponentUI {
	
	/*
	 * Padding added to left and right of markers (px) to make
	 * them easier to drag.
	 */
	public final static int MARKER_PADDING = 5;
	
	private TimeComponent timeComp;
	
	private final MarkerMouseListener markerListener = new MarkerMouseListener();
	
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
	
	public void paintMarker(Graphics2D g2, Marker marker) {
		final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
		
		final TimeUIModel timeModel = timeComp.getTimeModel();
		var x = timeModel.xForTime(marker.getTime());
		
		var line = new Line2D.Double(x, 0, x, timeComp.getHeight());
		
		g2.setStroke(dashed);
		g2.setColor(marker.getColor());
		g2.draw(line);
	}
	
	public void paintInterval(Graphics2D g2, Interval interval) {
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
		currentlyDraggedInterval = null;
		currentlyDraggedMarker = marker;
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
		currentlyDraggedMarker = marker;
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
		
		currentlyDraggedMarker = otherMarker;
	}
	
	/**
	 * End current drag
	 * 
	 */
	public void endDrag() {
		if(currentlyDraggedInterval != null)
			currentlyDraggedInterval.setValueAdjusting(false);
		if(currentlyDraggedMarker != null)
			currentlyDraggedMarker.setValueAdjusting(false);
		currentlyDraggedInterval = null;
		currentlyDraggedMarker = null;
	}
	
	public Interval getCurrentlyDraggedInterval() {
		return this.currentlyDraggedInterval;
	}
	
	public Marker getCurrentlyDraggedMarker() {
		return this.currentlyDraggedMarker;
	}
	
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
							Math.max(0, oldStartX-1), 0, oldEndX-oldStartX + 2, timeComp.getHeight());
					Rectangle newIntervalRect = new Rectangle(
							Math.max(0, newStartX-1), 0, newEndX-newStartX + 2, timeComp.getHeight());
					Rectangle clipRect = oldIntervalRect.union(newIntervalRect);
					
					timeComp.repaint(clipRect);
				} else {
					timeComp.repaint(Math.min(oldTime, newTime), Math.max(oldTime, newTime));
				}
			}
		}
	};
	
	private PropertyChangeListener markerTimeListener = (e) -> {
		Marker marker = (Marker)e.getSource();
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
			
			for(Interval interval:timeComp.getTimeModel().getIntervals()) {
				int startX = (int)Math.round(timeComp.xForTime(interval.getStartMarker().getTime()));
				int endX = (int)Math.round(timeComp.xForTime(interval.getEndMarker().getTime()));

				boolean insideStartMarker = (p.x >= startX - MARKER_PADDING && p.x <= startX + MARKER_PADDING);
				boolean insideEndMarker = (p.x >= endX - MARKER_PADDING && p.x <= endX + MARKER_PADDING);
						
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
			
			for(Marker marker:timeComp.getTimeModel().getMarkers()) {
				int x = (int)Math.round(timeComp.xForTime(marker.getTime()));

				if(p.x >= x - MARKER_PADDING && p.x <= x + MARKER_PADDING) {
					beginDrag(marker);
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
								&& (newTime - oldTime > 0)) {
							beginDragOtherIntervalMarker();
						}
					} else if(currentlyDraggedInterval.getEndMarker() == currentlyDraggedMarker) {
						newTime = Math.max(newTime, currentlyDraggedInterval.getStartMarker().getTime());
						
						if(newTime <= currentlyDraggedInterval.getStartMarker().getTime()
								&& (newTime - oldTime < 0) ) {
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
				int startX = (int)Math.round(timeComp.xForTime(interval.getStartMarker().getTime()));
				int endX = (int)Math.round(timeComp.xForTime(interval.getEndMarker().getTime()));

				if((p.x >= startX - MARKER_PADDING && p.x <= startX + MARKER_PADDING) || 
						(p.x >= endX - MARKER_PADDING && p.x <= endX + MARKER_PADDING)) {
					timeComp.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				} else {
					if(timeComp.getCursor() != Cursor.getDefaultCursor()) {
						timeComp.setCursor(Cursor.getDefaultCursor());
					}
				}
			}
			
			for(Marker marker:timeComp.getTimeModel().getMarkers()) {
				int x = (int)Math.round(timeComp.xForTime(marker.getTime()));

				if(p.x >= x - MARKER_PADDING && p.x <= x + MARKER_PADDING) {
					timeComp.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				}
			}
		}
		
	}
	
}
