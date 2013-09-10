package ca.phon.ui.toast;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;

/**
 * Task to display a toast.
 */
public class ToastTask implements Runnable {

	/**
	 * The toast
	 */
	private final Toast toast;
	
	/**
	 * Parent component (can be <code>null</code>)
	 */
	private final JComponent parent;
	
	/**
	 * Point at which to display the toast
	 */
	private final Point p;
	
	public ToastTask(Toast toast, JComponent parent, Point p) {
		super();
		this.toast = toast;
		this.parent = parent;
		this.p = p;
	}
	
	public ToastTask(Toast toast, JComponent parent, int x, int y) {
		this(toast, parent, new Point(x, y));
	}

	@Override
	public void run() {
		final PopupFactory factory = new PopupFactory();
		final Popup popup = factory.getPopup(parent, toast.getLabel(), p.x, p.y);
		
		if(toast.isFinishOnClink()) {
			toast.addToastListener(new ToastListener() {
				
				@Override
				public void startedToast(Toast toast, Popup window) {
					
				}
				
				@Override
				public void finishedToast(Toast toast, Popup popup) {
					
				}
				
				@Override
				public void clink(Toast toast, MouseEvent event) {
					closeToast(toast, popup);
					toast.removeToastListener(this);
				}
			});
		}
		
		popup.show();	
		toast.fireStartToast(popup);
		
		if(toast.getDisplayTime() > 0) {
			final Timer timer = new Timer((int)toast.getDisplayTime(), new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					closeToast(toast, popup);
				}
			});
			timer.setRepeats(false);
			timer.start();
		}
	}
	
	private void closeToast(Toast toast, Popup popup) {
		popup.hide();
		toast.fireFinishToast(popup);
	}
}
