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
package ca.phon.query.db;

import java.util.*;

import ca.phon.util.*;

/**
 * Various methods aiding report generation.  Results have a <i>schema<i>, the schema determines
 * the format of the output string.  The result schema may be one of the following:
 * 
 * <ul>
 * <li>'LINEAR' - result values are listed in order, spearated by ';'</li>
 * <li>'ALIGNED' - result values are listed in order, separated by '\u2192'.  Empty values will be displayed
 * using a 'null' character.</li>
 * <li>'DETECTOR' - pair of aligned results, separated by '\u2026'.  Empty values will be displayed
 * using a 'null' character.</li>
 * </ul>
 * 
 */
public class ReportHelper {
	
	/**
	 * Map result to a String.  This function will include
	 * all results values (including tiers added using the 'add aligned groups/word'
	 * query options.
	 * 
	 * @param r
	 * @return
	 */
	public static String createResultString(Result r) 
	{
		final int numVals = r.getNumberOfResultValues();
		String[] rvals = new String[numVals];
		for(int i = 0; i < numVals; i++) {
			rvals[i] = r.getResultValue(i).getData();
			if(rvals[i] == null || rvals[i].length() == 0)
				rvals[i] = PhonConstants.nullChar + "";
		}
		return createReportString(rvals, r.getSchema());
	}
	
	/**
	 * Produces the schema for the given result (including all result values)
	 * 
	 * @param r
	 * @return
	 */
	public static String createResultSchemaString(Result r) 
	{
		final int numVals = r.getNumberOfResultValues();
		String[] rvals = new String[numVals];
		for(int i = 0; i < numVals; i++) {
			rvals[i] = r.getResultValue(i).getTierName();
		}
		return createReportString(rvals, r.getSchema());
	}
	
	/**
	 * Map the result to a string including only the primary
	 * result values.
	 * 
	 * @param result
	 * @return s
	 */
	public static String createPrimaryResultString(Result r) {
		int numResultValues = 0;
		
		if(r.getSchema().equals("LINEAR")) {
			numResultValues = 1;
		} else if(r.getSchema().equals("ALIGNED")) {
			numResultValues = 2;
		} else if(r.getSchema().equals("DETECTOR")) {
			numResultValues = 4;
		} else {
			// TODO support custom schema
		}
		
		String[] rvals = new String[numResultValues];
		for(int i = 0; i < numResultValues; i++) {
			rvals[i] = r.getResultValue(i).getData();
			if(rvals[i] == null || rvals[i].length() == 0)
				rvals[i] = PhonConstants.nullChar + "";
		}
		return createReportString(rvals, r.getSchema());
	}
	
	/**
	 * Return a map of extra result values for the given result.
	 * 
	 * @param result
	 * @return map of non-primary result values contained in the result
	 */
	public static List<ResultValue> getExtraResultValues(Result r) {
		int rvIdx = 0;
		
		if(r.getSchema().equals("LINEAR")) {
			rvIdx = 1;
		} else if(r.getSchema().equals("ALIGNED")) {
			rvIdx = 2;
		} else if(r.getSchema().equals("DETECTOR")) {
			rvIdx = 4;
		} else {
			// TODO support custom schema
		}
		
		List<ResultValue> rvList = new ArrayList<>();
		for(; rvIdx < r.getNumberOfResultValues(); rvIdx++) {
			var rv = r.getResultValue(rvIdx);
			rvList.add(rv);
		}
		return rvList;
	}
	
	private static final char sepChar = ',';
	
	/**
	 * Creates a report string for the given values.
	 * If aligned, the first two values are assumed
	 * to be aligned, with the rest forming a context
	 * to the right of the ';'.
	 * 
	 * @param values
	 * @param aligned
	 * @return the generated report string
	 * 
	 */
	public static String createReportString(String[] values, String resultType) {
		String retVal = "";
		
		if(resultType.equalsIgnoreCase("LINEAR")) {
			retVal = createLinearReportString(values);
		} else if(resultType.equalsIgnoreCase("ALIGNED")) {
			retVal = createAlignedReportString(values);
		} else if(resultType.equalsIgnoreCase("DETECTOR")) {
			retVal = createDetectorReportString(values);
		} else {
			// TODO add support for custom schema
		}
		
		return retVal;
	}
	
	public static String createLinearReportString(String[] values) {
		String retVal = "";
		
		int startValue = 0;
		if(values.length > 1) 
		{
			retVal += values[0] + "; ";
			startValue = 1;
		}
		
		// add the rest of the values
		for(int i = startValue; i < values.length; i++) 
		{
			retVal += (i == startValue ? "" : sepChar + " ") + values[i];
		}
		
		return retVal;
	}
	
	public static String createAlignedReportString(String[] values) {
		String retVal = "";
		
		int startValue = 0;
		if(values.length >= 2) 
		{
			retVal = values[0] + " " + PhonConstants.doubleArrow + " " + values[1];
			startValue = 2;
			if(values.length > startValue)
				retVal += "; ";
		}
		
		if(values.length == 1) {
			retVal = values[0] + " " + PhonConstants.doubleArrow + " " + PhonConstants.nullChar;
			startValue = 1;
		}
		
		// add the rest of the values
		for(int i = startValue; i < values.length; i++) 
		{
			retVal += (i == startValue ? "" : sepChar + " ") + values[i];
		}
		
		return retVal;
	}
	
	public static String createDetectorReportString(String[] values) {
		String retVal = "";
		
		for(int i = 0; i < values.length; i++) {
			if(i > 0)
				retVal += (i % 2 == 0 ? " " + PhonConstants.doubleArrow + " " : 
					PhonConstants.ellipsis);
			retVal += values[i];
		}
		
		return retVal;
	}
}
