package ca.phon.ipamap;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.JTextComponent;

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
public class IpaMapFrame extends JFrame {
	
	/**
	 * Property to save window location
	 */
	private final static String WINDOW_LOCATION_PROP = 
		IpaMapFrame.class.getName() + ".windowLocation";
	
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
		
		super.setFocusableWindowState(false);
		super.setAlwaysOnTop(true);
		
		init();
	}
	
	private void init() {
		mapContents = new IpaMap();
		mapContents.addPropertyChangeListener(IpaMap.SCALE_PROP, new ScaleListener());
		mapContents.setBorder(BorderFactory.createEtchedBorder());
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mapContents, BorderLayout.CENTER);
		
		WindowMoveListener l = new WindowMoveListener();
		super.addMouseListener(l);
		super.addMouseMotionListener(l);
		
//		GlassPaneFadeHandler gpfh = new GlassPaneFadeHandler(getGlassPane());
//		getGlassPane().setVisible(true);
		
//		Toolkit.getDefaultToolkit().addAWTEventListener(new FadeListener(), AWTEvent.MOUSE_EVENT_MASK);
		
		mapContents.addListener(new ButtonListener());
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
	
	/**
	 * Scale listener
	 */
	private class ScaleListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals(IpaMap.SCALE_PROP)) {
				mapContents.updateDisplay();
				Dimension screenDim = 
					Toolkit.getDefaultToolkit().getScreenSize();
				// calculate new window size
				int w = mapContents.getPreferredSize().width + ((new JScrollPane()).getVerticalScrollBar().getPreferredSize().width);
				if(w > (2 * (screenDim.width/3)))
					w = 2 * (screenDim.width/3);
				int h = mapContents.getPreferredSize().height;
				if(h > screenDim.height)
					h = screenDim.height;
				
				Point loc = IpaMapFrame.this.getLocation();
//				IpaMapFrame.this.setBounds(loc.x, loc.y, w, h);
				IpaMapFrame.this.setSize(w, h);
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
	    		if(tc.isEnabled() && tc.isEditable())
	    			tc.paste();
			}
		}
		
	}
	
	/**
	 * Listener for fading in and out the window (if supported)
	 */
	private class FadeListener implements AWTEventListener {
		
		boolean hasExited = true;

		@Override
		public void eventDispatched(AWTEvent event) {
//			if(!IpaMapFrame.this.isVisible() || !mapContents.isFadeWindow()) return;
//			// only process mouse events for this window
//			if(SwingUtilities.isDescendingFrom((Component)event.getSource(),
//					IpaMapFrame.this) && event instanceof MouseEvent) {
////				if(AWTUtilities.getWindowOpacity(IpaMapFrame.this) < 1.0f) {
////					AWTUtilities.setWindowOpacity(IpaMapFrame.this, 1.0f);
////				}
//			} else {
////				if(AWTUtilities.getWindowOpacity(IpaMapFrame.this) >= 1.0f) {
////					AWTUtilities.setWindowOpacity(IpaMapFrame.this, mapContents.getFadeOpacity());
////				}
//			}
		}
		
	}
	
	/**
	 * Pass-thru mouse handler for the glasspane, another
	 * approach to detecting when to fade the window
	 */
	private class GlassPaneFadeHandler extends MouseInputAdapter {
		
		/**
		 * Install the handler on the given component
		 * @param c
		 */
		public GlassPaneFadeHandler(Component c) {
			c.addMouseListener(this);
			c.addMouseMotionListener(this);
			c.addMouseWheelListener(this);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			redispatch(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			redispatch(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
//			AWTUtilities.setWindowOpacity(IpaMapFrame.this, 1.0f);
			redispatch(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
//			AWTUtilities.setWindowOpacity(IpaMapFrame.this, 0.1f);
			redispatch(e);
//			super.mouseExited(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			redispatch(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			redispatch(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			redispatch(e);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			redispatch(e);
		}
		
		/*
		 * Redirect the mouse event the 
		 * underlying component
		 */
		private void redispatch(MouseEvent e) {
			 Point glassPanePoint = e.getPoint();
			    Container container = IpaMapFrame.this.getContentPane();
			    Component glassPane = IpaMapFrame.this.getGlassPane();
			    Component contentPane = mapContents;
			    Point containerPoint = SwingUtilities.convertPoint(
			                                    glassPane,
			                                    glassPanePoint,
			                                    contentPane);

			    if (containerPoint.y < 0) { //we're not in the content pane
			        //Could have special code to handle mouse events over
			        //the menu bar or non-system window decorations, such as
			        //the ones provided by the Java look and feel.
			    } else {
			        //The mouse event is probably over the content pane.
			        //Find out exactly which component it's over.
			        Component component =
			            SwingUtilities.getDeepestComponentAt(
			                                    container,
			                                    containerPoint.x,
			                                    containerPoint.y);

			        if ((component != null)) {
			            //Forward events over the check box.
			            Point componentPoint = SwingUtilities.convertPoint(
			                                        glassPane,
			                                        glassPanePoint,
			                                        component);
			            component.dispatchEvent(new MouseEvent(component,
			                                                 e.getID(),
			                                                 e.getWhen(),
			                                                 e.getModifiers(),
			                                                 componentPoint.x,
			                                                 componentPoint.y,
			                                                 e.getClickCount(),
			                                                 e.isPopupTrigger()));
			        }
			    }
		}
	}
	
	/**
	 * Window movement listener
	 */
	private class WindowMoveListener extends MouseInputAdapter {
		
		boolean moving = false;
		
		Point windowPoint = new Point(0,0);

		@Override
		public void mouseDragged(MouseEvent e) {
			if(moving) {
				Point newPoint = e.getLocationOnScreen();
				newPoint.x -= windowPoint.x;
				newPoint.y -= windowPoint.y;
				
				// keep below menu bar on mac
				if(OSInfo.isMacOs()) {
					if(newPoint.y < 22) {
						newPoint.y = 22;
					}
				}
				
				IpaMapFrame.this.setLocation(newPoint.x, newPoint.y);
				setSavedWindowLocation(newPoint);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			moving = true;
			windowPoint = e.getPoint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			moving = false;
		}
		
	}
}
