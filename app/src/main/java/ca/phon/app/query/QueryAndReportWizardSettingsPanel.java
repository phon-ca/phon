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
package ca.phon.app.query;

import ca.phon.app.query.QueryAndReportWizardSettings.ReportLoadStrategy;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * UI for editing {@link QueryAndReportWizardSettings}
 *
 */
public class QueryAndReportWizardSettingsPanel extends JPanel {
	
	private final QueryAndReportWizardSettings settings;
	
	private JCheckBox loadPreviousParamsBox;
	
	private JCheckBox cleanHistoryOnCloseBox;
	
	private JRadioButton loadDefaultReportButton;
	private JRadioButton loadPreviousReportButton;
	
	public QueryAndReportWizardSettingsPanel() {
		super();
		this.settings = new QueryAndReportWizardSettings();
		
		init();
	}
	
	private void init() {
		setLayout(new VerticalLayout(5));
		
		JPanel queryHistoryOptions = new JPanel(new VerticalLayout());
		queryHistoryOptions.setBorder(BorderFactory.createTitledBorder("History"));
		loadPreviousParamsBox = new JCheckBox("Load most recent entry in query history when opening a new wizard");
		loadPreviousParamsBox.setSelected(settings.isLoadPreviousExecutionOnStartup());
		loadPreviousParamsBox.addActionListener( (e) -> {
			settings.setLoadPreviousExecutionOnStartup(loadPreviousParamsBox.isSelected());
			settings.savePreferences();
		});
		
		cleanHistoryOnCloseBox = new JCheckBox("Clean history when closing window");
		cleanHistoryOnCloseBox.setSelected(settings.isCleanHistoryOnClose());
		cleanHistoryOnCloseBox.addActionListener( (e) -> {
			settings.setCleanHistoryOnClose(cleanHistoryOnCloseBox.isSelected());
			settings.savePreferences();
		});
		queryHistoryOptions.add(loadPreviousParamsBox);
		queryHistoryOptions.add(cleanHistoryOnCloseBox);
		
		JPanel reportOptions = new JPanel(new VerticalLayout());
		reportOptions.setBorder(BorderFactory.createTitledBorder("Report"));
		
		loadDefaultReportButton = new JRadioButton("Load default report when opening a new wizard");
		loadDefaultReportButton.setSelected(settings.getReportLoadStrategy() == ReportLoadStrategy.LoadDefaultReport);
		
		loadPreviousReportButton = new JRadioButton("Load previous report when opening a new wizard");
		loadPreviousReportButton.setSelected(settings.getReportLoadStrategy() == ReportLoadStrategy.LoadPreviousReport);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(loadDefaultReportButton);
		bg.add(loadPreviousReportButton);
		
		ActionListener reportLoadListener = (e) -> {
			if(loadDefaultReportButton.isSelected()) {
				settings.setReportLoadStrategy(ReportLoadStrategy.LoadDefaultReport);
			} else if(loadPreviousReportButton.isSelected()) {
				settings.setReportLoadStrategy(ReportLoadStrategy.LoadPreviousReport);
			}
			settings.savePreferences();
		};
		loadDefaultReportButton.addActionListener(reportLoadListener);
		loadPreviousReportButton.addActionListener(reportLoadListener);
		
		reportOptions.add(loadDefaultReportButton);
		reportOptions.add(loadPreviousReportButton);
		
		add(queryHistoryOptions);
		add(reportOptions);
	}

}
