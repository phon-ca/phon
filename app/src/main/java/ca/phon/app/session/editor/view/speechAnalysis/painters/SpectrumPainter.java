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

import ca.hedlund.jpraat.binding.fon.Spectrum;
import ca.phon.ui.painter.BufferedPainter;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicReference;

public class SpectrumPainter extends BufferedPainter<Spectrum> implements PraatPainter<Spectrum> {

	@Override
	public void paintGarnish(Spectrum obj, Graphics2D g2d, Rectangle2D bounds, int location) {
		
	}

	@Override
	protected void paintBuffer(Spectrum spectrum, Graphics2D g2d, Rectangle2D bounds) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		
		if(spectrum == null) return;
		
		g2d.setColor(Color.white);
		g2d.fill(bounds);
		
		g2d.setColor(Color.black);
		
		double fmin = 0.0;
		double fmax = 0.0;
		double minimum = 0.0;
		double maximum = 0.0;
		
		boolean autoscaling = ( minimum >= maximum );
		
		final AtomicReference<Long> ifminRef = new AtomicReference<Long>(0L);
		final AtomicReference<Long> ifmaxRef = new AtomicReference<Long>(0L);
		long numSamples = spectrum.getWindowSamplesX(fmin, fmax, ifminRef, ifmaxRef);
		if(numSamples == 0) return;
		
		/**
		 * Pass 1 : compute power density
		 */
		int n = (int)(ifmaxRef.get() - ifminRef.get())+1;
		double[] yWC = new double[n];
		if(autoscaling) maximum = -1e308;
		int i = 0;
		for(long ifreq = ifminRef.get(); ifreq <= ifmaxRef.get(); ifreq++) {
			double y = spectrum.getValueAtSample(ifreq, 0, 2);
			if(autoscaling && y > maximum) maximum = y;
			yWC[i++] = y;
		}
		if(autoscaling) {
			double defaultDynamicRange_dB = 60.0;
			minimum = maximum - defaultDynamicRange_dB;
			if(minimum == maximum) {
				return;
			}
		}
		
		/**
		 * Pass 2 : clip
		 */
		i = 0;
		for(long ifreq = ifminRef.get(); ifreq <= ifmaxRef.get(); ifreq++,i++) {
			if(yWC[i] < minimum) yWC[i] = minimum;
			else if(yWC[i] > maximum) yWC[i] = maximum;
		}
		
		// TODO draw
	}

}
