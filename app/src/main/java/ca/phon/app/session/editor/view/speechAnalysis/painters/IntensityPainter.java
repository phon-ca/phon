/*
 * Copyright (C) 2012-2018 Gregory Hedlund
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
package ca.phon.app.session.editor.view.speechAnalysis.painters;

import ca.hedlund.jpraat.binding.fon.Intensity;
import ca.phon.app.session.editor.view.speechAnalysis.IntensitySettings;
import ca.phon.ui.painter.BufferedPainter;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicReference;

public class IntensityPainter extends BufferedPainter<Intensity> implements PraatPainter<Intensity> {
	
	private final Color intensityColor = Color.YELLOW;
	
	private IntensitySettings settings = new IntensitySettings();

	public IntensityPainter() {
		this(new IntensitySettings());
	}
	
	public IntensityPainter(IntensitySettings settings) {
		super();
		super.setResizeMode(ResizeMode.REPAINT_ON_RESIZE);
		this.settings = settings;
	}
	
	public void setSettings(IntensitySettings settings) {
		this.settings = settings;
	}
	
	public IntensitySettings getSettings() {
		return this.settings;
	}
	
	@Override
	public void paintGarnish(Intensity intensity, Graphics2D g2d, Rectangle2D bounds, int location) {
		if(intensity == null) return;
		
		final NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		
		final FontMetrics fm = g2d.getFontMetrics(g2d.getFont());
		final double minIntensity = getSettings().getViewRangeMin();
		final double maxIntensity = getSettings().getViewRangeMax();
		final String suffix = " dB";
		final String minStr = nf.format(minIntensity) + suffix;
		final Rectangle2D minBounds = g2d.getFontMetrics().getStringBounds(minStr, g2d);
		final String maxStr = nf.format(maxIntensity) + suffix;
		final Rectangle2D maxBounds = g2d.getFontMetrics().getStringBounds(maxStr, g2d);
		
		int y = 
				(int)Math.round(
						(bounds.getY() + bounds.getHeight())) - fm.getDescent();
		g2d.setColor(intensityColor);
		g2d.drawString(minStr, (float)(bounds.getX() - minBounds.getWidth()),
				(float)(y));
		
		g2d.drawString(maxStr, (float)(bounds.getX() - maxBounds.getWidth()),
				(float)(bounds.getY() + maxBounds.getHeight()) - fm.getDescent());
	}
	
	protected void paintBuffer(Intensity intensity, Graphics2D g2d, Rectangle2D bounds) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		
		if(intensity == null) return;
		
		double tmin = intensity.getXMin();
		double tmax = intensity.getXMax();
		
		final double len = (tmax - tmin);
		if(len <= 0.0) return;
		
		double dbMin = settings.getViewRangeMin();
		double dbMax = settings.getViewRangeMax();
		double range = Math.abs(dbMax - dbMin);
		
		final double unitsPerPixel = range / bounds.getHeight();
		final double pixelPerSec = bounds.getWidth() / len;
		
		final AtomicReference<Long> iminRef = new AtomicReference<Long>();
		final AtomicReference<Long> imaxRef = new AtomicReference<Long>();
		
		intensity.getWindowSamples(tmin, tmax, iminRef, imaxRef);
		
		final int firstFrame = iminRef.get().intValue();
		final int lastFrame = imaxRef.get().intValue();
		
		g2d.setColor(intensityColor);
		
		double lastY = Double.POSITIVE_INFINITY;
		double lastX = Double.POSITIVE_INFINITY;
		for(int i = firstFrame; i <= lastFrame; i++) {
			double time = intensity.indexToX(i);
			double v = intensity.getValueAtSample(i, Intensity.AVERAGING_MEDIAN, Intensity.UNITS_DB);
			
			double x = bounds.getX()  + ((time - tmin) * pixelPerSec);
			double y = (bounds.getY() + bounds.getHeight()) - ((v-dbMin) / unitsPerPixel);
			
			if(lastX != Double.POSITIVE_INFINITY && lastY != Double.POSITIVE_INFINITY) {
				final Line2D line = new Line2D.Double(lastX, lastY, x, y);
				g2d.draw(line);
			}
			lastX = x;
			lastY = y;
		}
	}

}
