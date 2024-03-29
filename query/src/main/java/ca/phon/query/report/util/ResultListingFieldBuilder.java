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
package ca.phon.query.report.util;

import ca.phon.query.db.ResultSet;
import ca.phon.query.report.io.*;

import java.util.*;

/**
 * Scripts for adding values to (now deprecated) reports.
 * 
 * @deprecated
 */
@Deprecated
public class ResultListingFieldBuilder {

	/*
	 * Script templates
	 */
	public final static String SESSION_NAME_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output session name\", \"\"};\n" + "*/\n" + "\n"
			+ "function getValue() { return session.name; }\n" + "";

	public final static String SESSION_DATE_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output session date (YYYY-MM-DD)\", \"\"};\n"
			+ "*/\n" + "\n" + "function getValue() { return (session.date != null ? Packages.ca.phon.session.DateFormatter.dateTimeToString(session.date) : \"\"); }\n" + "";

	public final static String SESSION_MEDIA_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output session media filename\", \"\"};\n" + "*/\n"
			+ "\n" + "function getValue() { return session.mediaLocation; }\n" + "";

	public final static String RECORD_NUMBER_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output record number\", \"\"};\n" + "*/\n" + "\n" 
			+ "function getValue() { rnum = recordIndex.intValue() + 1; return \"\" + rnum; }\n" + "";

	public final static String SPEAKER_NAME_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output speaker (participant) name\", \"\"};\n"
			+ "*/\n" + "\n" + "function getValue() { return (record.speaker != null ? record.speaker.toString() : \"\"); }\n" + "";

	public final static String SPEAKER_AGE_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output speaker (participant) age (YY;MM.DD)\", \"\"};\n"
			+ "*/\n" + "\n" + "function getValue() { return Packages.ca.phon.session.AgeFormatter.ageToString("
					+ "(record.speaker.age == null ? record.speaker.getAge(session.date) : record.speaker.age)); }\n" + "";

	public final static String SPEAKER_GENDER_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output speaker (participant) gender\", \"\"};\n"
			+ "*/\n" + "\n" + "function getValue() { return (record.speaker != null && record.speaker.sex != null ? record.speaker.sex : \"\"); }\n" + "";

	public final static String RECORD_TIER_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output value for specified tier\", \"\"},\n"
			+ "		{separator, \"Options\", false},\n"
			+ "			{string, tierName, \"${TIER}\", \"Tier name:\"},\n"
			+ "			{bool, removeGroupMarkers, false, \"Remove group markers (i.e., '[' and ']')\", \"Group markers:\"};\n"
			+ "*/\n" + "function getValue() { \n" + "retVal = \"\";\n"
			+ "retVal += (record.getTier(tierName) != null ? record.getTier(tierName).toString() : \"\");\n" + "\n"
			+ "if(removeGroupMarkers == true) {\n"
			+ "	retVal = retVal.replace(/\\[/g, \"\").replace(/\\]/g, \"\");\n"
			+ "}\n return retVal; }\n" + "";

	public final static String RESULT_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output result\", \"\"};\n" + "*/\n" + "\n"
			+ "function getValue() { return result; }\n" + "";

	public final static String RESULT_FORMAT_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output result format\", \"\"};\n" + "*/\n" + "\n"
			+ "function getValue() { return result.schema; }\n" + "";

	public final static String RESULT_EXCLUDED_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Outputs 'true' if result was marked as excluded from reports\", \"\"};\n"
			+ "*/\n" + "\n" + "function getValue() { result.excluded;} \n" + "";

	public final static String METADATA_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output value for specified metadata field\", \"\"},\n"
			+ "		{separator, \"Options\", false},\n"
			+ "			{string, fieldName, \"\", \"Metadata type:\"};\n" + "*/\n"
			+ "\n function getValue() {\n" + "retVal = \"\";\n" + "\n"
			+ "metadata = result.metadata;\n" + "\n"
			+ "if(fieldName == null || fieldName.length == 0) {\n"
			+ "	// print metadata info with values\n"
			+ "	keys = metadata.keySet();\n" + "	keyitr = keys.iterator();\n"
			+ "	\n" + "	while(keyitr.hasNext()) {\n"
			+ "		key = keyitr.next();\n"
			+ "		if(key == \"<default>\") continue;\n" + "\n"
			+ "		val = metadata.get(key) + \"\";\n"
			+ "		if(val != null && val.length > 0) {\n"
			+ "			retVal += (retVal.length > 0 ? \",\" : \"\")\n"
			+ "				   + key + \"=\" + val;\n" + "		}\n" + "	}\n" + "} else {\n"
			+ "	val = metadata.get(fieldName);\n" + "	if(val)\n"
			+ "		retVal = val + \"\";\n" + "	else\n" + "		retVal = \"\";\n"
			+ "}\n return retVal; }\n" + "";

	public final static String SYLLABIFICATION_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output syllabification for specified tier\", \"\"},\n"
			+ "		{separator, \"Options\", false},\n"
			+ "			{enum, tierName, \"IPA Target\"|\"IPA Actual\", ${TIER}, \"Tier name:\"},\n"
			+ "			{bool, removeGroupMarkers, false, \"Remove group markers (i.e., '[' and ']')\", \"Group markers:\"};\n"
			+ "*/\n" + "function getValue() { \n" + "retVal = \"\";\n"
			+ "tier = record.getTier(tierName);\n"
			+ "if(tier != null) {\n"
			+ "\tfor(gIdx = 0; gIdx < tier.numberOfGroups(); gIdx++) {\n"
			+ "\t\tretVal += (gIdx > 0 ? ' ' : '');\n"
			+ "\t\tretVal += (removeGroupMarkers != true ? '[' : '');\n"
			+ "\t\tretVal += tier.getGroup(gIdx).toString(true);\n"
			+ "\t\tretVal += (removeGroupMarkers != true ? ']' : '');\n"
			+ "\t}\n} return retVal; }\n" + "";
			;
	
	public final static String ALIGNMENT_SCRIPT = "/*\n"
			+ "params = {separator, \"Information\", false},\n"
			+ "		{label, \"Output phone alignment\", \"\"},\n"
			+ "		{separator, \"Options\", false},\n"
			+ "			{bool, includeConstituentType, false, \"Include syllable constituent\", \"Syllabification:\"},\n"
			+ "			{bool, removeGroupMarkers, false, \"Remove group markers (i.e., '[' and ']')\", \"Group markers:\"};\n"
			+ "*/\n" + "function getValue() { \n" + "retVal = \"\";\n"
			+ "tier = record.getPhoneAlignment();\n"
			+ "if(tier != null) {\n"
			+ "\tfor(gIdx = 0; gIdx < tier.numberOfGroups(); gIdx++) {\n"
			+ "\t\tretVal += (gIdx > 0 ? ' ' : '');\n"
			+ "\t\tretVal += (removeGroupMarkers != true ? '[' : '');\n"
			+ "\t\tretVal += tier.getGroup(gIdx).toString(includeConstituentType.booleanValue());\n"
			+ "\t\tretVal += (removeGroupMarkers != true ? ']' : '');\n"
			+ "\t}\n} return retVal; }\n" + "";
			;
			;
	
	/**
	 * Returns fields: 'record #', 'Speaker' and 'Result'
	 * 
	 * @return default result listing fields
	 */
	public static ResultListingField[] getDefaultFields() {
		List<ResultListingField> retVal = new ArrayList<ResultListingField>();
		// record info
		ResultListingField rNumField = createEmptyField();
		rNumField.setTitle("Record #");
		rNumField.getFieldValue().setLang("Javascript");
		rNumField.getFieldValue().setScript(RECORD_NUMBER_SCRIPT);
		retVal.add(rNumField);
		
		ResultListingField rSpeakerField = createEmptyField();
		rSpeakerField.setTitle("Speaker");
		rSpeakerField.getFieldValue().setLang("Javascript");
		rSpeakerField.getFieldValue().setScript(SPEAKER_NAME_SCRIPT);
		retVal.add(rSpeakerField);
		
		// result info
		ResultListingField resValField = createEmptyField();
		resValField.setTitle("Result");
		resValField.getFieldValue().setLang("Javascript");
		resValField.getFieldValue().setScript(RESULT_SCRIPT);
		retVal.add(resValField);
		
		return retVal.toArray(new ResultListingField[0]);
	}
	
	/**
	 * Get a list of default fields for the given result set.  Returns the
	 * same as {@link #getDefaultFields()} along with additional fields
	 * for metadata.
	 * 
	 * @param rs
	 * @return
	 */
	public static ResultListingField[] getDefaultFields(ResultSet rs) {
		final List<ResultListingField> retVal = new ArrayList<ResultListingField>();
		
		for(ResultListingField defField:getDefaultFields()) retVal.add(defField);
		
		for(String metakey:rs.getMetadataKeys()) {
			final ResultListingField metadataField = createEmptyField();
			metadataField.setTitle(metakey);
			metadataField.getFieldValue().setLang("Javascript");
			metadataField.getFieldValue().setScript(METADATA_SCRIPT);
			
			final ObjectFactory factory = new ObjectFactory();
			final ScriptParameter param = factory.createScriptParameter();
			param.setName("fieldName");
			param.setContent(metakey);
			metadataField.getFieldValue().getParam().add(param);
			
			retVal.add(metadataField);
		}
		
		return retVal.toArray(new ResultListingField[0]);
	}
	
	public static ResultListingField createEmptyField() {
		
		ObjectFactory factory = new ObjectFactory();
		
		ResultListingField retVal = factory.createResultListingField();
		retVal.setFieldValue(factory.createScriptContainer());
		retVal.getFieldValue().setScript("");
		
		return retVal;
	}

}
