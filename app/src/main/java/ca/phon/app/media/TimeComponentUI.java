package ca.phon.app.media;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import ca.phon.app.media.TimeUIModel.Interval;
import ca.phon.app.media.TimeUIModel.Marker;

public class TimeComponentUI extends ComponentUI {
	
	private TimeComponent timeComp;

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
		var x = timeModel.xForTime(marker.time);
		
		var line = new Line2D.Double(x, 0, x, timeComp.getHeight());
		
		g2.setStroke(dashed);
		g2.setColor(Color.darkGray);
		g2.draw(line);
	}
	
	public void paintInterval(Graphics2D g2, Interval interval) {
		paintMarker(g2, interval.startMarker);
		paintMarker(g2, interval.endMarker);
	}
	
}
