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
package ca.phon.ui;

import java.awt.*;

import javax.swing.*;

import org.jdesktop.swingx.JXBusyLabel;

import ca.phon.ui.action.*;
import ca.phon.util.icons.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.TaskStatus;

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
		
		super.setTopLabelText(
				"<html><div style='font-size:14;'><b>" + task.getName() + "</b></div></html>");
		super.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		topPanel.add(busyLabel);
		topPanel.add(Box.createHorizontalStrut(hgap));
		topPanel.add(super.getTopLabel());
		
		add(topPanel, BorderLayout.NORTH);
		add(getBottomLabel(), BorderLayout.SOUTH);
		
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
