package ca.phon.media.sampled;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import ca.phon.ui.painter.Painter;

public class SampledPainter implements Painter<Sampled> {

	/**
	 * Paint color
	 */
	private Color foregroundColor;

	/**
	 * Determines number of bars to paint.  This is the x unit increment
	 * for each bar.
	 */
	private float xIncr = 0.25f;
	
	/**
	 * Size of bars
	 */
	private float barSize = 0.5f;
	
	/**
	 * Channel to paint
	 */
	private int channel = 0;
	
	/**
	 * Window (in seconds)
	 */
	private float windowStart;
	
	private float windowLength;
	
	/**
	 * Cached extrema values
	 */
	private double[][] extremaCache;
	
	private double maxValue;
	
	public SampledPainter() {
		super();
		this.foregroundColor = Color.black;
	}
	
	public SampledPainter(int channel) {
		this(channel, Color.black);
	}
	
	public SampledPainter(int channel, Color color) {
		super();
		this.foregroundColor = color;
		this.channel = channel;
	}
	
	public float getWindowStart() {
		return windowStart;
	}

	public void setWindowStart(float windowStart) {
		float oldVal = getWindowStart();
		this.windowStart = windowStart;
		if(oldVal != windowStart) {
			extremaCache = null;
		}
	}

	public float getWindowLength() {
		return windowLength;
	}

	public void setWindowLength(float windowLength) {
		float oldVal = getWindowLength();
		this.windowLength = windowLength;
		if(oldVal != windowLength) {
			extremaCache = null;
		}
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
		if(oldVal != xIncr) {
			extremaCache = null;
		}
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
		if(oldVal != channel) {
			extremaCache = null;
		}
	}
	
	public double[][] getExtremaCache() {
		return this.extremaCache;
	}

	private Dimension prevSize = new Dimension(0, 0);
	@Override
	public void paint(Sampled obj, Graphics2D g2, Rectangle2D bounds) {
		final double width = bounds.getWidth();
		final double height = bounds.getHeight();
		final double halfHeight = height / 2.0;
		final double refY = bounds.getY() + halfHeight;
		final Dimension currDimension = new Dimension((int)width, (int)height);
		
		final float startTime = getWindowStart();
		final float endTime = startTime + getWindowLength();
		final float length = getWindowLength();
		final float secondsPerPixel = (float)(length / width);
		
		final Stroke oldStroke = g2.getStroke();
		final Stroke stroke = new BasicStroke(getBarSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2.setStroke(stroke);
		
		final Color oldColor = g2.getColor();
		g2.setColor(getForegroundColor());
		
		double[][] extrema = getExtremaCache();
		boolean calculateExtrema = false;
		if(extrema == null || prevSize == null || !prevSize.equals(currDimension)) {
			calculateExtrema = true;
			extremaCache = new double[(int)(bounds.getWidth() * (1/getxIncr()))][];
			final double[] windowExtrema = obj.getWindowExtrema(getChannel(), startTime, endTime);
			maxValue = Math.max(Math.abs(windowExtrema[0]), Math.abs(windowExtrema[1]));
		}
		prevSize = currDimension;
		final double unitPerPixel = maxValue / halfHeight;
		int idx = 0;
		for(double x = bounds.getX(); x < bounds.getX() + width; x += getxIncr()) {
			float time = startTime + (float)((x - bounds.getX()) * secondsPerPixel);
			
			double[] sliceExtrema = 
					(calculateExtrema ? obj.getWindowExtrema(getChannel(), time, time + secondsPerPixel) : extremaCache[idx]);
			if(calculateExtrema) {
				extremaCache[idx] = sliceExtrema;
			}
			idx++;
			double ymin = refY - (sliceExtrema[0] / unitPerPixel);
			double ymax = refY - (sliceExtrema[1] / unitPerPixel);
			
			final Line2D line = new Line2D.Double(x, ymin, x, ymax);
			g2.draw(line);
		}
		
		g2.setStroke(oldStroke);
		g2.setColor(oldColor);
	}
}
