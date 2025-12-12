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

import ca.hedlund.jpraat.binding.fon.*;
import ca.hedlund.jpraat.exceptions.PraatException;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.painter.BufferedPainter;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextGridPainter extends BufferedPainter<TextGrid> {
	
	private final static Logger LOGGER = Logger.getLogger(TextGridPainter.class.getName());
	
	private List<String> hiddenTiers = new ArrayList<>();

	public TextGridPainter() {
		super();
		setResizeMode(ResizeMode.REPAINT_ON_RESIZE);
	}
	
	public boolean isHidden(String tierName) {
		return hiddenTiers.contains(tierName);
	}
	
	public void setHidden(String tierName, boolean hidden) {
		if(hidden)
			hiddenTiers.add(tierName);
		else
			hiddenTiers.remove(tierName);
	}
	
	public void clearHiddenTiers() {
		this.hiddenTiers.clear();
	}

	@Override
	protected void paintBuffer(TextGrid obj, Graphics2D g2d, Rectangle2D bounds) {
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		double contentHeight = bounds.getHeight();
		double tierHeight = contentHeight / (obj.numberOfTiers() - hiddenTiers.size());
		
		g2d.setColor(Color.WHITE);
		g2d.fill(bounds);
		g2d.setFont(FontPreferences.getTierFont());
		
		int visibleTierIdx = 0;
		for(long tIdx = 1; tIdx <= obj.numberOfTiers(); tIdx++) {
			final Function tier = obj.tier(tIdx);
			if(isHidden(tier.getName())) continue;
			
			// tier rect
			final Rectangle2D tierRect = new Rectangle2D.Double(
					bounds.getX(), bounds.getY() + (visibleTierIdx*tierHeight),
					bounds.getWidth(), tierHeight);
			visibleTierIdx++;
			
			try {
				final IntervalTier intervalTier = obj.checkSpecifiedTierIsIntervalTier(tIdx);
				paintIntervalTier(intervalTier, g2d, tierRect);
			} catch (PraatException pe) {
				try {
					final TextTier pointTier = obj.checkSpecifiedTierIsPointTier(tIdx);
					paintPointTier(pointTier, g2d, tierRect);
				} catch (PraatException pe1) {
					LOGGER.log(Level.SEVERE, pe1.getLocalizedMessage(), pe1);
				}
			}
		}
	}
	
	public void paintTierLabel(Function tier, Graphics2D g2d, Rectangle2D bounds) {
		final String name = (tier.getName() != null ? tier.getName().toString() : "");
		if(name.length() > 0) {
			// get bounding rectangle of tier name
			Rectangle2D tierNameBounds = g2d.getFontMetrics().getStringBounds(name, g2d);
			
			// create a rounded rectangle
			Rectangle2D labelRect = new Rectangle2D.Double(bounds.getX(), bounds.getY(), tierNameBounds.getWidth() + 20, 
					tierNameBounds.getHeight());
			final Color tierBgColor = new Color(255, 255, 0, 120);
			g2d.setColor(tierBgColor);
			g2d.fill(labelRect);
			g2d.setColor(Color.darkGray);
			g2d.draw(labelRect);
			
			g2d.setColor(Color.black);
			g2d.drawString(name, (float)bounds.getX() + 10,
					(float)(bounds.getY() + (float)tierNameBounds.getHeight() - g2d.getFontMetrics().getDescent()) );
		}
	}
	
	public void paintIntervalTier(IntervalTier intervalTier, Graphics2D g2d, Rectangle2D bounds) {
		double contentWidth = bounds.getWidth();
		double tgLen = intervalTier.getXmax() - intervalTier.getXmin();
		double pxPerSec = contentWidth / tgLen;
		double xoffset = intervalTier.getXmin();
		
		for(long i = 1; i <= intervalTier.numberOfIntervals(); i++) {
			final TextInterval interval = intervalTier.interval(i);
			
			double startX = (interval.getXmin() - xoffset) * pxPerSec;
			double endX = (interval.getXmax() - xoffset) * pxPerSec;
			
			g2d.setColor(Color.DARK_GRAY);
			final Line2D startLine = new Line2D.Double(startX, bounds.getY(), startX, 
					bounds.getY() + bounds.getHeight());
			g2d.draw(startLine);
			
			final Line2D endLine = new Line2D.Double(endX, bounds.getY(),
					endX, bounds.getY() + bounds.getHeight());
			g2d.draw(endLine);
			
			final Rectangle2D labelRect = new Rectangle2D.Double(
					startX, bounds.getY(), endX - startX, bounds.getHeight());
			final String labelText = interval.getText();
			final Rectangle2D textBounds = 
					g2d.getFontMetrics().getStringBounds(labelText, g2d);
			
			g2d.setColor(Color.black);
			if(textBounds.getWidth() <= labelRect.getWidth()) {
				// center text
				double textX = labelRect.getCenterX() - textBounds.getCenterX();
				double textY = labelRect.getCenterY() - textBounds.getCenterY();
				g2d.drawString(labelText, (float)textX, (float)textY);
			}
		}		
	}
	
	public void paintPointTier(TextTier textTier, Graphics2D g2d, Rectangle2D bounds) {
		double contentWidth = bounds.getWidth();
		double tgLen = textTier.getXmax() - textTier.getXmin();
		double pxPerSec = contentWidth / tgLen;
		double xoffset = textTier.getXmin();
		
		for(long i = 1; i <= textTier.numberOfPoints(); i++) {
			final TextPoint tp = textTier.point(i);
			
			double lineX = (tp.getNumber() - xoffset) * pxPerSec;
			
			g2d.setColor(Color.DARK_GRAY);
			final Line2D pointLine = new Line2D.Double(lineX, bounds.getY(), lineX,
					bounds.getY() + bounds.getHeight());
			g2d.draw(pointLine);
			
			final Rectangle2D textBounds = 
					g2d.getFontMetrics().getStringBounds(tp.getText(), g2d);
			
			float x = (float)(lineX - textBounds.getCenterX());
			float y = (float)((bounds.getY() + (bounds.getHeight() / 2.0)) - (textBounds.getHeight()/2.0));

			g2d.setColor(Color.white);
			textBounds.setRect(x, y, textBounds.getWidth(), textBounds.getHeight());
			g2d.fill(textBounds);
			
			g2d.setColor(Color.black);
			g2d.drawString(tp.getText(), x, (float)(textBounds.getY() + textBounds.getHeight() - g2d.getFontMetrics().getDescent()));
		}
	}

}
