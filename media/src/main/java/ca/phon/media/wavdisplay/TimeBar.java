/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.media.wavdisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;

import ca.phon.util.MsFormatter;

public class TimeBar extends JComponent {
	
	private final static Logger LOGGER = Logger.getLogger(TimeBar.class.getName());
	
	private static int _defaultMinorTick = 5;
	private static int _defaultMajorTick = 10;
	
	private static Font _timeFont;
	
	private WavDisplay _parent;
	
	private long startMs;
	
	private long endMs;
	
	private long currentMs = -1;
	
	private long segStart = -1;
	private long segLength = 0;
	
	private int _minorTick;
	private int _majorTick;
	
	public TimeBar(WavDisplay parent) {
		this(_defaultMinorTick, _defaultMajorTick, parent);
	}

	public TimeBar(int minorTick, int majorTick, WavDisplay parent) {
		_minorTick = minorTick;
		_majorTick = majorTick;
		_parent = parent;
		try {
//			_timeFont = Font.createFont(Font.PLAIN, new File("data/fonts/LiberationMono-Regular.ttf")).deriveFont(10.0f);
			_timeFont = Font.decode("monospace-10-PLAIN");
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(0, 50);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Dimension size = getSize();
		g.setColor(Color.white);
		g.fillRect(0, 0, size.width, size.height);
		
		g.setColor(Color.black);
		
		Graphics2D g2 = (Graphics2D)g;
		
		Color markerColor = new Color(125, 125, 125, 100);
		
		double majorTickSpacing = ((double)(size.width - 2 * WavDisplay._TIME_INSETS_) / (double)_majorTick);
		double minorTickSpacing = majorTickSpacing / _minorTick;
		double msPerPixel = (endMs - startMs) / (double)(size.width - 2 * WavDisplay._TIME_INSETS_);
		
		double lineHeight = size.height / 3.0;
		
		double lineMax = lineHeight + size.height/3.0;
		
		double xIndex = WavDisplay._TIME_INSETS_;
		double oldX = xIndex;
		for(int tickIndex = 0; tickIndex < _majorTick * _minorTick; tickIndex++) {
			Line2D line = null;
			if(tickIndex % _minorTick == 0) {
				line =
					new Line2D.Double(xIndex, lineHeight, xIndex, lineMax);
			} else {
				line =
					new Line2D.Double(xIndex, lineHeight, xIndex, size.height/2.0);
			}
			g2.draw(line);
			
			oldX = xIndex;
			xIndex += minorTickSpacing;
			Line2D refLine = 
				new Line2D.Double(oldX, lineHeight, xIndex, lineHeight);
			g2.draw(refLine);
		}
		Line2D lastTick = 
			new Line2D.Double(xIndex, lineHeight, xIndex, lineMax);
		g2.draw(lastTick);
		
		g2.setFont(_timeFont);
		
		g2.setColor(Color.gray);
		// draw time values
//		String startString = MsFormatter.msToDisplayString(startMs);
//		Rectangle2D startStringBounds = 
//			g2.getFontMetrics(_timeFont).getStringBounds(startString, g);
//		float startX = 
//			WavDisplay._TIME_INSETS_ - (float)(startStringBounds.getWidth()/2);
//		g2.drawString(startString, startX, (float)startStringBounds.getHeight());
//		
//		String endString = MsFormatter.msToDisplayString(endMs);
//		Rectangle2D endStringBounds = 
//			g2.getFontMetrics(_timeFont).getStringBounds(endString, g);
//		float endX = 
//			(size.width - WavDisplay._TIME_INSETS_) - (float)(endStringBounds.getWidth()/2.0);
//		g2.drawString(endString, endX, (float)endStringBounds.getHeight());
		
		// segment boundaries
		if(segStart >= 0) {
			double rectWidth = (segStart - startMs)  / msPerPixel;
			
			g2.setColor(new Color(200, 200, 200, 100));
			Rectangle2D segStartRect = 
				new Rectangle2D.Double(WavDisplay._TIME_INSETS_,
						lineHeight, rectWidth+1, getHeight());
			g2.fill(segStartRect);
			
			long endTime = segStart + segLength;
			long endLength = endMs - endTime;
			
			double segStartX = rectWidth + WavDisplay._TIME_INSETS_;
			rectWidth = endLength  / msPerPixel;
			double xPos = (getWidth() - WavDisplay._TIME_INSETS_) - rectWidth;
			
//			xOff = (double)(segStart + segLength) / msPerPixel;
//			
			Rectangle2D segEndRect = 
				new Rectangle2D.Double(xPos+1,
						lineHeight, xIndex - xPos, getHeight());
			g2.fill(segEndRect);
			
			// draw time values
			String segStartString = MsFormatter.msToDisplayString(segStart);
			String segEndString = MsFormatter.msToDisplayString(endTime);
			
			Color fadeColor = new Color(255, 255, 255, 180);
			Rectangle2D segStartBounds = 
				g2.getFontMetrics(_timeFont).getStringBounds(segStartString, g2);
//			double segStartX = (rectWidth+WavDisplay._TIME_INSETS_)-(segStartBounds.getWidth()/2.0);
			segStartX -= segStartBounds.getWidth()/2.0;
			double segEndX = xPos - (segStartBounds.getWidth()/2.0);
			double yVal = segStartBounds.getHeight();
			g2.setColor(fadeColor);
			
			Rectangle2D fadeRect = 
				new Rectangle2D.Double(segStartX, yVal-segStartBounds.getHeight(), 
					segStartBounds.getWidth(), segStartBounds.getHeight());
			g2.fill(fadeRect);
			g2.setColor(Color.black);
			g2.drawString(segStartString, (float)segStartX, 
					(float)(yVal));
			
			g2.setColor(fadeColor);
			fadeRect.setRect(segEndX, yVal-segStartBounds.getHeight(), segStartBounds.getWidth(), segStartBounds.getHeight());
			g2.setColor(Color.black);
			g2.drawString(segEndString, (float)segEndX,
					(float)(yVal));
			
		}
		
		// draw selection
//		if(_parent.get_selectionStart() > 0
//				&& _parent.get_selectionEnd() > 0) {
//			int selLen = Math.abs(_parent.get_selectionEnd() - _parent.get_selectionStart());
//			int xPos = Math.min(_parent.get_selectionStart(), _parent.get_selectionEnd());
//			
//			Color selColor = new Color(50, 125, 200, 100);
//			g2.setColor(selColor);
//			
//			//ouble rectWidth = (double)selLen / msPerPixel;
//			Rectangle2D selRect = 
//				new Rectangle2D.Double((double)xPos, lineHeight, (double)selLen, (double)getHeight());
//			g2.fill(selRect);
//		}
		
		if(_parent.get_selectionStart() >= 0
				&& _parent.get_selectionEnd() >= 0) {
			Color selColor = new Color(50, 125, 200, 100);
			g2.setColor(selColor);
			
			// convert time values to x positions
			double startXPos = _parent.get_selectionStart() / msPerPixel;
			double endXPos = _parent.get_selectionEnd() / msPerPixel;
			double xPos = 
				Math.min(startXPos, endXPos) + WavDisplay._TIME_INSETS_;
			double rectLen = 
				Math.abs(endXPos - startXPos);
			
			Rectangle2D selRect =
				new Rectangle2D.Double(xPos, lineHeight,
						rectLen, getHeight());
			g2.fill(selRect);
			
			// text
			final double duration =
					(Math.max(_parent.get_selectionEnd(), _parent.get_selectionStart())) - 
					(Math.min(_parent.get_selectionStart(), _parent.get_selectionEnd()));
			final String durationTxt = MsFormatter.msToDisplayString((long)duration);
			final Rectangle2D durationBounds = g2.getFontMetrics().getStringBounds(durationTxt, g2);
			double durationTxtX = xPos;
			if(durationBounds.getWidth() < selRect.getWidth()) {
				durationTxtX = selRect.getCenterX() - durationBounds.getCenterX();
			}
			g2.drawString(durationTxt, (float)durationTxtX, (float)durationBounds.getHeight());
		}
		
		// draw currentms string
		if(currentMs >= 0) {
			String currentString = MsFormatter.msToDisplayString(currentMs+startMs);
			Rectangle2D currentStringBounds =
				g2.getFontMetrics(_timeFont).getStringBounds(currentString, g);
			
			
			double xPos = currentMs / msPerPixel + WavDisplay._TIME_INSETS_;
			
			g2.setColor(markerColor);
			Line2D markerLine = 
				new Line2D.Double(xPos, lineHeight, xPos, getHeight());
			g2.draw(markerLine);
			
			g2.setColor(Color.black);
			
			double xVal = xPos - (currentStringBounds.getWidth()/2.0);
			if(xVal < 0)
				xVal = 0.0;
			double yVal = lineMax + currentStringBounds.getHeight();
			
			g2.drawString(currentString, (float)xVal, (float)yVal);
		}
		
		
	}
	
	public Rectangle2D getSegStartRect() {
		double lineHeight = getHeight() / 3.0;
		double msPerPixel = (endMs - startMs) / (double)(getWidth() - 2 * WavDisplay._TIME_INSETS_);
		double rectWidth = (segStart - startMs) / msPerPixel;
		Rectangle2D segStartRect = 
			new Rectangle2D.Double(WavDisplay._TIME_INSETS_,
					lineHeight, rectWidth+1, getHeight());
		return segStartRect;
	}
	
	public Rectangle2D getSegEndRect() {
		double lineHeight = getHeight() / 3.0;
		double msPerPixel = (endMs - startMs) / (double)(getWidth() - 2 * WavDisplay._TIME_INSETS_);
		
		long endTime = segStart + segLength;
		long endLength = endMs - endTime;
		
		double xIndex = (getWidth() - WavDisplay._TIME_INSETS_);
		double rectWidth = endLength  / msPerPixel;
		double xPos = (getWidth() - WavDisplay._TIME_INSETS_) - rectWidth;
		
//		xOff = (double)(segStart + segLength) / msPerPixel;
//		
		Rectangle2D segEndRect = 
			new Rectangle2D.Double(xPos+1,
					lineHeight, xIndex - xPos, getHeight());
		return segEndRect;
	}
	
	public long getStartMs() {
		return startMs;
	}

	public void setStartMs(long startMs) {
		this.startMs = startMs;
	}

	public long getEndMs() {
		return endMs;
	}

	public void setEndMs(long endMs) {
		this.endMs = endMs;
	}

	public long getCurrentMs() {
		return currentMs;
	}

	public void setCurrentMs(long currentMs) {
		if(currentMs > endMs)
			currentMs = endMs;
		this.currentMs = currentMs;
	}

	public long getSegStart() {
		return segStart;
	}

	public void setSegStart(long segStart) {
		this.segStart = segStart;
	}

	public long getSegLength() {
		return segLength;
	}

	public void setSegLength(long segLength) {
		this.segLength = segLength;
	}
	
	public WavDisplay getWavDisplay() {
		return _parent;
	}
}
