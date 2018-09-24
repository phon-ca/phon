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
package ca.phon.ui.toast;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.Popup;
import javax.swing.SwingUtilities;

import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;

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
	
	private final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(Toast.class.getName());
	
	private String messageTxt;
	
	private Color messageBackground;
	
	private Color messgaeForeground;
	
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
		this.messageTxt = message;
	}
	
	public String getMessage() {
		return this.messageTxt;
	}
	
	/**
	 * Set background color of message
	 * 
	 * @param c
	 */
	public void setMessageBackground(Color c) {
		this.messageBackground = c;
	}
	
	public Color getMessageBackground() {
		return this.messageBackground;
	}
	
	
	/**
	 * Foreground
	 */
	public void setMessageForeground(Color c) {
		this.messgaeForeground = c;
	}
	
	public Color getMessageForeground() {
		return this.messgaeForeground;
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
		start(null, ToastTask.CENTER_X, ToastTask.CENTER_Y);
	}
	
	/**
	 * Start the toast display in the center of the 
	 * window.
	 * 
	 * @param window
	 */
	public void start(JWindow window) {
		start(window.getRootPane(), ToastTask.CENTER_X, ToastTask.CENTER_Y);
	}
	
	/**
	 * Start the toast display in the center of the 
	 * frame.
	 * 
	 * @param frame
	 */
	public void start(JFrame frame) {
		start(frame.getRootPane(), ToastTask.CENTER_X, ToastTask.CENTER_Y);
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
	
	public void displayNotification() {
		displayNotification("Phon");
	}
	
	public void displayNotification(String title) {
		displayNotification(title, "");
	}
	
	/**
	 * Display message as a system notification.  On Mac OS X and Windows
	 * this will display a new system-level notification.
	 * 
	 * @param title
	 * @param subtitle
	 * 
	 */
	public void displayNotification(String title, String subtitle) {
		if(OSInfo.isMacOs()) {
			displayMacOSNotification(title, subtitle);
		}
	}
	
	/**
	 * Use AppleScript to display system notification.
	 */
	private void displayMacOSNotification(String title, String subtitle) {
		final StringBuffer src = new StringBuffer();
		
		src.append("display notification")
		   .append(" \"").append(messageTxt).append("\"");
		if(title != null && title.length() > 0)
			src.append(" with title ").append("\"").append(title).append("\"");
		if(subtitle != null && subtitle.length() > 0)
		    src.append(" subtitle ").append("\"").append(subtitle).append("\"");
		
		/*
		 * If not running from an application package, we need to use the
		 * osascript binary to display the notification.
		 * 
		 * Assume we are running in 'development' mode when phon.debug=true
		 */
		if(PrefHelper.getBoolean("phon.debug", false)) {
			final Runtime rt = Runtime.getRuntime();
			final String[] cmd = new String[]{ "/usr/bin/osascript", "-e", src.toString() };
			try {
				rt.exec(cmd);
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		} else {
			/*
			 * Use scripting engine so that the notification has the proper icon
			 * and application name
			 */
			final ScriptEngineManager manager = new ScriptEngineManager();
			final ScriptEngine appleScriptEngine = manager.getEngineByName("AppleScript");
			if(appleScriptEngine != null) {
				try {
					appleScriptEngine.eval(src.toString());
				} catch (ScriptException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			}
		}
	}
}
