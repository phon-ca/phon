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
	
	private TimeUIModelListener timeModelListener = new TimeUIModelListener() {

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			
		}

		@Override
		public void intervalAdded(Interval interval) {
			interval.addPropertyChangeListener(intervalTimeListener);
			repaintInterval(interval);			
		}

		@Override
		public void intervalRemoved(Interval interval) {
			interval.removePropertyChangeListener(intervalTimeListener);
			repaintInterval(interval);
		}

		@Override
		public void markerAdded(Marker marker) {
			marker.addPropertyChangeListener(markerTimeListener);
			repaintMarker(marker);
		}

		@Override
		public void markerRemoved(Marker marker) {
			marker.removePropertyChangeListener(markerTimeListener);
			repaintMarker(marker);
		}
		
	};
	
	public void repaintInterval(Interval interval) {
		var startX = 
				Math.max(0, timeComp.xForTime(interval.getStartMarker().getTime()) - 1);
		var endX = 
				Math.min(timeComp.getWidth(), timeComp.xForTime(interval.getEndMarker().getTime()) + 1);
		
		Rectangle clipRect = new Rectangle(
				(int)startX, 0, (int)(endX-startX), timeComp.getHeight());
		timeComp.repaint(clipRect);
	}
	
	public void repaintMarker(Marker marker) {
		var markerX = timeComp.xForTime(marker.getTime());
		
		Rectangle clipRect = new Rectangle(
				(int)Math.max(0, markerX - 1), 0, 3, timeComp.getHeight());
		timeComp.repaint(clipRect);
	}
	
	private PropertyChangeListener intervalTimeListener = (e) -> {
		final Interval interval = (Interval)e.getSource();
		
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
		
		private TimeUIModel.Interval currentlyDraggedInterval = null;
		
		private TimeUIModel.Marker currentlyDraggedMarker = null;

		@Override
		public void mousePressed(MouseEvent e) {
			var p = e.getPoint();
			
			for(Interval interval:timeComp.getTimeModel().getIntervals()) {
				int startX = (int)Math.round(timeComp.xForTime(interval.getStartMarker().getTime()));
				int endX = (int)Math.round(timeComp.xForTime(interval.getEndMarker().getTime()));

				if(p.x >= startX - MARKER_PADDING && p.x <= startX + MARKER_PADDING) {
					currentlyDraggedInterval = interval;
					currentlyDraggedMarker = interval.getStartMarker();
					currentlyDraggedInterval.setValueAdjusting(true);
				} else if(p.x >= endX - MARKER_PADDING && p.x <= endX + MARKER_PADDING) {
					currentlyDraggedInterval = interval;
					currentlyDraggedMarker = interval.getEndMarker();
					currentlyDraggedInterval.setValueAdjusting(true);
				}
			}
			
			for(Marker marker:timeComp.getTimeModel().getMarkers()) {
				int x = (int)Math.round(timeComp.xForTime(marker.getTime()));

				if(p.x >= x - MARKER_PADDING && p.x <= x + MARKER_PADDING) {
					currentlyDraggedMarker = marker;
					currentlyDraggedMarker.setValueAdjusting(true);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(currentlyDraggedInterval != null)
				currentlyDraggedInterval.setValueAdjusting(false);
			if(currentlyDraggedMarker != null)
				currentlyDraggedMarker.setValueAdjusting(false);
			currentlyDraggedInterval = null;
			currentlyDraggedMarker = null;
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
					} else if(currentlyDraggedInterval.getEndMarker() == currentlyDraggedMarker) {
						newTime = Math.max(newTime, currentlyDraggedInterval.getStartMarker().getTime());
					}
				}
				
				// TODO snapping
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
