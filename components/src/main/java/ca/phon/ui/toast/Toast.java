package ca.phon.ui.toast;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Popup;
import javax.swing.SwingUtilities;

/**
 * <p>A message to the user that is displayed in an 
 * un-decorated floating window.  The window
 * can be displayed for a certain amount of time
 * and/or hidden when clicked.</p>
 * 
 * <p>To create a new toast, use {@link ToastFactory}.<br/>
 * E.g.,
 * <pre>
 * final Toast toast = ToastFactory.makeToast("Hello world!");
 * toast.setMessageBackground(Color.red);
 * toast.setDisplayTime(2000);
 * toast.start(comp);
 * </pre>
 * </p>
 */
public final class Toast {
	
	/**
	 * Message label
	 */
	private JLabel label;
	
	private final static long DEFAULT_DISPLAY_TIME = 2000;
	
	/**
	 * Length of time to display window.  A negative
	 * value indicates the window is visible until clicked.
	 */
	private long displayTime = DEFAULT_DISPLAY_TIME;
	
	/**
	 * Close the window when clicked.
	 */
	private boolean closeWhenClicked = true;
	
	/**
	 * listeners
	 */
	private final List<ToastListener> listeners = 
			Collections.synchronizedList(new ArrayList<ToastListener>());
	
	/**
	 * Constructor - use {@link ToastFactory} to create new instances
	 */
	Toast() {
	}
	
	JLabel getLabel() {
		if(label == null) {
			label = new JLabel();
			label.setBorder(BorderFactory.createLineBorder(Color.black));
		}
		return label;
	}
	
	/**
	 * Is finished on clink?
	 * 
	 * @return boolean
	 */
	public boolean isFinishOnClink() {
		return this.closeWhenClicked;
	}
	
	public void setFinishOnClink(boolean v) {
		this.closeWhenClicked = v;
	}
	
	/**
	 * Set message
	 *
	 * @param message
	 */
	public void setMessage(String message) {
		getLabel().setText(message);
	}
	
	public String getMessage() {
		return getLabel().getText();
	}
	
	/**
	 * Set background color of message
	 * 
	 * @param c
	 */
	public void setMessageBackground(Color c) {
		getLabel().setBackground(c);
	}
	
	public Color getMessageBackground() {
		return getLabel().getBackground();
	}
	
	/**
	 * Foreground
	 */
	public void setMessageForeground(Color c) {
		getLabel().setForeground(c);
	}

	public Color getMessageForeground() {
		return getLabel().getForeground();
	}
	
	/**
	 * Set the amount of time the toast is visible.
	 * 
	 * @param displayTime display time in ms.  A negative
	 *  displayTime will leave the Toast shown until clicked
	 *  or otherwise handled by the programmer
	 */
	public void setDisplayTime(long displayTime) {
		this.displayTime = displayTime;
	}
	
	public long getDisplayTime() {
		return this.displayTime;
	}
	
	/**
	 * Add a new toast listener
	 *
	 * @param listener
	 */
	public void addToastListener(ToastListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}
	
	/**
	 * Remove a toast listener
	 * 
	 * @param listener
	 */
	public void  removeToastListener(ToastListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Fire a toastOpened event
	 */
	void fireStartToast(Popup popup) {
		final ToastListener[] list = listeners.toArray(new ToastListener[0]);
		for(ToastListener l:list) {
			l.startedToast(this, popup);
		}
	}
	
	/**
	 * Fire a toastClosed event
	 */
	 void fireFinishToast(Popup popup) {
		final ToastListener[] list = listeners.toArray(new ToastListener[0]);
		for(ToastListener l:list) {
			l.finishedToast(this, popup);
		}
	}
	
	/**
	 * Fire toast clinked event
	 */
	void fireToastClinked(MouseEvent evt) {
		final ToastListener[] list = listeners.toArray(new ToastListener[0]);
		for(ToastListener l:list) {
			l.clink(this, evt);
		}
	}
	
	/**
	 * Start the toast displayed in the center of the
	 * screen.
	 * 
	 */
	public void start() {
		// TODO
	}
	
	/**
	 * Start the toast display in the center of the 
	 * window.
	 * 
	 * @param window
	 */
	public void start(JWindow window) {
		// TODO
	}
	
	/**
	 * Start the tosast displayed under the given
	 * component.
	 * 
	 * @param component
	 */
	public void start(JComponent comp) {
		final Rectangle rect = comp.getVisibleRect();
		final Point p = new Point(rect.x, rect.y);
		SwingUtilities.convertPointToScreen(p, comp);
		
		start(comp, p.x, p.y + rect.height);
	}
	
	/**
	 * Start the toast displayed at the given
	 * screen coords for the given component.
	 * 
	 * @param comp
	 * @param x
	 * @param y
	 */
	public void start(JComponent comp, int x, int y) {
		final ToastTask task = new ToastTask(this, comp, new Point(x, y));
		if(SwingUtilities.isEventDispatchThread())
			task.run();
		else
			SwingUtilities.invokeLater(task); 
	}
}
