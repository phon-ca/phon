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
package ca.phon.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.RectanglePainter;

import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;

/**
 * Button for watching phon tasks.
 *
 */
public class PhonTaskButton extends MultiActionButton {
	
	private static final long serialVersionUID = 7756765374858429477L;

	/** Busy label */
	private JXBusyLabel busyLabel;
	
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
	
	@Override
	public Insets getInsets() {
		Insets insets = new Insets(5, 5, 10, 10);
		return insets;
	}
	
	private void init() {
		super.removeAll();

		FlowLayout topLayout = new FlowLayout(FlowLayout.LEFT);
		int hgap = topLayout.getHgap();
		topLayout.setHgap(0);
		topLayout.setVgap(0);
		
		JPanel topPanel = new JPanel(topLayout);
		topPanel.setOpaque(false);
		
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		busyLabel.setBusy(false);
		
		//		super.setTopLabelText();
//		super.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		topPanel.add(busyLabel);
		topPanel.add(Box.createHorizontalStrut(hgap));
		topPanel.add(super.getTopLabel());
		
		add(topPanel, BorderLayout.NORTH);
		add(getBottomLabel(), BorderLayout.SOUTH);
		
		ImageIcon cancelIcn = 
			IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		ImageIcon cancelIcnL =
			IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		
		PhonUIAction cancelAction = new PhonUIAction(this, "onCancelTask");
		cancelAction.putValue(Action.NAME, "Stop task");
		cancelAction.putValue(Action.SHORT_DESCRIPTION, "Shutdown task");
		cancelAction.putValue(Action.SMALL_ICON, cancelIcn);
		cancelAction.putValue(Action.LARGE_ICON_KEY, cancelIcnL);
		addAction(cancelAction);
		
		MattePainter matte = new MattePainter(UIManager.getColor("control"));
		RectanglePainter rectPainter = new RectanglePainter(1, 1, 1, 1);
		rectPainter.setFillPaint(PhonGuiConstants.PHON_SHADED);
		CompoundPainter<JXLabel> cmpPainter = new CompoundPainter<JXLabel>(matte, rectPainter);
		super.setBackgroundPainter(cmpPainter);
	}
	
	public JXBusyLabel getBusyLabel() {
		return this.busyLabel;
	}
	
	/*
	 * UI Actions
	 */
	public void onToggleConsole(PhonActionEvent pae) {
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
			} else {
				busyLabel.setBusy(false);
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
	
}
