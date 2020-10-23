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

import ca.phon.util.*;

public class QueryAndReportWizardSettings {
	
	public static enum ReportLoadStrategy {
		LoadDefaultReport,
		LoadPreviousReport
	};
	
	public static final String LOAD_PREVIOUS_EXECUTION_ON_STARTUP = 
			QueryAndReportWizardSettings.class.getName() + ".loadPreviousExecutionOnStartup";
	public static final boolean DEFAULT_LOAD_PREVIOUS_EXECUTION_ON_STARTUP = true;
	private boolean loadPreviousExecutionOnStartup = PrefHelper.getBoolean(LOAD_PREVIOUS_EXECUTION_ON_STARTUP, DEFAULT_LOAD_PREVIOUS_EXECUTION_ON_STARTUP);
	
	public static final String CLEAN_HISTORY_ON_CLOSE = 
			QueryAndReportWizardSettings.class.getName() + ".cleanHistoryOnClose";
	public static final boolean DEFAULT_CLEAN_HISTORY_ON_CLOSE = false;
	private boolean cleanHistoryOnClose = PrefHelper.getBoolean(CLEAN_HISTORY_ON_CLOSE, DEFAULT_CLEAN_HISTORY_ON_CLOSE);
	
	public static final String REPORT_LOAD_STRATEGY =
			QueryAndReportWizardSettings.class.getName() + ".reportLoadStrategy";
	public static final ReportLoadStrategy DEFAULT_REPORT_LOAD_STRATEGY = ReportLoadStrategy.LoadPreviousReport;
	private ReportLoadStrategy reportLoadStrategy = 
			PrefHelper.getEnum(ReportLoadStrategy.class, REPORT_LOAD_STRATEGY, DEFAULT_REPORT_LOAD_STRATEGY);

	QueryAndReportWizardSettings() {
		super();
	}
	
	public boolean isLoadPreviousExecutionOnStartup() {
		return loadPreviousExecutionOnStartup;
	}
	
	public void setLoadPreviousExecutionOnStartup(boolean load) {
		this.loadPreviousExecutionOnStartup = load;
	}
	
	public boolean isCleanHistoryOnClose() {
		return cleanHistoryOnClose;
	}
	
	public void setCleanHistoryOnClose(boolean cleanHistory) {
		this.cleanHistoryOnClose = cleanHistory;
	}

	public ReportLoadStrategy getReportLoadStrategy() {
		return reportLoadStrategy;
	}

	public void setReportLoadStrategy(ReportLoadStrategy reportLoadStrategy) {
		this.reportLoadStrategy = reportLoadStrategy;
	}
	
	public void savePreferences() {
		PrefHelper.getUserPreferences().putBoolean(LOAD_PREVIOUS_EXECUTION_ON_STARTUP, isLoadPreviousExecutionOnStartup());
		PrefHelper.getUserPreferences().putBoolean(CLEAN_HISTORY_ON_CLOSE, isCleanHistoryOnClose());
		PrefHelper.getUserPreferences().put(REPORT_LOAD_STRATEGY, getReportLoadStrategy().toString());
	}
	
}
