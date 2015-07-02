/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.media.sampled;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import ca.phon.ui.painter.BufferedPainter;

public class SampledPainter extends BufferedPainter<Sampled> {

	/**
	 * Paint color
	 */
	private Color foregroundColor;

	/**
	 * Determines number of bars to paint.  This is the x unit increment
	 * for each bar.
	 */
	private float xIncr = 1.0f;
	
	/**
	 * Size of bars
	 */
	private float barSize = 1.0f;
	
	/**
	 * Channel to paint
	 */
	private int channel = 0;
	
	/**
	 * Window (in seconds)
	 */
	private float windowStart;
	
	private float windowLength;
	
	private double maxValue;
	
	public SampledPainter() {
		this(0);
	}
	
	public SampledPainter(int channel) {
		this(channel, Color.black);
	}
	
	public SampledPainter(int channel, Color color) {
		super();
		this.foregroundColor = color;
		this.channel = channel;
		
		// by default, only repaint when width changes.  Scale height changes.
		setResizeMode(ResizeMode.REPAINT_ON_RESIZE);
		
		addPropertyChangeListener(repaintListener);
	}
	
	public float getWindowStart() {
		return windowStart;
	}

	public void setWindowStart(float windowStart) {
		float oldVal = getWindowStart();
		this.windowStart = windowStart;
		firePropertyChange("windowStart", oldVal, windowStart);
	}

	public float getWindowLength() {
		return windowLength;
	}

	public void setWindowLength(float windowLength) {
		float oldVal = getWindowLength();
		this.windowLength = windowLength;
		firePropertyChange("windowLength", oldVal, windowLength);
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public float getxIncr() {
		return xIncr;
	}

	public void setxIncr(float xIncr) {
		float oldVal = getxIncr();
		this.xIncr = xIncr;
		firePropertyChange("xIncr", oldVal, xIncr);
	}

	public float getBarSize() {
		return barSize;
	}

	public void setBarSize(float barSize) {
		this.barSize = barSize;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		int oldVal = getChannel();
		this.channel = channel;
		firePropertyChange("channel", oldVal, channel);
	}

	private Dimension prevSize = new Dimension(0, 0);
	@Override
	public void paintBuffer(Sampled obj, Graphics2D g2, Rectangle2D bounds) {
		final double width = bounds.getWidth();
		final double height = bounds.getHeight();
		final double halfHeight = height / 2.0;
		
		final float startTime = getWindowStart();
		final float endTime = startTime + getWindowLength();
		final float length = getWindowLength();
		final float secondsPerPixel = (float)(length / width);
		
		final Stroke stroke = new BasicStroke(getBarSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2.setStroke(stroke);
		
		double extrema[] = new double[2];
		final Line2D line = new Line2D.Double();
		
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
				RenderingHints.VALUE_STROKE_PURE);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setColor(getForegroundColor());
		
		maxValue = 0;
		for(int ch = 0; ch < obj.getNumberOfChannels(); ch++) {
			obj.getWindowExtrema(ch, startTime, endTime, extrema);
			maxValue = Math.max(maxValue, Math.max(Math.abs(extrema[0]), Math.abs(extrema[1])));
		}
		final double unitPerPixel = maxValue / halfHeight;
		
		double ymindiff, ymaxdiff = 0.0;
		float time = 0.0f;
		double ymin, ymax = halfHeight;
		
		for(double x = 0.0; x < width; x += getxIncr()) {
			time = startTime + (float)(x * secondsPerPixel);
			
			
			obj.getWindowExtrema(getChannel(), time, time + secondsPerPixel, extrema);
			
			ymindiff = Math.abs(extrema[0]) / unitPerPixel;
			ymaxdiff = Math.abs(extrema[1]) / unitPerPixel;
			
			ymin = halfHeight;
			if(extrema[0] < 0) {
				ymin += ymindiff;
			} else {
				ymin -= ymindiff;
			}
			
			ymax = halfHeight;
			if(extrema[1] < 0) {
				ymax += ymaxdiff;
			} else {
				ymax -= ymaxdiff;
			}
			
			line.setLine(x, ymin, x, ymax);
			g2.draw(line);
		}
	}
	
	private final PropertyChangeListener repaintListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			setRepaintBuffer(true);
		}
	};
}
