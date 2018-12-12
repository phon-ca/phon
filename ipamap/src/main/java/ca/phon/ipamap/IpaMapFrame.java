/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.ipamap;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.JTextComponent;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;

/**
 * Frame with custom behaviour for the Ipa Map.
 *
 *<p>
 * Notes:
 * <ul> 
 *  <li>Window will save it's location across Phon sessions.  To display
 *    the window at it's saved location use the showWindow method</li>
 *  <li>Window will (optionally) fade based on the fade value of the IpaMap
 *    when mouse events are detected in another (Phon) window.</li>
 *  <li>Window size is controlled by the IpaMap scale property.  Window max
 *    size is [screenWidth/2, 2 * (screenHeight/3)]</li>
 *  <li>Window will not have default decorations and will always be on top.
 *    Changing these settings can result in strange behaviour.</li>
 *  <li>Except when using the search field this window is not focusable.  Changing
 *    the focusable state of the window can result in strange behaviour</li>
 * </ul>
 *</p>
 */
public class IpaMapFrame extends CommonModuleFrame {
	
	private static final long serialVersionUID = 5216574250809340476L;

	/**
	 * Property to save window location
	 */
	private final static String WINDOW_LOCATION_PROP = 
		IpaMapFrame.class.getName() + ".windowLocation";
	
	public final static String ALWAYS_ON_TOP_PROP =
		IpaMapFrame.class.getName() + ".alwaysOnTop";	
	
	private static Point getSavedWindowLocation() {
		Point p = new Point(0,0);
		
		final String xProp = WINDOW_LOCATION_PROP + ".x";
		final String yProp = WINDOW_LOCATION_PROP + ".y";
		
		p.x = PrefHelper.getInt(xProp, 0);
		p.y = PrefHelper.getInt(yProp, 0);
		
		return p;
	}
	
	private static void setSavedWindowLocation(Point p) {
		final String xProp = WINDOW_LOCATION_PROP + ".x";
		final String yProp = WINDOW_LOCATION_PROP + ".y";
		PrefHelper.getUserPreferences().putInt(xProp, p.x);
		PrefHelper.getUserPreferences().putInt(yProp, p.y);
	}
	
	/**
	 * Contents
	 */
	private IpaMap mapContents;
	
	public IpaMap getMapContents() {
		return mapContents;
	}

	public void setMapContents(IpaMap mapContents) {
		this.mapContents = mapContents;
	}

	/**
	 * Constructor
	 */
	public IpaMapFrame() {
		super("IPA Chart");
		super.setAlwaysOnTop(PrefHelper.getBoolean(ALWAYS_ON_TOP_PROP, true));
		addPropertyChangeListener("alwaysOnTop", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				PrefHelper.getUserPreferences().putBoolean(ALWAYS_ON_TOP_PROP, isAlwaysOnTop());
			}
			
		});
		
		setFocusableWindowState(false);
		
		
		init();
	}
	
	private void init() {
		mapContents = new IpaMap();
		mapContents.addPropertyChangeListener(IpaMap.SCALE_PROP, new ScaleListener());
		mapContents.setBorder(BorderFactory.createEtchedBorder());
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mapContents, BorderLayout.CENTER);
		
		mapContents.addListener(new ButtonListener());
		
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				setSavedWindowLocation(getLocation());
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				
			}
			
		});
	}
	
	public void showWindow() {
		Dimension screenDim = 
			java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int w = getMapContents().getPreferredSize().width + ((new JScrollPane()).getVerticalScrollBar().getPreferredSize().width);
		if(w > (2 * (screenDim.width/3)))
			w = 2 * (screenDim.width/3);
		int h = getMapContents().getPreferredSize().height;
		if(h > screenDim.height)
			h = screenDim.height;
		
		setSize(w, h);
		Point p = getSavedWindowLocation();
		
		if(p.x >= screenDim.width) {
			p.x = screenDim.width-w;
		}
		if(p.y >= screenDim.height) {
			p.y = screenDim.height-h;
		}
		
		if(p.x == 0 && p.y == 0)
			super.setLocationByPlatform(true);
		else
			setLocation(p);
		
		setVisible(true);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if(!visible) {
			setSavedWindowLocation(getLocationOnScreen());
		}
		super.setVisible(visible);
	}
	
	/**
	 * Scale listener
	 */
	private class ScaleListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals(IpaMap.SCALE_PROP)) {
				mapContents.updateDisplay();
			}
		}
		
	}
	
	/**
	 * Default key listener
	 */
	private class ButtonListener implements IpaMapListener {
		
		@Override
		public void ipaMapEvent(String txt) {
			KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	    	Component focusedComp = focusManager.getFocusOwner();
	    	if(focusedComp != null && focusedComp instanceof JTextComponent) {
	    		JTextComponent tc = (JTextComponent)focusedComp;
	    		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(txt), mapContents);
	    		if(tc.isEnabled() && tc.isEditable()) {
	    			tc.paste();
	    		}
			}
		}
		
	}
	
}
