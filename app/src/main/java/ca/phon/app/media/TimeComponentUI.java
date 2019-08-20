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

public class TimeComponentUI extends ComponentUI {
	
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
		g2.setColor(Color.darkGray);
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
			interval.addPropertyChangeListener(markerTimeListener);
			repaintInterval(interval);			
		}

		@Override
		public void intervalRemoved(Interval interval) {
			interval.removePropertyChangeListener(markerTimeListener);
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

				if(p.x >= startX - 1 && p.x <= startX + 1) {
					currentlyDraggedInterval = interval;
					currentlyDraggedMarker = interval.getStartMarker();
					currentlyDraggedInterval.setValueAdjusting(true);
				} else if(p.x >= endX - 1 && p.x <= endX + 1) {
					currentlyDraggedInterval = interval;
					currentlyDraggedMarker = interval.getEndMarker();
					currentlyDraggedInterval.setValueAdjusting(true);
				}
			}
			
			for(Marker marker:timeComp.getTimeModel().getMarkers()) {
				int x = (int)Math.round(timeComp.xForTime(marker.getTime()));

				if(p.x >= x - 1 && p.x <= x + 1) {
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
				
				// TODO snapping
				// TODO sanity check new value
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

				if((p.x >= startX - 1 && p.x <= startX + 1) || 
						(p.x >= endX - 1 && p.x <= endX + 1)) {
					timeComp.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				} else {
					if(timeComp.getCursor() != Cursor.getDefaultCursor()) {
						timeComp.setCursor(Cursor.getDefaultCursor());
					}
				}
			}		
		}
		
	}
	
}
