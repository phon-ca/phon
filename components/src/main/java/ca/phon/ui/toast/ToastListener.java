package ca.phon.ui.toast;

import java.awt.event.MouseEvent;

import javax.swing.Popup;

/**
 * Listens for display/close events on a Toast window.
 */
public interface ToastListener {
	
	/**
	 * Called when the Toast window is displayed.
	 * 
	 * @param toast the toast object 
	 * @param popup the toast popup
	 */
	public void startedToast(Toast toast, Popup window);
	
	/**
	 * Called when the Toast window is closed.
	 * 
	 * @param toast
	 * @param popup
	 */
	public void finishedToast(Toast toast, Popup popup);
	
	/**
	 * Called when the Toast window is clicked.
	 * 
	 * @param toast
	 * @param window
	 * @param mouseEvent
	 */
	public void clink(Toast toast, MouseEvent event);

}
