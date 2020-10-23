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
