/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

public class DefaultRangeSliderUI extends RangeSliderUI implements SwingConstants {
//	static {
//		UIManager.put("RangeSlider.font", 
//				UserPrefManager.getTranscriptFont().deriveFont(10.0f));
//	}
	
	/** The component this UI is installed on */
	protected JRangeSlider slider;
	
	protected Insets focusInsets;
	protected Insets insetCache;
	protected boolean leftToRightCache = true;
	
	/** Gemometry */
	protected Rectangle focusRect;
	protected Rectangle contentRect;
	protected Rectangle trackRect;
	protected Rectangle filledTrackRect;
	protected Rectangle startThumbRect;
	protected Rectangle extentThumbRect;
	protected Rectangle startLabelRect;
	protected Rectangle extentLabelRect;
	
	/** Is the user dragging one of the controls? */
	private transient boolean isDraggingStart = false;
	private transient boolean isDraggingExtent = false;
	private transient boolean isDraggingAmbigious = false;
	
	/** Listeners */
	protected ThumbListener thumbListener;
	protected ChangeListener changeListener;
	protected ComponentListener componentListener;
	protected FocusListener focusListener;
	protected PropertyChangeListener propertyChangeListener;
	private DefaultHandler handler;
	
	private ImageIcon thumbIcon;
	
	public boolean isDragging() {
		return isDraggingExtent() || isDraggingStart();
	}
	
	public boolean isDraggingExtent() {
		return isDraggingExtent;
	}
	public boolean isDraggingStart() {
		return isDraggingStart;
	}
	
	/**
	 * Constructor
	 */
	public DefaultRangeSliderUI(JRangeSlider slider) {
		super();
		
		this.slider = slider;
		
		loadIcon();
	}
	
	private void loadIcon() {
		// load it from the classpath
		thumbIcon = 
			new ImageIcon(getClass().getResource("rsicon_horiz.png"));
	}
	
	/**
	 * UI Implementation
	 */
	public static ComponentUI createUI(JComponent b) {
		return new DefaultRangeSliderUI((JRangeSlider)b);
	}
	
	@Override
	public void installUI(JComponent comp) {
		slider = (JRangeSlider)comp;
		
		slider.setEnabled(slider.isEnabled());
		LookAndFeel.installProperty(slider, "opaque", Boolean.TRUE);
		
		isDraggingStart = false;
		isDraggingExtent = false;
		
		// listeners
		thumbListener = createThumbListener(slider);
		changeListener = createChangeListener(slider);
		componentListener = createComponentListener(slider);
		focusListener = createFocusListener(slider);
		propertyChangeListener = createPropertyChangeListener(slider);
		
		// add listeners
		installListeners(slider);
		
		focusInsets = (Insets)UIManager.get("Slider.focusInsets");
		
		insetCache = slider.getInsets();
		leftToRightCache = slider.getComponentOrientation().isLeftToRight();
		
		// create rectangles
		focusRect = new Rectangle();
		contentRect = new Rectangle();
		trackRect = new Rectangle();
		filledTrackRect = new Rectangle();
		startThumbRect = new Rectangle();
		extentThumbRect = new Rectangle();
		startLabelRect = new Rectangle();
		extentLabelRect = new Rectangle();
		
//		slider.setFont((Font)UIManager.getFont("RangeSlider.font"));
		setupGeom();
	}
	
	protected ThumbListener createThumbListener(JRangeSlider slider) {
		return new ThumbListener();
	}
	
	protected ChangeListener createChangeListener(JRangeSlider slider) {
		return getHandler();
	}
	
	protected ComponentListener createComponentListener(JRangeSlider slider) {
		return getHandler();
	}
	
	protected FocusListener createFocusListener(JRangeSlider slider) {
		return getHandler();
	}
	
	protected PropertyChangeListener createPropertyChangeListener(JRangeSlider slider) {
		return getHandler();
	}
	
	private DefaultHandler getHandler() {
		if(handler == null)
			handler = new DefaultHandler();
		return handler;
	}
	
	protected void installListeners(JRangeSlider slider) {
		slider.addMouseListener(thumbListener);
		slider.addMouseMotionListener(thumbListener);
		slider.addFocusListener(focusListener);
		slider.addComponentListener(componentListener);
		slider.addPropertyChangeListener(propertyChangeListener);
		slider.getModel().addChangeListener(changeListener);
	}
	
	protected void uninstallListeners(JRangeSlider slider) {
		slider.removeMouseListener(thumbListener);
		slider.removeMouseMotionListener(thumbListener);
		slider.removeFocusListener(focusListener);
		slider.removeComponentListener(componentListener);
		slider.removePropertyChangeListener(propertyChangeListener);
		slider.getModel().removeChangeListener(changeListener);
	}
	
	@Override
	public void uninstallUI(JComponent comp) {
		if(comp != slider) {
			throw new IllegalArgumentException(this + " can only uninstall UI from component: " + slider);
		}
		
		uninstallListeners(slider);
		
		focusInsets = null;
		insetCache = null;
		leftToRightCache = true;
		focusRect = null;
		contentRect = null;
		trackRect = null;
		startThumbRect = null;
		extentThumbRect = null;
		
		slider = null;
	}
	
	private void checkOrientation(int orientation) {
		if(orientation != HORIZONTAL &&
				orientation != VERTICAL) {
			throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL");
		}
	}
	
	/**
	 * Calculate preferred, min and max sizes
	 */
	protected Dimension getPreferredSize(int orientation) {
		checkOrientation(orientation);
		
		if(orientation == HORIZONTAL)
			return new Dimension(200, 21);
		else
			return new Dimension(21, 200);
	}
	
	@Override
	public Dimension getPreferredSize(JComponent comp) {
		Dimension retVal = 
			getPreferredSize(slider.getOrientation());
		
		if(slider.getOrientation() == HORIZONTAL) {
			int heightMod = 
				insetCache.top + insetCache.bottom;
			heightMod +=
				focusInsets.top + focusInsets.bottom;
			heightMod +=
				trackRect.height;
			retVal.height += heightMod;
		} else {
			int widthMod = 
				insetCache.left + insetCache.right;
			widthMod +=
				focusInsets.left + focusInsets.right;
			widthMod +=
				trackRect.width;
			retVal.width += widthMod;
		}
		
		return retVal;
	}
	
	protected Dimension getMinimumSize(int orientation) {
		checkOrientation(orientation);
		
		if(orientation == HORIZONTAL)
			return new Dimension(36, 21);
		else
			return new Dimension(21, 36);
	}
	
	@Override
	public Dimension getMinimumSize(JComponent comp) {
		Dimension retVal = 
			getMinimumSize(slider.getOrientation());
		
		if(slider.getOrientation() == HORIZONTAL) {
			int heightMod = 
				insetCache.top + insetCache.bottom;
			heightMod +=
				focusInsets.top + focusInsets.bottom;
			heightMod +=
				trackRect.height;
			retVal.height += heightMod;
		} else {
			int widthMod = 
				insetCache.left + insetCache.right;
			widthMod +=
				focusInsets.left + focusInsets.right;
			widthMod +=
				trackRect.width;
			retVal.width += widthMod;
		}
		
		return retVal;
	}
	
	@Override
	public Dimension getMaximumSize(JComponent comp) {
		Dimension d = getPreferredSize(comp);
		if(slider.getOrientation() == HORIZONTAL)
			d.width = Short.MAX_VALUE;
		else
			d.height = Short.MAX_VALUE;
		
		return d;
	}
	
	protected void setupGeom() {
		setupFocusRect();
		setupContentRect();
		setupStartThumbRect();
		setupExtentThumbRect();
		setupTrackRect();
		setupStartThumbLocation();
		setupExtentThumbLocation();
		setupFilledTrackRect();
		setupStartLabelRect();
		setupExtentLabelRect();
	}
	
	protected void setupFocusRect() {
		focusRect.x = insetCache.left;
		focusRect.y = insetCache.top;
		focusRect.width = 
			slider.getWidth() - (insetCache.left + insetCache.right);
		focusRect.height = 
			slider.getHeight() - (insetCache.top + insetCache.bottom);
		
//		System.out.println("Focus:\t" + focusRect);
	}
	
	protected void setupContentRect() {
		contentRect.x = focusRect.x + focusInsets.left;
		contentRect.y = focusRect.y + focusInsets.top;
		contentRect.width =
			focusRect.width - (focusInsets.left + focusInsets.right);
		contentRect.height = 
			focusRect.height - (focusInsets.top + focusInsets.bottom);
		
//		System.out.println("Content:\t" + contentRect);
	}
	
	protected void setupStartThumbRect() {
		startThumbRect.setSize(13, 14);
	}
	
	protected void setupExtentThumbRect() {
		extentThumbRect.setSize(13, 14);
	}
	
	protected void setupTrackRect() {
		if(slider.getOrientation() == HORIZONTAL) {
			trackRect.x = contentRect.x + startThumbRect.width/2;
			trackRect.y = contentRect.y + startLabelRect.height;
			trackRect.width = contentRect.width - startThumbRect.width;
			trackRect.height = startThumbRect.height;
		} else {
			trackRect.x = contentRect.x + (contentRect.width - 1)/2;
			trackRect.y = contentRect.y + startThumbRect.height/2;
			trackRect.width = startThumbRect.width;
			trackRect.height = contentRect.height - startThumbRect.height;
		}
	}
	
	protected void setupFilledTrackRect() {
		if(slider.getOrientation() == HORIZONTAL) {
			filledTrackRect.x = 
				startThumbRect.x + startThumbRect.width/2 + 1;
			filledTrackRect.y = trackRect.y;
			filledTrackRect.width =
				extentThumbRect.x-startThumbRect.x;
			filledTrackRect.height = trackRect.height;
		} else {
			
		}
	}
	
	protected void setupStartThumbLocation() {
		if(slider.getOrientation() == HORIZONTAL) {
			int valuePosition = 
				getXForValue(slider.getStart());
			
			startThumbRect.x = valuePosition - (startThumbRect.width/2);
			startThumbRect.y = trackRect.y;
		} else {
			int valuePosition = 
				getYForValue(slider.getStart());
			
			startThumbRect.x = trackRect.x;
			startThumbRect.y = valuePosition - (startThumbRect.height/2);
		}
		
//		System.out.println("Start:\t" + startThumbRect);
	}
	
	protected void setupExtentThumbLocation() {
		if(slider.getOrientation() == HORIZONTAL) {
			int valuePosition = 
				getXForValue(slider.getStart()+slider.getLength());
			
			extentThumbRect.x = valuePosition - (extentThumbRect.width/2);
			extentThumbRect.y = trackRect.y;
		} else {
			int valuePosition = 
				getYForValue(slider.getStart()+slider.getLength());
			
			extentThumbRect.x = trackRect.x;
			extentThumbRect.y = valuePosition - (extentThumbRect.height/2);
		}
	}
	
	protected void setupStartLabelRect() {
		if(slider.getOrientation() == HORIZONTAL) {
			// calculate height using the sliders font
			
			Font theFont = slider.getFont();
			if(theFont == null)
				return;
			FontMetrics fm =
				slider.getFontMetrics(theFont);
			
			String startLabelString = getStartLabelString();
			
			startLabelRect.x = startThumbRect.x + (startThumbRect.width/2);
			startLabelRect.width = 
				(int)Math.round(fm.getStringBounds(startLabelString, slider.getGraphics()).getWidth());
			startLabelRect.height = 
				fm.getHeight();
			startLabelRect.y = startThumbRect.y - startLabelRect.height;
			
			if(startLabelRect.x+startLabelRect.width > contentRect.x+contentRect.width) {
				startLabelRect.x = 
					(contentRect.x+contentRect.width)-startLabelRect.width;
			}
		} else {
			// TODO: Vertical label
		}
	}
	
	protected void setupExtentLabelRect() {
		if(slider.getOrientation() == HORIZONTAL) {
			// calculate height using the sliders font
			Font theFont = slider.getFont();
			if(theFont == null)
				return;
			FontMetrics fm =
				slider.getFontMetrics(theFont);
			
			String extentLabelString = getStartLabelString();
			
			extentLabelRect.width = 
				(int)Math.round(fm.getStringBounds(extentLabelString, slider.getGraphics()).getWidth());
			extentLabelRect.height = 
				fm.getHeight();
			extentLabelRect.y = extentThumbRect.y-extentLabelRect.height;
			extentLabelRect.x = (extentThumbRect.x + (extentThumbRect.width/2)) - extentLabelRect.width;
			
			if(extentLabelRect.x < contentRect.x) {
				extentLabelRect.x = contentRect.x;
			}
		} else {
			// TODO: Vertical label
		}
	}
	
	protected String getStartLabelString() {
		String retVal = new String();
		
		if(slider.getLabelFormat() != null) {
			retVal = slider.getLabelFormat().format(slider.getStart());
		} else {
			retVal = slider.getStart() + "";
		}
		
		return retVal;
	}
	
	protected String getExtentLabelString() {
		String retVal = new String();
		
		int endValue = 
			slider.getStart() + slider.getLength();
		
		if(slider.getLabelFormat() != null) {
			retVal = slider.getLabelFormat().format(endValue);
		} else {
			retVal = endValue + "";
		}
		
		return retVal;
	}
	
	/*
	 * Position info
	 */
	protected int getXForValue(int value) {
		int min = slider.getMinimum();
		int max = slider.getMaximum();
		int trackLength = trackRect.width;
		
		double range = (double)max - (double)min;
		double valueWidth = trackLength / range; // number of pixel per value
		
		int trackLeft = trackRect.x;
		int trackRight = trackRect.x + (trackRect.width - 1);
		
		int retVal = trackLeft;
		retVal += Math.round( valueWidth * ((double)value - min) );
		
		retVal = Math.max(trackLeft, retVal);
		retVal = Math.min(trackRight, retVal);
		
		return retVal;
	}
	
	protected int getYForValue(int value) {
		int min = slider.getMinimum();
		int max = slider.getMaximum();
		int trackLength = trackRect.height;
		
		double range = (double)max - (double)min;
		double valueWidth = trackLength / range; // number of pixel per value
		
		int trackTop = trackRect.y;
		int trackBottom = trackRect.y + (trackRect.height - 1);
		
		int retVal = trackTop;
		retVal += Math.round( valueWidth * ((double)max - value) );
		
		retVal = Math.max(trackTop, retVal);
		retVal = Math.min(trackBottom, retVal);
		
		return retVal;
	}
	
	public int getValueForX(int x) {
		int retVal = 0;
		
		int min = slider.getMinimum();
		int max = slider.getMaximum();
		int trackLength = trackRect.width;
		
		int trackLeft = trackRect.x;
		int trackRight = trackRect.x + (trackRect.width - 1);
		
		if( x <= trackLeft ) {
			retVal = min;
		} else if ( x >= trackRight ) {
			retVal = max;
		} else {
			int delta = x - trackLeft;
			
			double range = (double)max - (double)min;
			double valueWidth = range / trackLength;
			
			int valueDelta = (int)Math.round( delta * valueWidth );
			retVal = min + valueDelta;
		}
		
		return retVal;
	}
	
	public int getValueForY(int y) {
		int retVal = 0;
		
		int min = slider.getMinimum();
		int max = slider.getMaximum();
		int trackLength = trackRect.height;
		
		int trackTop = trackRect.y;
		int trackBottom = trackRect.y + (trackRect.height - 1);
		
		if( y <= trackTop ) {
			retVal = max;
		} else if ( y >= trackBottom ) {
			retVal = min;
		} else {
			int delta = y - trackTop;
			
			double range = (double)max - (double)min;
			double valueWidth = range / trackLength;
			
			int valueDelta = (int)Math.round( delta * valueWidth );
			retVal = max - valueDelta;
		}
		
		return retVal;
	}
	
	private static Rectangle startUnionRect = new Rectangle();

    public void setStartThumbLocation(int x, int y)  {
    	startUnionRect.setBounds( startThumbRect );

    	startThumbRect.setLocation( x, y );
    	
    	SwingUtilities.computeUnion(startLabelRect.x, startLabelRect.y, startLabelRect.width+1, startLabelRect.height+1, startUnionRect);
    	
    	startLabelRect.setLocation(x, contentRect.y + startLabelRect.height);
//    	slider.repaint();
		SwingUtilities.computeUnion( startThumbRect.x, startThumbRect.y, startThumbRect.width, startThumbRect.height, startUnionRect ); 
	    SwingUtilities.computeUnion( startLabelRect.x, startLabelRect.y, startLabelRect.width+1, startLabelRect.height+1, startUnionRect );
		slider.repaint( startUnionRect.x, startUnionRect.y, startUnionRect.width+1, startUnionRect.height +1);
    }
    
    private static Rectangle extentUnionRect = new Rectangle();

    public void setExtentThumbLocation(int x, int y)  {
    	extentUnionRect.setBounds( extentThumbRect );

    	
        extentThumbRect.setLocation( x, y );
        
        SwingUtilities.computeUnion( extentLabelRect.x, extentLabelRect.y, extentLabelRect.width+1, extentLabelRect.height+1, extentUnionRect );
        
        extentLabelRect.setLocation( x, contentRect.y + extentThumbRect.height);
//        slider.repaint();
        SwingUtilities.computeUnion( extentThumbRect.x, extentThumbRect.y, extentThumbRect.width, extentThumbRect.height, extentUnionRect ); 
        SwingUtilities.computeUnion( extentLabelRect.x, extentLabelRect.y, extentLabelRect.width+1, extentLabelRect.height+1, extentUnionRect );
        slider.repaint( extentUnionRect.x, extentUnionRect.y, extentUnionRect.width+1, extentUnionRect.height+1 );
    }
	
	/*
	 * Painting
	 */
	@Override
	public void paint(Graphics g, JComponent comp) {
		recalculateIfInsetsChanged();
		recalculateIfOrientationChanged();
		
		Rectangle clipRect = g.getClipBounds();
		
//		g.setColor(Color.green);
//		g.drawRect(contentRect.x, contentRect.y, contentRect.width, contentRect.height);
		
		if(!clipRect.intersects(trackRect))
			setupGeom();
		
		if(clipRect.intersects(trackRect))
			paintTrack(g);
		
		if(clipRect.intersects(filledTrackRect) || slider.getValueIsAdjusting())
			paintFilledTrack(g);
		
//		if(slider.hasFocus() && clipRect.intersects(focusRect))
//			paintFocus(g);
		
		if(clipRect.intersects(startThumbRect)) {
//			paintFilledTrack(g);
			paintStartThumb(g);
			if(slider.getValueIsAdjusting()) paintExtentThumb(g);
		}
		
		if(clipRect.intersects(extentThumbRect)) {
//			paintFilledTrack(g);
			paintExtentThumb(g);
			if(slider.getValueIsAdjusting()) paintStartThumb(g);
		}
		
		if(slider.isPaintSlidingLabel()) {
			if(clipRect.intersects(startLabelRect) && isDraggingStart)
				paintStartLabel(g);
			
			if(clipRect.intersects(extentLabelRect) && isDraggingExtent)
				paintExtentLabel(g);
		}
	}
	
	protected void recalculateIfInsetsChanged() {
		Insets newInsets = slider.getInsets();
		if(!newInsets.equals(insetCache))
			insetCache = newInsets;
		setupGeom();
	}
	
	protected void recalculateIfOrientationChanged() {
		boolean ltr = 
			slider.getComponentOrientation().isLeftToRight();
		if(ltr != leftToRightCache)
			leftToRightCache = ltr;
		setupGeom();
	}
	
	private final Color focusColor = Color.decode("0xbbbbbb");
	public void paintFocus(Graphics g) {
		g.setColor(focusColor);
		BasicGraphicsUtils.drawDashedRect(g, 
				focusRect.x, focusRect.y, focusRect.width, focusRect.height);
	}


	private final Color trackColor = Color.decode("0xbbbbbb");
	public void paintTrack(Graphics g) {
		g.drawRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height);
		if(slider.getOrientation() == HORIZONTAL) {
//			int trackX = 0;
			int trackY = (trackRect.height/2) - 7;
			int trackW = trackRect.width;
			int trackH = 3;
			
			g.translate(trackRect.x, trackRect.y+trackY);
			
//			BasicGraphicsUtils.drawGroove(g, 0, 0, trackW, trackH, 
//					focusColor, Color.black);
			g.setColor(trackColor);
			g.drawLine(0, 1, trackW-2, 1);
			
			g.translate(-trackRect.x, -(trackRect.y+trackY));
		} else {
			int trackX = (trackRect.width/2) - 2;
//			int trackY = 0;
			int trackW = 3;
			int trackH = trackRect.height;
			
			g.translate(trackRect.x + trackX, trackRect.y);
			
//			BasicGraphicsUtils.drawGroove(g, 0, 0, trackW, trackH, 
//					focusColor, Color.black);
			g.setColor(Color.black);
			g.drawLine(1, 0, 1, trackH-2);
			
			g.translate(-(trackRect.x + trackX), -trackRect.y);
		}
	}
	
	protected final static Color filledTrackColor = 
		Color.decode("0xffffcc");
	public void paintFilledTrack(Graphics g) {
		if(slider.getOrientation() == HORIZONTAL) {
//			int trackX = 0;
			int trackY = (filledTrackRect.height/2) - 7;
			int trackW = filledTrackRect.width;
			int trackH = 4;
			
			Rectangle r = new Rectangle();
			r.x = filledTrackRect.x;
			r.y = filledTrackRect.y+trackY;
			r.width = trackW;
			r.height = trackH;
			
			g.translate(r.x, r.y);
			
//			BasicGraphicsUtils.drawGroove(g, 0, 0, trackW, trackH, 
//					focusColor, Color.black);
//			g.setColor(Color.green);
//			g.drawLine(0, 1, trackW-2, 1);
			// setup gradient
			Color gradTop = filledTrackColor.brighter();
			Color gradBottom = filledTrackColor;
			GradientPaint gradPaint = new GradientPaint(
					new Point(0, 0), gradTop,
					new Point(0, r.height), gradBottom);
			((Graphics2D)g).setPaint(gradPaint);
			
			g.fillRect(0, 0, r.width-2, r.height);
			
			g.setColor(trackColor);
			g.drawRect(0, 0, r.width-2, r.height);
			
			g.translate(-r.x, -r.y);
		} else {
			int trackX = (filledTrackRect.width/2) - 2;
//			int trackY = 0;
			int trackW = 3;
			int trackH = filledTrackRect.height;
			
			g.translate(filledTrackRect.x + trackX, filledTrackRect.y);
			
//			BasicGraphicsUtils.drawGroove(g, 0, 0, trackW, trackH, 
//					focusColor, Color.black);
			g.setColor(Color.green);
			g.drawLine(1, 0, 1, trackH-2);
			
			g.translate(-(filledTrackRect.x + trackX), -filledTrackRect.y);
		}
	}
	
	public void paintStartThumb(Graphics g) {
		g.setColor(Color.blue);
		drawThumbRect(g, startThumbRect);
	}
	
	public void paintExtentThumb(Graphics g) {
		g.setColor(Color.red);
		drawThumbRect(g, extentThumbRect);
	}
	
	public void paintStartLabel(Graphics g) {
		g.setColor(Color.black);
		g.setFont(slider.getFont());
		
		g.translate(startLabelRect.x, startLabelRect.y+startLabelRect.height);
		
		g.drawString(getStartLabelString(), 0, 0);
		
		g.translate(-startLabelRect.x, -startLabelRect.y);
	}
	
	public void paintExtentLabel(Graphics g) {
		g.setColor(Color.black);
		g.setFont(slider.getFont());
		
		g.translate(extentLabelRect.x, extentLabelRect.y+extentLabelRect.height);
		
		g.drawString(getExtentLabelString(), 0, 0);
		
		g.translate(-extentLabelRect.x, -extentLabelRect.y);
	}
	
	private void drawThumbRect(Graphics g, Rectangle thumbRect) {
		g.translate(thumbRect.x, thumbRect.y);
		
//		g.setColor(Color.black);
		
		g.drawImage(thumbIcon.getImage(), 0, 0, slider);
//		g.drawRect(0, 0, thumbRect.width, thumbRect.height);
//		
//		if(slider.getOrientation() == HORIZONTAL) {
//			int lineX = (thumbRect.width / 2);
//			int lineY = 0;
//			
//			g.drawLine(lineX, lineY, lineX, thumbRect.height);
//		} else {
//			int lineX = 0;
//			int lineY = (thumbRect.height / 2);
//			
//			g.drawLine(lineX, lineY, thumbRect.width, lineY);
//		}
//		
		g.translate(-thumbRect.x, -thumbRect.y);
	}
	
	/*
	 * Listeners
	 */
	private class DefaultHandler implements ChangeListener, 
		ComponentListener, FocusListener, PropertyChangeListener {
		
		String[] propertyList1 = {
				"orientation", "inverted", 
				"majorTickSpacing", "minorTickSpacing",
				"componentOrientation", "labelformat"
		};
		
		String[] propertyList2 = {
				"model"
		};

		@Override
		public void stateChanged(ChangeEvent e) {
			if(!isDraggingStart() && !isDraggingExtent()) {
				setupStartThumbLocation();
				setupExtentThumbLocation();
				
				slider.repaint();
			}
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentResized(ComponentEvent e) {
			setupGeom();
			slider.repaint();
		}

		@Override
		public void componentShown(ComponentEvent e) {
		}

		@Override
		public void focusGained(FocusEvent e) {
			slider.repaint();
		}

		@Override
		public void focusLost(FocusEvent e) {
			slider.repaint();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String propName = evt.getPropertyName();
			
			for(String p:propertyList1) {
				if(p.equals(propName)) {
					setupGeom();
					slider.repaint();
				}
			}
			
			for(String p:propertyList2) {
				if(p.equals(propName)) {
					((BoundedRangeModel)evt.getOldValue()).removeChangeListener(changeListener);
					((BoundedRangeModel)evt.getNewValue()).addChangeListener(changeListener);
					
					setupStartThumbLocation();
					setupExtentThumbLocation();
					slider.repaint();
				}
			}
		}
		
	}
	
	public class ChangeHandler implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			getHandler().stateChanged(e);
		}
		
	}
	
	public class ThumbListener extends MouseInputAdapter {
		protected transient int offset;
		protected transient int currentMouseX, currentMouseY;
		
		protected transient int oldEnd; 
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(!slider.isEnabled()) return;
			
//			if(isDraggingStart) {
//				// calculate the length change
//				int lengthChange = oldStart - slider.getStart();
//				int newLength = slider.getLength() + lengthChange;
//				slider.setLength(newLength);
//			}
			
			String propName = new String();
			if(isDraggingStart)
				propName = "valuebythumb";
			else if(isDraggingExtent)
				propName = "extentbythumb";
			
			isDraggingStart = false;
			isDraggingExtent = false;
			
			slider.setValueIsAdjusting(false);
			
			slider.firePropertyChange(propName, true, false);
			
			slider.repaint();
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(!slider.isEnabled()) return;
			
			slider.requestFocus();
			
			currentMouseX = e.getX();
			currentMouseY = e.getY();
			
			if(startThumbRect.contains(currentMouseX, currentMouseY)) {
				if(slider.getOrientation() == HORIZONTAL) {
					offset = 
						currentMouseX - startThumbRect.x;
				} else {
					offset = 
						currentMouseY - startThumbRect.y;
				}
				
				if(slider.getLength() == 0) {
					isDraggingAmbigious = true;
				} else {
					isDraggingStart = true;
				}
				oldEnd = slider.getStart() + slider.getLength();
				return;
			} else if(extentThumbRect.contains(currentMouseX, currentMouseY)) {
				if(slider.getOrientation() == HORIZONTAL) {
					offset = 
						currentMouseX - extentThumbRect.x;
				} else {
					offset = 
						currentMouseY - extentThumbRect.y;
				}
				isDraggingExtent = true;
//				oldLength = slider.getLength();
				return;
			}
			
			isDraggingStart = false;
			isDraggingExtent = false;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(!slider.isEnabled()) return;
			
//			currentMouseX = e.getX();
//			currentMouseY = e.getY();
			
			slider.setValueIsAdjusting(true);
			
			
			if(isDraggingAmbigious) {
				int offset = 
						(slider.getOrientation() == HORIZONTAL ? 
								e.getX() - currentMouseX : e.getY() - currentMouseY);
				System.out.println(offset);
				if(offset > 0) {
					isDraggingStart = false;
					isDraggingExtent = true;
				} else {
					isDraggingStart = true;
					isDraggingExtent = false;
				}
				isDraggingAmbigious = false;
			}
			if(isDraggingStart())
				dragStartThumb(e);
			else if(isDraggingExtent())
				dragExtentThumb(e);
		}
		
		private void dragStartThumb(MouseEvent e) {
			if(slider.getOrientation() == HORIZONTAL) {
				int halfThumbWidth = startThumbRect.width / 2;
				int thumbLeft = e.getX() - offset;
				int trackLeft = trackRect.x;
				int trackRight = getXForValue(oldEnd)-halfThumbWidth;
				
				thumbLeft = Math.max(thumbLeft, trackLeft-halfThumbWidth);
				thumbLeft = Math.min(thumbLeft, trackRight-halfThumbWidth);
				
				int thumbMiddle = thumbLeft + halfThumbWidth;
				
				int newStart = getValueForX(thumbMiddle);
				setStartThumbLocation(thumbLeft, startThumbRect.y);

				slider.setLength(oldEnd-newStart);
				slider.setStart(newStart);
				return;
			} else {
				int halfThumbHeight = startThumbRect.height / 2;
				int thumbTop = e.getY() - offset;
				int trackTop = trackRect.y;
				int trackBottom = getYForValue(slider.getMaximum()
						- (slider.getStart() + slider.getLength()));
				
				thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
				thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

				int thumbMiddle = thumbTop + halfThumbHeight;
				int newStart = getValueForY(thumbMiddle);
//				int newLength = slider.getLength() + lengthChange;
				
				setStartThumbLocation(startThumbRect.x, thumbTop);
//				
				slider.setStart(newStart);
//				slider.setLength(newLength);
				return;
			}
		}
		
		private void dragExtentThumb(MouseEvent e) {
			if(slider.getOrientation() == HORIZONTAL) {
				int halfThumbWidth = extentThumbRect.width / 2;
				int thumbLeft = e.getX() - offset;
				int trackLeft = getXForValue(slider.getStart())+halfThumbWidth;
				int trackRight = trackRect.x + trackRect.width;
				
				thumbLeft = Math.max(thumbLeft, trackLeft-halfThumbWidth);
				thumbLeft = Math.min(thumbLeft, trackRight-halfThumbWidth);
				
				setExtentThumbLocation(thumbLeft, extentThumbRect.y);
				
				int thumbMiddle = thumbLeft + halfThumbWidth;
				slider.setLength(getValueForX(thumbMiddle) - slider.getStart());
				return;
			} else {
				int halfThumbHeight = extentThumbRect.height / 2;
				int thumbTop = e.getY() - offset;
				int trackTop = getYForValue(slider.getStart());
				int trackBottom = getYForValue(slider.getMaximum());
				
				thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
				thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);
				
				int oldTop = extentThumbRect.y;
				
				int delta = oldTop - thumbTop;
				
				double range = (double)slider.getMaximum() - (double)slider.getMaximum();
				double valueWidth = trackRect.height / range;
				
				double valueDelta = valueWidth * delta;
				int lengthChange = (int)Math.round(valueDelta);
				
				setExtentThumbLocation(extentThumbRect.x, thumbTop);
				
//				int thumbMiddle = thumbTop + halfThumbHeight;
				slider.setLength(slider.getLength()-lengthChange);
				return;
			}
		}
	}
}
