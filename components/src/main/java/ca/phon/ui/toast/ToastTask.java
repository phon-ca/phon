/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
