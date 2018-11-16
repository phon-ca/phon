package ca.phon.app.query;

import ca.phon.util.PrefHelper;

public class QueryAndReportWizardSettings {
	
	public static final String LOAD_PREVIOUS_EXECUTION_ON_STARTUP = 
			QueryAndReportWizardSettings.class.getName() + ".loadPreviousExecutionOnStartup";
	public static final boolean DEFAULT_LOAD_PREVIOUS_EXECUTION_ON_STARTUP = true;
	private boolean loadPreviousExecutionOnStartup = PrefHelper.getBoolean(LOAD_PREVIOUS_EXECUTION_ON_STARTUP, DEFAULT_LOAD_PREVIOUS_EXECUTION_ON_STARTUP);
	
	public static final String CLEAN_HISTORY_ON_CLOSE = 
			QueryAndReportWizardSettings.class.getName() + ".cleanHistoryOnClose";
	public static final boolean DEFAULT_CLEAN_HISTORY_ON_CLOSE = false;
	private boolean cleanHistoryOnClose = PrefHelper.getBoolean(CLEAN_HISTORY_ON_CLOSE, DEFAULT_CLEAN_HISTORY_ON_CLOSE);

	public QueryAndReportWizardSettings() {
		super();
	}
	
	public boolean isLoadPreviousExecutionOnStartup() {
		return loadPreviousExecutionOnStartup;
	}
	
	public void setLoadPreviousExecutionOnStartup(boolean load) {
		this.loadPreviousExecutionOnStartup = load;
		PrefHelper.getUserPreferences().putBoolean(LOAD_PREVIOUS_EXECUTION_ON_STARTUP, load);
	}
	
	public boolean isCleanHistoryOnClose() {
		return cleanHistoryOnClose;
	}
	
	public void setCleanHistoryOnClose(boolean cleanHistory) {
		this.cleanHistoryOnClose = cleanHistory;
		PrefHelper.getUserPreferences().putBoolean(CLEAN_HISTORY_ON_CLOSE, cleanHistory);
	}
	
}
