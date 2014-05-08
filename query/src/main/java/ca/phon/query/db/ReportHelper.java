/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.query.db;

import java.util.Map;

import ca.phon.util.PhonConstants;

/**
 * Various methods aiding report generation.
 * 
 */
public class ReportHelper {
	
	public static String createResultString(Result r) 
	{
		final int numVals = r.getResultValues().size();
		String[] rvals = new String[numVals];
		for(int i = 0; i < numVals; i++) {
			rvals[i] = r.getResultValues().get(i).getData();
		}
		return createReportString(rvals, r.getSchema());
	}
	
	public static String createResultSchemaString(Result r) 
	{
		final int numVals = r.getResultValues().size();
		String[] rvals = new String[numVals];
		for(int i = 0; i < numVals; i++) {
			rvals[i] = r.getResultValues().get(i).getTierName();
		}
		return createReportString(rvals, r.getSchema());
	}
	
	@Deprecated
	public static void getDataBreakdown(ResultSet s,
			/*out*/ Map<String, Map<String, Integer>> dataBreakdown,
			boolean caseSensitive) {
//		// Query
//		String qSt = 
//			"SELECT RID, RVID, TIERNAME, DATA FROM " +
//			"VIEWRESULTVALUES WHERE RID = ? ORDER BY RVID";
//		
//		// get a connection
//		Connection conn = QueryDBManager.connect();
//		
//		if(conn != null) {
//			PreparedStatement stmt = null;
//			try {
//				stmt = QueryDBManager.getInstance().prepareStatement(conn, qSt);
//			} catch (SQLException e) {
//				PhonLogger.warning(e.toString());
//				return;
//			}
//			
//			// for each result, get the tier list (in order)
//			for(int rIndex = 0; rIndex < s.numberOfResults(); rIndex++) {
//				Result r = s.getResult(rIndex);
//				
//				try {
//					stmt.setLong(1, r.getId());
//					
//					List<String> tierNames = new ArrayList<String>();
//					List<String> vals = new ArrayList<String>();
////					boolean aligned = false;
//					java.sql.ResultSet rs = stmt.executeQuery();
//					while(rs.next()) {
//						String tier = rs.getString("TIERNAME");
//						tierNames.add(tier);
//						String v = rs.getString("DATA");
//						vals.add(v);
////						if(!aligned && r.getType().equals("ALIGNED")) {
////							aligned = true;
////						}
//					}
//					rs.close();
//					
//					// Create key string
//					String keyString = 
//						createReportString(tierNames.toArray(new String[0]), r.getType());
//					if(!caseSensitive)
//						keyString = keyString.toLowerCase();
//					String resultString = 
//						createReportString(vals.toArray(new String[0]), r.getType());
//					if(!caseSensitive)
//						resultString = resultString.toLowerCase();
//					Map<String, Integer> tierData = dataBreakdown.get(keyString);
//					if(tierData == null) {
//						tierData = new HashMap<String, Integer>();
//						dataBreakdown.put(keyString, tierData);
//					}
//					Integer v = tierData.get(resultString);
//					if(v == null) {
//						v = new Integer(0);
//					}
//					v++;
//					tierData.put(resultString, v);
//				} catch (SQLException e) {
//					PhonLogger.warning(e.toString());
//				}
//			}
//		}
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
			// TODO add support for custom schema types
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
			retVal = values[0] + " " + PhonConstants.doubleArrow + " " + PhonConstants.doubleArrow;
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
