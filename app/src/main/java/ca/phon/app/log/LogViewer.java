/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.log;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.*;
import java.util.logging.LogRecord;

import javax.swing.*;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

/**
 * Panel with list for viewing {@link LogRecord}s as well as
 * filtering options.
 */
public class LogViewer extends CommonModuleFrame {

	private static final long serialVersionUID = -777740161353215841L;

	private JTabbedPane tabbedPane;
	
	private JList<LogRecord> logRecordList;
	
	private LogHandler logHandler;
	
	private BufferPanel logBufferPanel;
	
	private BufferPanel prevLogBufferPanel;
	
	public LogViewer() {
		super();
		setTitle("Log Viewer");
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader("Log Viewer", "View logs for current and previous execution of Phon");
		add(header, BorderLayout.NORTH);
		
		logRecordList = new JList<>();
		logRecordList.setCellRenderer(new LogRecordListCellRenderer());
		tabbedPane = new JTabbedPane();
		updateRecentMessages();

		final JScrollPane logScroller = new JScrollPane(logRecordList);
		tabbedPane.addTab("Recent Messages", logScroller);
		
		logBufferPanel = new BufferPanel("Current Log");
		tabbedPane.addTab("Current Log", logBufferPanel);
		
		prevLogBufferPanel = new BufferPanel("Previous Log");
		tabbedPane.addTab("Previous Log", prevLogBufferPanel);
		
		add(tabbedPane, BorderLayout.CENTER);
		
		tabbedPane.addChangeListener( (e) -> {
			if(tabbedPane.getSelectedComponent() == logBufferPanel) {
				updateLogBufferPanel();
			} else if(tabbedPane.getSelectedComponent() == prevLogBufferPanel) {
				updatePreviousLogBufferPanel();
			} else {
				updateRecentMessages();
			}
			
		});
		
		final PhonUIAction closeAct = new PhonUIAction(this, "setVisible", false);
		closeAct.putValue(PhonUIAction.NAME, "Close");
		final JButton closeBtn = new JButton(closeAct);
		
		final JComponent btnPanel = ButtonBarBuilder.buildOkBar(closeBtn);
		add(btnPanel, BorderLayout.SOUTH);
	}
	
	private void updateRecentMessages() {
		logHandler = LogHandler.getInstance();
		@SuppressWarnings("unchecked")
		List<LogRecord> records = new ArrayList<>(logHandler.getLogBuffer());
	
		final DefaultListModel<LogRecord> recordModel = new DefaultListModel<>();
		records.forEach( (r) -> { recordModel.add(0, r);} );
		logRecordList.setModel(recordModel);
	}
	
	private void updateLogBufferPanel() {
		logBufferPanel.clear();
		
		try {
			String logData = LogManager.getInstance().readLogFile();
			logBufferPanel.getLogBuffer().setText(logData);
		} catch (IOException e) {
			// do nothing
		}
	}
	
	private void updatePreviousLogBufferPanel() {
		prevLogBufferPanel.clear();
		
		try {
			String logData = LogManager.getInstance().readPreviousLogFile();
			prevLogBufferPanel.getLogBuffer().setText(logData);
		} catch (IOException e) {
			// do nothing
		}
	}
	
}
