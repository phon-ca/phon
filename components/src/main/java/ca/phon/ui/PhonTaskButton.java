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
package ca.phon.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;

import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

/**
 * Button for watching phon tasks.
 *
 */
public class PhonTaskButton extends MultiActionButton {
	
	private final static Logger LOGGER = Logger.getLogger(PhonTaskButton.class.getName());
	
	/** Busy label */
	private JXBusyLabel busyLabel;
//	
//	/** Logger console */
//	private PhonLoggerConsole console;
	
	/** Task */
	private PhonTask task;
	
	public PhonTaskButton(PhonTask task) {
		super();
		
		this.task = task;
		this.task.addTaskListener(new TaskListener());
		
		init();
	}
	
	@Override
	public Dimension getMaximumSize() {
		Dimension retVal = super.getMaximumSize();
		Dimension prefVal = super.getPreferredSize();
		
		retVal.height = prefVal.height;
		
		return retVal;
	}
	
	private void init() {
		super.removeAll();
		
		super.setBackgroundPainter(new BgPainter());
		
		FlowLayout topLayout = new FlowLayout(FlowLayout.LEFT);
		int hgap = topLayout.getHgap();
		topLayout.setHgap(0);
		topLayout.setVgap(0);
		
		JPanel topPanel = new JPanel(topLayout);
		topPanel.setOpaque(false);
		
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		busyLabel.setBusy(false);
		
		super.setTopLabelText(
				"<html><div style='font-size:14;'><b>" + task.getName() + "</b></div></html>");
		super.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		topPanel.add(busyLabel);
		topPanel.add(Box.createHorizontalStrut(hgap));
		topPanel.add(super.getTopLabel());
		
//		console = new PhonLoggerConsole();
//		console.setVisible(false);
		
		add(topPanel, BorderLayout.NORTH);
//		add(busyLabel, BorderLayout.WEST);
//		add(console, BorderLayout.CENTER);
		add(getBottomLabel(), BorderLayout.SOUTH);
		
//		PhonUIAction defAction = new PhonUIAction(this, "onToggleConsole");
//		defAction.putValue(PhonUIAction.NAME, "More info");
//		defAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle console");
//		setDefaultAction(defAction);
		
		ImageIcon cancelIcn = 
			IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		ImageIcon cancelIcnL =
			IconManager.getInstance().getIcon("actions/button_cancel", IconSize.MEDIUM);
		
		PhonUIAction cancelAction = new PhonUIAction(this, "onCancelTask");
		cancelAction.putValue(Action.NAME, "Stop task");
		cancelAction.putValue(Action.SHORT_DESCRIPTION, "Shutdown task");
		cancelAction.putValue(Action.SMALL_ICON, cancelIcn);
		cancelAction.putValue(Action.LARGE_ICON_KEY, cancelIcnL);
		addAction(cancelAction);
	}
	
	/*
	 * UI Actions
	 */
	public void onToggleConsole(PhonActionEvent pae) {
//		console.setVisible(!console.isVisible());
		revalidate();
	}
	
	public void onCancelTask(PhonActionEvent pae) {
		if(task.getStatus() == TaskStatus.RUNNING)
			task.shutdown();
	}

	private class TaskListener implements PhonTaskListener {

		@Override
		public void statusChanged(PhonTask task, TaskStatus oldStatus,
				TaskStatus newStatus) {
			if(newStatus == TaskStatus.RUNNING) {
				busyLabel.setBusy(true);
				
//				console.addReportThread(Thread.currentThread());
//				console.startLogging();
			} else {
				busyLabel.setBusy(false);
//				console.stopLogging();
//				console.removeReportThread(Thread.currentThread());
			}
		}

		@Override
		public void propertyChanged(PhonTask task, String property,
				Object oldValue, Object newValue) {
			if(property.equals(PhonTask.STATUS_PROP)) {
				PhonTaskButton.this.setBottomLabelText(newValue.toString());
			}
		}
		
	}
	
	private class BgPainter implements Painter<PhonTaskButton> {

		@Override
		public void paint(Graphics2D g, PhonTaskButton ptb, int width,
				int height) {
			
			GradientPaint gp = new GradientPaint(new Point(0,0), Color.white, 
					new Point(width, height), PhonGuiConstants.PHON_UI_STRIP_COLOR);
			MattePainter gpPainter = new MattePainter(gp);

			gpPainter.paint(g, ptb, width, height);
			
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		PhonTask task = new PhonTask("Long Task") {
			

			@Override
			public void performTask() {
				long iteration  = 0L;
				while(true) {
					if(isShutdown()) break;
					setProperty(STATUS_PROP, "Loop #" + iteration++);
					LOGGER.info("Loop #" + iteration);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
					
				
				}
			}
			
		};
		PhonWorker.getInstance().invokeLater(task);
		
		PhonTaskButton ptb = new PhonTaskButton(task);
		
		JFrame f = new JFrame("Test");
		f.add(ptb);
		f.pack();
		f.setVisible(true);
		
		PhonWorker.getInstance().start();
	}
}
