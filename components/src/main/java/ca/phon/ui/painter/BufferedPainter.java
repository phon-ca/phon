/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.ui.painter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BufferedPainter<T> implements Painter<T> {
	
	/**
	 * This class will keep a reference to the size of the
	 * previously rendered area.  When this changes, one
	 * of two mode are supported.
	 *
	 */
	public static enum ResizeMode {
		/** Repaint buffer when the display area changes (default) */
		REPAINT_ON_RESIZE,
		/** Only repaint when height changes */
		REPAINT_ON_RESIZE_Y,
		/** Only repaint when width changes */
		REPAINT_ON_RESIZE_X,
		/** Scale the contents of the current buffer, useful when client data scales well */
		SCALE_ON_RESIZE;
	}
	
	/**
	 * Repaint mode
	 */
	private ResizeMode resizeMode = ResizeMode.REPAINT_ON_RESIZE;
	
	/**
	 * Flag to indicate the buffer needs to be repainted
	 */
	private volatile boolean repaintBuffer = true;
	
	/**
	 * Atomic reference of the current buffer
	 */
	private final AtomicReference<BufferedImage> bufferRef = new AtomicReference<BufferedImage>();
	
	/**
	 * Previous size of display area
	 */
	private final AtomicReference<Dimension> prevSizeRef = new AtomicReference<Dimension>(new Dimension());
	
	private final AtomicReference<BufferedImage> scaledRef = new AtomicReference<BufferedImage>();
	
	/**
	 * Property support
	 */
	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	public BufferedImage getBufferdImage() {
		return bufferRef.get();
	}
	
	public void setBufferedImage(BufferedImage img) {
		bufferRef.set(img);
	}
	
	public Dimension getPrevSize() {
		return prevSizeRef.get();
	}
	
	public void setPrevSize(Dimension size) {
		prevSizeRef.set(size);
	}
	
	public boolean isRepaintBuffer() {
		return this.repaintBuffer;
	}
	
	public void setRepaintBuffer(boolean repaintBuffer) {
		this.repaintBuffer = repaintBuffer;
	}
	
	public ResizeMode getResizeMode() {
		return this.resizeMode;
	}
	
	public void setResizeMode(ResizeMode mode) {
		this.resizeMode = mode;
	}
	
	/**
	 * Paint the buffer.
	 * 
	 * @param obj
	 * @param g2
	 * @param bounds
	 */
	protected abstract void paintBuffer(T obj, Graphics2D g2, Rectangle2D bounds);

	@Override
	public void paint(T obj, Graphics2D g2, Rectangle2D bounds) {
		final Dimension oldBounds = getPrevSize();
		final Dimension newBounds = new Dimension((int)bounds.getWidth(), (int)bounds.getHeight());
		if(oldBounds == null) {
			setRepaintBuffer(true);
		} else if(!oldBounds.equals(newBounds)) {
			if(getResizeMode() == ResizeMode.REPAINT_ON_RESIZE) {
				setRepaintBuffer(true);
			} else if(getResizeMode() == ResizeMode.REPAINT_ON_RESIZE_X
					&& oldBounds.getWidth() != newBounds.getWidth()) {
				setRepaintBuffer(true);
			} else if(getResizeMode() == ResizeMode.REPAINT_ON_RESIZE_Y
					&& oldBounds.getHeight() != newBounds.getHeight()) {
				setRepaintBuffer(true);
			}
		}
		setPrevSize(newBounds);
		
		BufferedImage img = getBufferdImage();
		if((img == null || isRepaintBuffer())
				&& (newBounds.getWidth() > 0 && newBounds.getHeight() > 0)) {
			final BufferedImage buffer = new BufferedImage((int)newBounds.getWidth(), (int)newBounds.getHeight(), 
					BufferedImage.TYPE_4BYTE_ABGR);
			final Graphics2D bufferG2 = (Graphics2D)buffer.createGraphics();
			paintBuffer(obj, bufferG2, new Rectangle2D.Double(0, 0, bounds.getWidth(), bounds.getHeight()));
			setBufferedImage(buffer);
			setRepaintBuffer(false);
			img = getBufferdImage();
		}
		
		// check for null in case something didn't work
		// in the paintBuffer implementation
		if(img != null) {
			int imgHeight = img.getHeight();
			int imgWidth = img.getWidth();
			int surfaceHeight = (int)bounds.getHeight();
			int surfaceWidth = (int)bounds.getWidth();
			
			final AffineTransform op = new AffineTransform();
			op.translate(bounds.getX(), bounds.getY());
			
			if(getResizeMode() != ResizeMode.REPAINT_ON_RESIZE
					&& (imgHeight != surfaceHeight || imgWidth != surfaceWidth)) {
				g2.drawImage(getScaledImage(bounds), op, (ImageObserver)null);
			} else {
				g2.drawImage(img, op, (ImageObserver) null);
			}
		}
	}
	
	private BufferedImage getScaledImage(Rectangle2D bounds) {
		final BufferedImage img = getBufferdImage();
		if(img == null) return null;
		
		BufferedImage scaled = scaledRef.get();
		boolean rescale = false;
		if(scaled != null) {
			// check bounds
			if(scaled.getWidth() != bounds.getWidth() || 
					scaled.getHeight() != bounds.getHeight())
				rescale = true;
		} else {
			rescale = true;
		}
		
		if(rescale) {
			int imgHeight = img.getHeight();
			int imgWidth = img.getWidth();
			int surfaceHeight = (int)bounds.getHeight();
			int surfaceWidth = (int)bounds.getWidth();
			
			scaled = new BufferedImage((int)bounds.getWidth(), (int)bounds.getHeight(), 
					BufferedImage.TYPE_4BYTE_ABGR);
			final Graphics2D g2 = scaled.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			final AffineTransform op = new AffineTransform();
			double xScale = (double)surfaceWidth / (double)imgWidth;
			double yScale = (double)surfaceHeight / (double)imgHeight;
			op.scale(xScale, yScale);
			g2.drawImage(img, op, (ImageObserver) null);
			
			scaledRef.set(scaled);
		}
		
		return scaled;
	}

	/*
	 * Property support
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void fireIndexedPropertyChange(String propertyName, int index,
			boolean oldValue, boolean newValue) {
		propSupport.fireIndexedPropertyChange(propertyName, index, oldValue,
				newValue);
	}

	public void fireIndexedPropertyChange(String propertyName, int index,
			int oldValue, int newValue) {
		propSupport.fireIndexedPropertyChange(propertyName, index, oldValue,
				newValue);
	}

	public void fireIndexedPropertyChange(String propertyName, int index,
			Object oldValue, Object newValue) {
		propSupport.fireIndexedPropertyChange(propertyName, index, oldValue,
				newValue);
	}

	public void firePropertyChange(PropertyChangeEvent evt) {
		propSupport.firePropertyChange(evt);
	}

	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return propSupport.getPropertyChangeListeners();
	}

	public PropertyChangeListener[] getPropertyChangeListeners(
			String propertyName) {
		return propSupport.getPropertyChangeListeners(propertyName);
	}

	public boolean hasListeners(String propertyName) {
		return propSupport.hasListeners(propertyName);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(propertyName, listener);
	}

}
