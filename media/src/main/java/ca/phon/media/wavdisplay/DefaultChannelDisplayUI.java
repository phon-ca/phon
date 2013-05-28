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
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import ca.phon.application.PhonTask;
import ca.phon.application.PhonTask.TaskStatus;
import ca.phon.application.PhonTaskListener;
import ca.phon.application.PhonWorker;

public class DefaultChannelDisplayUI extends ChannelDisplayUI {
	
	/* The display */
	private ChannelDisplay _display;
	
	private BufferedImage _buffer;
	
	private final BufferTaskListener _bufferListener = new BufferTaskListener();
	
	private class BufferTask extends PhonTask {
		
		private int _width;
		private int _height;
		
		public BufferTask(int width, int height) {
			_width = width;
			_height = height;
		}

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			
			if(_width <= 0 || _height <= 0) {
				super.setStatus(TaskStatus.TERMINATED);
			} else {
				BufferedImage img =
					paintImage(_width, _height);
				if(img == null) {
					super.setStatus(TaskStatus.TERMINATED);
				} else {
					super.setProperty("Buffer", img);
					super.setStatus(TaskStatus.FINISHED);
				}
			}
		}
		
		private BufferedImage paintImage(int width, int height) {
			BufferedImage retVal = new BufferedImage(width,
					height,BufferedImage.TYPE_4BYTE_ABGR);
			
			Graphics g = retVal.getGraphics();
			g.setClip(0, 0, width, height);
			
			Rectangle clipBounds = g.getClipBounds();
			
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			g.setColor(Color.white);
			g2.fillRect(0, 0, width, height);
			
			if(_display.is_loading()) {
				g.setColor(Color.black);
				g.drawString("Loading...", WavDisplay._TIME_INSETS_+1, 20);
				return null;
			} else {
				WavHelper audioInfo = _display.getAudioInfo();
//				audioInfo.playStream();

				int samples[][] = audioInfo.getAudioSamples();
				
//				double xScale = audioInfo.getXScaleFactor(width);
				double yScale = audioInfo.getYScaleFactor(height);
				int displayWidth = width - 2 * WavDisplay._TIME_INSETS_;
				double xIncr = (double)(displayWidth) / (double)samples[_display.getChannel()].length;
				
				// draw reference line
				double lineY = height/2.0;
				Line2D refLine = 
					new Line2D.Double(WavDisplay._TIME_INSETS_, lineY, displayWidth, lineY);
				
				
				g2.setColor(Color.gray);
				g2.draw(refLine);
				// draw initial sample
				
				g.setColor(_channelColors[_display.getChannel() % _channelColors.length]);
				int t = 0;
				double oldX = WavDisplay._TIME_INSETS_;
				double oldY = lineY;
				if(t > 0) {
					double scaledSample = samples[_display.getChannel()][t-1] * yScale;
					oldY = (int)(lineY - scaledSample);
				}
				double xIndex = WavDisplay._TIME_INSETS_;
				
				Line2D initialLine = 
					new Line2D.Double(oldX, oldY, xIndex, oldY);
				g2.draw(initialLine);
				xIndex += xIncr;
				oldX = xIndex;
				
				for(; t < samples[_display.getChannel()].length && xIndex < WavDisplay._TIME_INSETS_+displayWidth; t++) {
					// if width or height has changed, abort
					if(_display.getWidth() != width || _display.getHeight() != height) {
						return null;
					}
					double scaledSample = samples[_display.getChannel()][t] * yScale;
					double y = lineY - scaledSample;
					Line2D curveLine = 
						new Line2D.Double(oldX, oldY, xIndex, y);
					g2.draw(curveLine);
					
//					super.setProperty("Buffer", null);
//					super.setProperty("Buffer", )
//					BufferedImage tmpImage = new BufferedImage(width, height);
//					_buffer = retVal;
//					_display.repaint();
					
					xIndex += xIncr;
					oldX = xIndex;
					oldY = y;
				}
			}
			return retVal;
		}
	}
	
	private class BufferTaskListener implements PhonTaskListener {

		@Override
		public void propertyChanged(PhonTask task, String property,
				Object oldValue, Object newValue) {
			if(property.equals("Buffer") && newValue != null) {
				_buffer = (BufferedImage)newValue;
				_display.repaint();
				
			}
			
		}

		@Override
		public void statusChanged(PhonTask task, TaskStatus oldStatus,
				TaskStatus newStatus) {
		}
		
	}
	
	private class DisplayListener implements ComponentListener {

		@Override
		public void componentHidden(ComponentEvent e) {
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentResized(ComponentEvent e) {
			BufferTask task = new BufferTask(e.getComponent().getWidth(), e.getComponent().getHeight());
			task.addTaskListener(_bufferListener);
			PhonWorker.getInstance().invokeLater(task);
		}

		@Override
		public void componentShown(ComponentEvent e) {
		}
		
	}
	
	private final static Color[] _channelColors = {
		Color.blue,
		Color.green.darker()
	};
	
	public DefaultChannelDisplayUI(ChannelDisplay display) {
		super();
		
		_display = display;
		_display.addComponentListener(new DisplayListener());
	}
	
	@Override
	public void paint(Graphics g, JComponent display) {
		if(display != _display) return;
		
		g.setColor(Color.white);
		g.fillRect(0, 0, display.getWidth(), display.getHeight());
		
		Graphics2D g2 = (Graphics2D)g;
		
		if(_buffer == null ||
				(_buffer.getWidth() != _display.getWidth() || _buffer.getHeight() != _display.getHeight())) {
			g.setColor(Color.black);
//			g.drawString("Buffering...", 0, 20);
		} else {
			g.drawImage(_buffer, 0, 0, _display);
			
			g2.setColor(new Color(200, 200, 200, 100));
			Rectangle2D segStartRect = 
				_display.get_parent().get_timeBar().getSegStartRect();
			segStartRect.setRect(segStartRect.getX(), 0.0, segStartRect.getWidth(), _display.getHeight());
			g2.fill(segStartRect);
			
			Rectangle2D segEndRect = 
				_display.get_parent().get_timeBar().getSegEndRect();
			segEndRect.setRect(segEndRect.getX(), 0.0, segEndRect.getWidth(), _display.getHeight());
			g2.fill(segEndRect);
			
			if(_display.get_parent().get_selectionStart() >= 0
					&& _display.get_parent().get_selectionEnd() >= 0) {
				Color selColor = new Color(50, 125, 200, 100);
				g2.setColor(selColor);
				
				double msPerPixel = 
					_display.getAudioInfo().timeForFile() / (_display.getWidth() - 2*WavDisplay._TIME_INSETS_);
				
				// convert time values to x positions
				double startXPos = _display.get_parent().get_selectionStart() / msPerPixel;
				double endXPos = _display.get_parent().get_selectionEnd() / msPerPixel;
				double xPos = 
					Math.min(startXPos, endXPos) + WavDisplay._TIME_INSETS_;
				double rectLen = 
					Math.abs(endXPos - startXPos);
				
				Rectangle2D selRect =
					new Rectangle2D.Double(xPos, 0.0,
							rectLen, _display.getHeight());
				g2.fill(selRect);
			}
			
			long markerMs = _display.get_parent().get_timeBar().getCurrentMs();
			if(markerMs >= 0){
				Color markerColor = new Color(125, 125, 125, 100);
				if(_display.get_parent().getCursor() != Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)) {
					markerColor = Color.black;
				}
				
				g2.setColor(markerColor);
				
				double msPerPixel = 
					_display.getAudioInfo().timeForFile() / (_display.getWidth() - 2 * WavDisplay._TIME_INSETS_);
				double xPos = 
					markerMs / msPerPixel + WavDisplay._TIME_INSETS_;
//				int x = (int)Math.round(xPos);
				
				
				Line2D markerLine = 
					new Line2D.Double(xPos, 0.0, xPos, _display.getHeight());
				g2.draw(markerLine);
			}
		}
		
	}

}
