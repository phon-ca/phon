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
import java.beans.PropertyChangeListener;

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
		
		c.addMouseListener(markerListener);
		c.addMouseMotionListener(markerListener);
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		
		c.removeMouseListener(markerListener);
		c.removeMouseMotionListener(markerListener);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		if(!(c instanceof TimeComponent))
			throw new IllegalArgumentException("Wrong  class");
		this.timeComp = (TimeComponent)c;
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
		
	private class MarkerMouseListener extends MouseInputAdapter {
		
		private TimeUIModel.Interval currentlyDraggedInterval = null;
		
		private TimeUIModel.Marker currentlyDraggedMarker = null;

		@Override
		public void mousePressed(MouseEvent e) {
			var p = e.getPoint();
			
			for(Interval interval:timeComp.getTimeModel().getIntervals()) {
				int startX = (int)Math.round(timeComp.xForTime(interval.getStartMarker().getTime()));
				int endX = (int)Math.round(timeComp.xForTime(interval.getEndMarker().getTime()));

				if(p.x == startX) {
					currentlyDraggedInterval = interval;
					currentlyDraggedMarker = interval.getStartMarker();
				} else if(p.x == endX) {
					currentlyDraggedInterval = interval;
					currentlyDraggedMarker = interval.getEndMarker();
				} else {
					currentlyDraggedInterval = null;
					currentlyDraggedMarker = null;
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
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

				if(p.x == startX || p.x == endX) {
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
