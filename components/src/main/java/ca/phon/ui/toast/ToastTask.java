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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Task to display a toast.
 */
public class ToastTask implements Runnable {
	
	public final static int CENTER_X = Integer.MAX_VALUE;
	
	public final static int CENTER_Y = Integer.MAX_VALUE;

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

	private JLabel createLabel() {
		final JLabel label = new JLabel();
		
		label.setText(toast.getMessage());
		label.setBackground(toast.getMessageBackground());
		label.setForeground(toast.getMessageForeground());
		label.setBorder(BorderFactory.createLineBorder(Color.black));
		
		return label;
	}
	
	@Override
	public void run() {
		final PopupFactory factory = new PopupFactory();
		final JLabel label = createLabel();
		final Dimension prefSize = label.getPreferredSize();
		
		if(p.x == CENTER_X) {
			final Dimension bounds = 
					(parent == null ? Toolkit.getDefaultToolkit().getScreenSize() : parent.getSize());
			p.x = (bounds.width / 2) - (prefSize.width / 2);
		}
		
		if(p.y == CENTER_Y) {
			final Dimension bounds = 
					(parent == null ? Toolkit.getDefaultToolkit().getScreenSize() : parent.getSize());
			p.y = (bounds.height / 2) - (prefSize.height / 2);
		}
		
		final Popup popup = factory.getPopup(parent, label, p.x, p.y);
		
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
