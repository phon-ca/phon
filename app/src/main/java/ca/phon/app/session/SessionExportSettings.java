package ca.phon.app.session;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import ca.phon.query.db.ResultSet;
import ca.phon.session.RecordFilter;
import ca.phon.session.TierViewItem;
import ca.phon.util.PrefHelper;

/**
 * Common options use when exporting sessions.
 *
 */
public class SessionExportSettings implements Cloneable {

	// primary record filter
	private RecordFilter recordFilter;
	
	// participant information
	public final static String INCLUDE_PARTICIPANT_INFO = SessionExportSettings.class.getName() + ".includeParticipantInfo";
	public final static Boolean DEFAULT_INCLUDE_PARTICIPANT_INFO = true;
	private boolean includeParticipantInfo = 
			PrefHelper.getBoolean(INCLUDE_PARTICIPANT_INFO, DEFAULT_INCLUDE_PARTICIPANT_INFO);
	
	public final static String INCLUDE_PARTICIPANT_AGE = SessionExportSettings.class.getName() + ".includeParticipantAge";
	public final static Boolean DEFAULT_INCLUDE_PARTRICIPANT_AGE = true;
	private boolean includeAge =
			PrefHelper.getBoolean(INCLUDE_PARTICIPANT_AGE, DEFAULT_INCLUDE_PARTRICIPANT_AGE);
	
	public final static String INCLUDE_PARTICIPANT_BIRTHDAY = SessionExportSettings.class.getName() + ".includeParticipantBirthday";
	public final static Boolean DEFAULT_INCLUDE_PARTICIPANT_BIRTHDAY = false;
	private boolean includeBirthday = 
			PrefHelper.getBoolean(INCLUDE_PARTICIPANT_BIRTHDAY, DEFAULT_INCLUDE_PARTICIPANT_BIRTHDAY);
	
	public final static String INCLUDE_PARTICIPANT_ROLE = SessionExportSettings.class.getName() + ".includeParticipantRole";
	public final static Boolean DEFUALT_INCLUDE_PARTICIPANT_ROLE = true;
	private boolean includeRole =
			PrefHelper.getBoolean(INCLUDE_PARTICIPANT_ROLE, DEFUALT_INCLUDE_PARTICIPANT_ROLE);
	
	public final static String INCLUDE_PARTICIPANT_SEX = SessionExportSettings.class.getName() + ".includeParticipantSex";
	public final static Boolean DEFAULT_INCLUDE_PARTICIPANT_SEX = false;
	private boolean includeSex = 
			PrefHelper.getBoolean(INCLUDE_PARTICIPANT_SEX, DEFAULT_INCLUDE_PARTICIPANT_SEX);
	
	public final static String INCLUDE_PARTICIPANT_LANGUAGE = SessionExportSettings.class.getName() + ".includeParticipantLanguage";
	public final static Boolean DEFAULT_INCLUDE_PARTICIPANT_LANGUAGE = false;
	private boolean includeLanguage = 
			PrefHelper.getBoolean(INCLUDE_PARTICIPANT_LANGUAGE, DEFAULT_INCLUDE_PARTICIPANT_LANGUAGE);
	
	public final static String INCLUDE_PARTICIPANT_GROUP = SessionExportSettings.class.getName() + ".includeParticipantGroup";
	public final static Boolean DEFAULT_INCLUDE_PARTICIPANT_GROUP = false;
	private boolean includeGroup = 
			PrefHelper.getBoolean(INCLUDE_PARTICIPANT_GROUP, DEFAULT_INCLUDE_PARTICIPANT_GROUP);
	
	public final static String INCLUDE_PARTICIPANT_EDUCATION = SessionExportSettings.class.getName() + ".includeParticipantEducation";
	public final static Boolean DEFAULT_INCLUDE_PARTICIPANT_EDUCATION = false;
	private boolean includeEducation = 
			PrefHelper.getBoolean(INCLUDE_PARTICIPANT_EDUCATION, DEFAULT_INCLUDE_PARTICIPANT_EDUCATION);
	
	public final static String INCLUDE_PARTICIPANT_SES = SessionExportSettings.class.getName() + ".includeParticipantSES";
	public final static Boolean DEFAULT_INCLUDE_PARTICIPANT_SES = false;
	private boolean includeSES = 
			PrefHelper.getBoolean(INCLUDE_PARTICIPANT_SES, DEFAULT_INCLUDE_PARTICIPANT_SES);
	
	// tier data
	public final static String INCLUDE_TIER_DATA = SessionExportSettings.class.getName() + ".includeTierData";
	public final static Boolean DEFAULT_INCLUDE_TIER_DATA = true;
	private boolean includeTierData = 
			PrefHelper.getBoolean(INCLUDE_TIER_DATA, DEFAULT_INCLUDE_TIER_DATA);
		
	// options to include/exclude tiers by name
	private boolean excludeTiers = false;
	private List<String> tierList = new ArrayList<>();
	
	// add syllabification display data to IPA Target/Actual tiers
	public final static String INCLUDE_SYLLABIFICATION = SessionExportSettings.class.getName() + ".includeSyllabification";
	public final static Boolean DEFAULT_INCLUDE_SYLLABIFICATION = false;
	private boolean includeSyllabification = 
			PrefHelper.getBoolean(INCLUDE_SYLLABIFICATION, DEFAULT_INCLUDE_SYLLABIFICATION);
	
	// add alignment display to end of tier information
	public final static String INCLUDE_ALIGNMENT = SessionExportSettings.class.getName() + ".includeAlignment";
	public final static Boolean DEFAULT_INCLUDE_ALIGNMENT = false;
	private boolean includeAlignment =
			PrefHelper.getBoolean(INCLUDE_ALIGNMENT, DEFAULT_INCLUDE_ALIGNMENT);
	
	// tier view - order, visibility and fonts
	private List<TierViewItem> tierView;
	
	// query result options
	private boolean includeQueryResults = false;
	private boolean filterRecordsUsingQueryResults = false;
	private ResultSet resultSet = null;
	
	// use in some outputs (HTML/Excel) to determine if query results are printed
	// as the first content in record tables
	private boolean showQueryResultsFirst = false;
	
	// options to include/exclude result values/metadata by name
	private boolean excludeResultValues;
	private List<String> resultValueList = new ArrayList<>();
	
	public boolean isIncludeParticipantInfo() {
		return includeParticipantInfo;
	}

	public void setIncludeParticipantInfo(boolean includeParticipantInfo) {
		this.includeParticipantInfo = includeParticipantInfo;
	}
	
	public boolean isIncludeAge() {
		return includeAge;
	}

	public void setIncludeAge(boolean includeAge) {
		this.includeAge = includeAge;
	}

	public boolean isIncludeBirthday() {
		return includeBirthday;
	}

	public void setIncludeBirthday(boolean includeBirthday) {
		this.includeBirthday = includeBirthday;
	}

	public boolean isIncludeRole() {
		return includeRole;
	}

	public void setIncludeRole(boolean includeRole) {
		this.includeRole = includeRole;
	}

	public boolean isIncludeSex() {
		return includeSex;
	}

	public void setIncludeSex(boolean includeSex) {
		this.includeSex = includeSex;
	}

	public boolean isIncludeLanguage() {
		return includeLanguage;
	}

	public void setIncludeLanguage(boolean includeLanguage) {
		this.includeLanguage = includeLanguage;
	}

	public boolean isIncludeGroup() {
		return includeGroup;
	}

	public void setIncludeGroup(boolean includeGroup) {
		this.includeGroup = includeGroup;
	}

	public boolean isIncludeEducation() {
		return includeEducation;
	}

	public void setIncludeEducation(boolean includeEducation) {
		this.includeEducation = includeEducation;
	}

	public boolean isIncludeSES() {
		return includeSES;
	}

	public void setIncludeSES(boolean includeSES) {
		this.includeSES = includeSES;
	}

	public boolean isIncludeSyllabification() {
		return includeSyllabification;
	}

	public void setIncludeSyllabification(boolean includeSyllabification) {
		this.includeSyllabification = includeSyllabification;
	}

	public boolean isIncludeAlignment() {
		return includeAlignment;
	}

	public void setIncludeAlignment(boolean includeAlignment) {
		this.includeAlignment = includeAlignment;
	}

	public boolean isIncludeTierData() {
		return this.includeTierData;
	}
	
	public void setIncludeTierData(boolean includeTierData) {
		this.includeTierData = includeTierData;
	}
	
	public boolean isExcludeTiers() {
		return this.excludeTiers;
	}
	
	public void setExcludeTiers(boolean excludeTiers) {
		this.excludeTiers = excludeTiers;
	}
	
	public List<String> getTierList() {
		return this.tierList;
	}
	
	public void setTierList(List<String> tierList) {
		this.tierList = tierList;
	}
	
	public List<TierViewItem> getTierView() {
		return tierView;
	}

	public void setTierView(List<TierViewItem> tierView) {
		this.tierView = tierView;
	}
	
	public boolean isIncludeQueryResults() {
		return this.includeQueryResults;
	}
	
	public void setIncludeQueryResults(boolean includeQueryResults) {
		this.includeQueryResults = includeQueryResults;
	}
	
	public boolean isFilterRecordsUsingQueryResults() {
		return this.filterRecordsUsingQueryResults;
	}
	
	public void setFilterRecordsUsingQueryResults(boolean filterRecords) {
		this.filterRecordsUsingQueryResults = filterRecords;
	}
	
	public ResultSet getResultSet() {
		return this.resultSet;
	}
	
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}
	
	public boolean isShowQueryResultsFirst() {
		return showQueryResultsFirst;
	}

	public void setShowQueryResultsFirst(boolean queryResultsFirst) {
		this.showQueryResultsFirst = queryResultsFirst;
	}

	public boolean isExcludeResultValues() {
		return this.excludeResultValues;
	}
	
	public void setExcludeResultValues(boolean excludeResultValues) {
		this.excludeResultValues = excludeResultValues;
	}
	
	public List<String> getResultValues() {
		return this.resultValueList;
	}
	
	public void setResultValues(List<String> resultValues) {
		this.resultValueList = resultValues;
	}
	
	public RecordFilter getRecordFilter() {
		return this.recordFilter;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
	/**
	 * Save settings as defaults.
	 * 
	 */
	public void saveAsDefaults() {
		Preferences prefs = PrefHelper.getUserPreferences();
		prefs.putBoolean(INCLUDE_PARTICIPANT_INFO, isIncludeParticipantInfo());
		prefs.putBoolean(INCLUDE_PARTICIPANT_AGE, isIncludeAge());
		prefs.putBoolean(INCLUDE_PARTICIPANT_BIRTHDAY, isIncludeBirthday());
		prefs.putBoolean(INCLUDE_PARTICIPANT_EDUCATION, isIncludeEducation());
		prefs.putBoolean(INCLUDE_PARTICIPANT_GROUP, isIncludeGroup());
		prefs.putBoolean(INCLUDE_PARTICIPANT_LANGUAGE, isIncludeLanguage());
		prefs.putBoolean(INCLUDE_PARTICIPANT_ROLE, isIncludeRole());
		prefs.putBoolean(INCLUDE_PARTICIPANT_SES, isIncludeSES());
		prefs.putBoolean(INCLUDE_PARTICIPANT_SEX, isIncludeSex());
		
		prefs.putBoolean(INCLUDE_TIER_DATA, isIncludeTierData());
		prefs.putBoolean(INCLUDE_ALIGNMENT, isIncludeAlignment());
		prefs.putBoolean(INCLUDE_SYLLABIFICATION, isIncludeSyllabification());
	}
	
	public void copySettings(SessionExportSettings settings) {
		setIncludeParticipantInfo(settings.isIncludeParticipantInfo());
		setIncludeAge(settings.isIncludeAge());
		setIncludeBirthday(settings.isIncludeBirthday());
		setIncludeRole(settings.isIncludeRole());
		setIncludeSex(settings.isIncludeSex());
		setIncludeLanguage(settings.isIncludeLanguage());
		setIncludeGroup(settings.isIncludeGroup());
		setIncludeEducation(settings.isIncludeEducation());
		setIncludeSES(settings.isIncludeSES());
		
		setRecordFilter(settings.getRecordFilter());
		
		setIncludeTierData(settings.isIncludeTierData());
		setTierView(settings.getTierView());
		setExcludeTiers(settings.isExcludeTiers());
		setTierList(settings.getTierList());
		setIncludeSyllabification(settings.isIncludeSyllabification());
		setIncludeAlignment(settings.isIncludeAlignment());
		
		setIncludeQueryResults(settings.isIncludeQueryResults());
		setFilterRecordsUsingQueryResults(settings.isFilterRecordsUsingQueryResults());
		setResultSet(settings.getResultSet());
		setExcludeResultValues(settings.isExcludeResultValues());
		setResultValues(settings.getResultValues());
		
		setShowQueryResultsFirst(settings.isShowQueryResultsFirst());
	}
	
	@Override
	public Object clone() {
		SessionExportSettings retVal = new SessionExportSettings();
		
		retVal.copySettings(this);
		
		return retVal;
	}
	
}
