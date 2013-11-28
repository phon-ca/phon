package ca.phon.app.session.editor.search;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.script.PhonScriptException;
import ca.phon.script.scripttable.AbstractScriptTableModel;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierDescription;
import ca.phon.util.Tuple;

public class SessionScriptTableModel extends AbstractScriptTableModel {
	
	private static final long serialVersionUID = 193148815690356981L;

	private static final Logger LOGGER = Logger
			.getLogger(SessionScriptTableModel.class.getName());
	
	/** The session */
	private Session session;
	
	private final Map<CellLocation, Object> cache =
			Collections.synchronizedMap(new TreeMap<CellLocation, Object>());
	
	/**
	 * Location information
	 */
	private class CellLocation extends Tuple<Integer, Integer> {
		
		public int getRow() {
			return (super.getObj1() == null ? -1 : super.getObj1());
		}
		
		public int getColumn() {
			return (super.getObj2() == null ? -1 : super.getObj2());
		}
		
	}
	
	private final static String RECORD_NUMBER_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output record number\", \"\"};\n" + "*/\n" + "\n" 
			+ "function getType() { return (new java.lang.Integer(0)).getClass(); }\n" 
			+ "function getName() { return \"Record #\"; }\n" 
			+ "retVal = 1+1;\n" + "";
	
	public final static String SPEAKER_NAME_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output speaker (participant) name\", \"\"};\n"
			+ "*/\n" 
			+ "function getName() { return \"Speaker\"; }\n"
			+ "\n" + "record.speaker.name\n" + "";

	public final static String SPEAKER_AGE_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output speaker (participant) age (YY;MM.DD)\", \"\"};\n"
			+ "*/\n" 
			+ "function getName() { return \"Record #\"; }\n"
			+ "\n" + "record.speaker.age\n" + "";

	public final static String SPEAKER_GENDER_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output speaker (participant) gender\", \"\"};\n"
			+ "*/\n"
			+ "function getName() { return \"Speaker Gender\"; }\n"
			+ "\n" + "record.speaker.sex\n" + "";
	
	private final static String RECORD_TIER_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output value for specified tier\", \"\"},\n"
			+ "		{separator, \"Options\", false},\n"
			+ "			{string, tierName, \"${TIER}\", \"Tier name:\"},\n"
			+ "			{bool, removeGroupMarkers, false, \"Remove group markers (i.e., '[' and ']')\", \"Group markers:\"};\n"
			+ "*/\n" + "\n" + "retVal = \"\";\n"
			+ "function getName() { return tierName; }\n" 
			+ "retVal += record.getTier(tierName).toString();\n" + "\n"
			+ "if(removeGroupMarkers) {\n"
			+ "	retVal = retVal.replace(/\\[/g, \"\").replace(/\\]/g, \"\");\n"
			+ "}\n" + "";
	
	public final static String SYLLABIFICATION_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output syllabification for specified tier\", \"\"},\n"
			+ "		{separator, \"Options\", false},\n"
			+ "			{enum, tierName, \"IPA Target\"|\"IPA Actual\", ${TIER}, \"Tier name:\"},\n"
			+ "			{bool, removeGroupMarkers, false, \"Remove group markers (i.e., '[' and ']')\", \"Group markers:\"};\n"
			+ "function getName() { return \"Syllabification\";}\n"
			+ "*/\n" + "\n" + "var retVal = \"\";\n" + "\n"
			+ "for(grpIdx = 0; grpIdx < record.numberOfGroups; grpIdx++)\n"
			+ "{\n" + "	var ipaGrp = record.getGroup(tierName, grpIdx);\n"
			+ "\n" + "	retVal += (grpIdx > 0 ? \" \" : \"\");\n"
			+ "	if(!removeGroupMarkers) retVal += \"[\";\n"
			+ "	for(wIdx = 0; wIdx < ipaGrp.numberOfWords; wIdx++) \n" + "	{\n"
			+ "		var ipaWrd = ipaGrp.getWord(wIdx);\n" + "\n"
			+ "		retVal += (wIdx > 0 ? \" \" : \"\");\n"
			+ "		for(sIdx = 0; sIdx < ipaWrd.numberOfSyllables; sIdx++)\n"
			+ "		{\n" + "			var ipaSyll = ipaWrd.getSyllable(sIdx);\n" + "\n"
			+ "			retVal += (sIdx > 0 ? \".\" : \"\");\n"
			+ "			for(pIdx = 0; pIdx < ipaSyll.numberOfPhones; pIdx++)\n"
			+ "			{\n" + "				var phone = ipaSyll.getPhone(pIdx);\n" + "\n"
			+ "				retVal += (pIdx > 0 ? \";\" : \"\");\n"
			+ "				retVal += phone + \":\" + phone.scType.identifier;\n"
			+ "			}\n" + "		}\n" + "	}\n" + "\n"
			+ "	if(!removeGroupMarkers) retVal += \"]\";\n" + "}";

	public final static String ALIGNMENT_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output phone alignment\", \"\"},\n"
			+ "		{separator, \"Options\", false},\n"
			+ "			{bool, includeConstituentType, false, \"Include syllable constituent\", \"Syllabification:\"},\n"
			+ "			{bool, removeGroupMarkers, false, \"Remove group markers (i.e., '[' and ']')\", \"Group markers:\"};\n"
			+ "function getName() { return \"Alignment\"; }\n"
			+ "*/\n" + "\n" + "var retVal = \"\";\n" + "\n"
			+ "for(grpIdx = 0; grpIdx < record.numberOfGroups; grpIdx++)\n"
			+ "{\n" + "	alignmentChar = \"\\u2194\";\n"
			+ "	ipaGrp = record.getGroup(\"IPA Target\", grpIdx);\n" + "\n"
			+ "	retVal += (grpIdx > 0 ? \" \" : \"\");\n"
			+ "	if(!removeGroupMarkers) retVal += \"[\";\n" + "\n"
			+ "	// print alignment by groups\n"
			+ "	alignment = record.getAlignmentData(ipaGrp);\n"
			+ "	if(!alignment) continue;\n"
			+ "	alignmentLength = alignment[0].length;\n" + "\n"
			+ "	for(aIdx = 0; aIdx < alignmentLength; aIdx++)\n" + "	{\n"
			+ "		topPhone = alignment[0][aIdx];\n"
			+ "		btmPhone = alignment[1][aIdx];\n" + "\n"
			+ "		retVal += (aIdx > 0 ? \";\" : \"\");\n"
			+ "		retVal += (topPhone ? topPhone : \"_\");\n"
			+ "		if(topPhone && includeConstituentType)\n"
			+ "			retVal += \":\" + topPhone.scType.identifier;\n"
			+ "		retVal += alignmentChar;\n"
			+ "		retVal += (btmPhone ? btmPhone : \"_\");\n"
			+ "		if(btmPhone && includeConstituentType)\n"
			+ "			retVal += \":\" + btmPhone.scType.identifier;\n" + "	}\n"
			+ "\n" + "	if(!removeGroupMarkers) retVal += \"]\";\n" + "}";
	
	public SessionScriptTableModel(Session session) {
		super();
		this.session = session;
		setupDefaultColumns();
	}

	@Override
	public int getRowCount() {
		return session.getRecordCount();
	}
	
	@Override
	public Map<String, Object> getMappingsAt(int row, int col) {
		final Map<String, Object> retVal = super.getMappingsAt(row, col);
		
		retVal.put("session", session);
		retVal.put("record", session.getRecord(row));
		retVal.put("table", this);
		
		return retVal;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object retVal = null;
		final CellLocation loc = new CellLocation();
		loc.setObj1(rowIndex);
		loc.setObj2(columnIndex);
		synchronized(cache) {
			retVal = cache.get(loc);
		}
		if(retVal == null) {
			retVal = super.getValueAt(rowIndex, columnIndex);
			synchronized(cache) {
				cache.put(loc, retVal);
			}
		}
		return retVal;
	}
	
	public void setRowDirty(int row) {
		synchronized(cache) {
			for(int i = 0; i < getColumnCount(); i++) {
				final CellLocation loc = new CellLocation();
				loc.setObj1(row);
				loc.setObj2(i);
				
				cache.remove(loc);
			}
			super.fireTableRowsUpdated(row, row);
		}
	}
	
	public void clearCache() {
		synchronized(cache) {
			cache.clear();
		}
	}
	
	public void resetColumns() {
		clearCache();
		removeAllColumns();
		setupDefaultColumns();
		
		super.fireTableStructureChanged();
	}

	/**
	 * Setup default columns.  Default columns are Record #, Speaker, and then
	 * all tiers.
	 * 
	 */
	private void setupDefaultColumns() {
		int colIdx = 0;
		try {
			
			super.setColumnScript(colIdx++, RECORD_NUMBER_SCRIPT);
			super.setColumnScript(colIdx++, SPEAKER_NAME_SCRIPT);
			
			// add appropriate system tiers
			for(SystemTierType systemTier:SystemTierType.values()) {
				if(systemTier != SystemTierType.TargetSyllables && 
						systemTier != SystemTierType.ActualSyllables &&
						systemTier != SystemTierType.SyllableAlignment &&
						systemTier != SystemTierType.Segment) {
					setColumnScript(colIdx++, RECORD_TIER_SCRIPT.replaceAll("\\$\\{TIER\\}", systemTier.getName()));
				}
			}
			
			// add depenent tiers
			for(TierDescription tierDesc:session.getUserTiers()) {
				setColumnScript(colIdx++, RECORD_TIER_SCRIPT.replaceAll("\\$\\{TIER\\}", tierDesc.getName()));
			}
			
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}
