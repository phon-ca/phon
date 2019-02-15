package ca.phon.app.session;

import java.util.ArrayList;
import java.util.List;

import ca.phon.session.RecordFilter;
import ca.phon.session.TierViewItem;

/**
 * Common options use when exporting sessions.
 *
 */
public class SessionExportSettings {

	// primary record filter
	private RecordFilter recordFilter;
	

	// participant information
	private boolean includeParticipantInfo;
	private boolean includeAge = true;
	private boolean includeBirthday = false;
	private boolean includeRole = true;
	private boolean includeSex = false;
	private boolean includeLanguage = false;
	private boolean includeGroup = false;
	private boolean includeEducation = false;
	private boolean includeSES = false;
	
	// tier data
	private boolean includeTierData = true;
		
	// options to include/exclude tiers by name
	private boolean excludeTiers = false;
	private List<String> tierList = new ArrayList<>();
	
	// add syllabification display data to IPA Target/Actual tiers
	private boolean includeSyllabification;
	
	// add alignment display to end of tier information
	private boolean includeAlignment;
	
	// tier view - order, visibility and fonts
	private List<TierViewItem> tierView;
	
	// query result options
	private boolean includeQueryResults = false;
	private boolean filterRecordsUsingQueryResults = false;
	
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
	
}
