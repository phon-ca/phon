/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.query.report.datasource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozilla.javascript.Scriptable;

import ca.phon.project.Project;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultSet;
import ca.phon.query.report.io.ResultListing;
import ca.phon.query.report.io.ResultListingField;
import ca.phon.query.report.io.ResultListingFormatType;
import ca.phon.query.report.io.ScriptContainer;
import ca.phon.query.report.io.ScriptParameter;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.EnumScriptParam;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.script.scripttable.AbstractScriptTableModel;
import ca.phon.session.Record;
import ca.phon.session.Session;

/**
 * A data source which lists each
 * result in a search in a table.  The value
 * of each column is given by a script.
 *
 */
public class ResultListingDataSource extends AbstractScriptTableModel implements TableDataSource {
	
	private static final long serialVersionUID = 6508115371509706432L;

	private final static Logger LOGGER = Logger.getLogger(ResultListingDataSource.class.getName());

	/**
	 * Section information
	 */
	private ResultListing invData;
	
	/**
	 * Project
	 */
	private Project project;
	
	/**
	 * Search 
	 */
	private ResultSet resultSet;
	
	/**
	 * Include excluded results?
	 */
	private boolean includeExcluded;
	
//	/**
//	 * Default pkg and class imports
//	 */
//	private final String scriptPkgImports[] = {
//			"Packages.ca.phon.engines.search.script",
//			"Packages.ca.phon.engines.search.db",
//			"Packages.ca.phon.engines.search.script.params",
//			"Packages.ca.phon.featureset"
//	};
//	
//	private final String scriptClazzImports[] = {
//			"Packages.ca.phon.featureset.FeatureSet",
//			"Packages.ca.phon.util.Range",
//			"Packages.ca.phon.util.StringUtils"
//	};
	
	/**
	 * Session
	 */
	private Session session;
	
	
	public ResultListingDataSource(Project project, ResultSet s, ResultListing section) {
		this.project = project;
		this.resultSet = s;
		this.invData = section;
		this.includeExcluded = section.isIncludeExcluded();
		
		try {
			session = project.openSession(s.getCorpus(), s.getSession());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		setupColumns();
	}
	
	public ResultListingDataSource(Session session, ResultSet rs, ResultListing section) {
		this.resultSet = rs;
		this.session = session;
		this.invData = section;
		this.includeExcluded = section.isIncludeExcluded();
		setupColumns();
	}
	
	public ResultListingFormatType getFormat() {
		return invData.getFormat();
	}

	public void setResultSet(ResultSet rs) {
		this.resultSet = rs;
	}
	
	public void setListing(ResultListing listing) {
		this.invData = listing;
		setupColumns();
	}
	
	private void setupColumns() {
		int colIdx = 0;
		for(ResultListingField field:invData.getField()) {
			ScriptContainer sc = field.getFieldValue();
			final PhonScript ps = new BasicScript(sc.getScript());
			try {
				setColumnScript(colIdx, ps);
				
				// setup static column mappings
				final Map<String, Object> bindings = new HashMap<String, Object>();
				
				final PhonScriptContext ctx = ps.getContext();
				final Scriptable scope = ctx.getEvaluatedScope();
				final ScriptParameters params = ctx.getScriptParameters(scope);
//				final ScriptParam[] params = ps.getScriptParams();
				
				// setup script parameters
				for(ScriptParam param:params) {
					for(String paramId:param.getParamIds()) {
						
						Object paramVal = null;
						ScriptParameter savedParam = null;
						for(ScriptParameter sp:sc.getParam()) {
							if(sp.getName().equals(paramId)) {
								savedParam = sp;
								break;
							}
						}
						
						if(savedParam != null) {
							try {
								if(param.getParamType().equals("bool")) {
									paramVal = Boolean.parseBoolean(savedParam.getContent());
								} else if (param.getParamType().equals("enum")) {
									EnumScriptParam esp = (EnumScriptParam)param;
									EnumScriptParam.ReturnValue rVal = null;
									for(EnumScriptParam.ReturnValue v:esp.getChoices()) {
										if(v.toString().equals(savedParam.getContent())) {
											rVal = v;
											break;
										}
									}
									if(rVal != null) 
										paramVal = rVal;
									else
										paramVal = esp.getDefaultValue(paramId);
								} else {
									paramVal = savedParam.getContent();
								}
							} catch (Exception e) {
								LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
							}
						} else {
							paramVal = param.getDefaultValue(paramId);
						}
						
						bindings.put(paramId, paramVal);
					}
				}
				if(bindings.size() > 0)
					setColumnMappings(colIdx, bindings);
				colIdx++;
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	@Override
	public int getColumnCount() {
		int retVal = 0;
		
		if(invData.getFormat() == ResultListingFormatType.LIST)
			retVal = 2;
		else
			retVal = invData.getField().size();
		
		return retVal;
	}

	@Override
	public int getRowCount() {
		int retVal = 0;
		
		final int numResults = resultSet.numberOfResults(includeExcluded);
		if(invData.getFormat() == ResultListingFormatType.LIST)
			retVal = invData.getField().size() * numResults +
				(numResults - 1);
		else
			retVal = numResults;
		
		return retVal;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		Object retVal = null;
		if(invData.getFormat() == ResultListingFormatType.LIST) {
			final int rowsPerResult = invData.getField().size() + 1;
			final int result = row / rowsPerResult;
			final int fieldIdx = row % rowsPerResult;
			
			// space between results
			if(fieldIdx == rowsPerResult-1) return "";
			
			if(col == 0) {
				retVal = invData.getField().get(fieldIdx).getTitle();
			} else {
				retVal = super.getValueAt(result, fieldIdx);
			}
		} else {
			retVal = super.getValueAt(row, col);
		}
		return retVal;
	}

	@Override
	public String getColumnName(int col) {
		String retVal = super.getColumnName(col);
		
		if(invData.getFormat() == ResultListingFormatType.LIST) {
			if(col == 0) {
				retVal = "Field Name";
			} else if(col == 1) {
				retVal = "Value";
			}
		} else {
			retVal = invData.getField().get(col).getTitle();
		}
		
		return retVal;
	}
	
	public boolean isIncludeExcluded() {
		return includeExcluded;
	}

	public void setIncludeExcluded(boolean includeExcluded) {
		this.includeExcluded = includeExcluded;
	}
	
//	@Override
//	public void setColumnScript(int col, PhonScript script)
//		throws PhonScriptException {
//		// append default imports to script
//		final StringBuffer buffer = new StringBuffer();
////		for(String imp:scriptPkgImports) {
////			buffer.append(String.format("importPackage(%s)\n", imp));
////		}
////		
////		for(String imp:scriptClazzImports) {
////			buffer.append(String.format("importClass(%s)\n", imp));
////		}
//		
//		buffer.append(script);
//		
//		super.setColumnScript(col, buffer.toString(), mimetype);
//	}

	@Override
	public Map<String, Object> getMappingsAt(int row, int col) {
		final Map<String, Object> bindings = super.getMappingsAt(row, col);
		
		Result result = null;
		if(includeExcluded) {
			result = resultSet.getResult(row);
		} else {
			int rIdx = -1;
			for(Result r:resultSet) {
				if(!r.isExcluded()) rIdx++;
				if(rIdx == row) {
					result = r;
					break;
				}
			}
		}
		
		if(result == null) return bindings;
		
		Record record = session.getRecord(result.getRecordIndex());
		
		bindings.put("project", project);
		bindings.put("session", session);
		bindings.put("resultSet", resultSet);
		bindings.put("result", result);
		bindings.put("record", record);
		bindings.put("recordIndex", result.getRecordIndex());
		bindings.put("table", this);
		
		return bindings;
	}

	@Override
	public String getColumnTitle(int col) {
		return getColumnName(col);
	}
	
}
