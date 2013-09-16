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
package ca.phon.query.script;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.script.PhonScript;
import ca.phon.script.params.EnumScriptParam;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.rewrite.ScriptRewriter;

/**
 * Holds the text for a query script.
 * Handles methods for parsing the script paramaters
 * and default tags.
 * 
 */
public class QueryScript extends PhonScript {
	
	private final static Logger LOGGER = Logger.getLogger(QueryScript.class.getName());
	
	/**
	 * Constructor
	 * 
	 */
	public QueryScript() {
		super();
	}
	
	public QueryScript(String script) {
		super(script);
		
	}
	
	public QueryScript(File file) {
		super(file);
	}

	/**
	 * Get script text with form data replaced with
	 * hard-coded vars.
	 * @return script
	 */
	public String getCannedQuery(String name, String comments, boolean rewrite) {
		String retVal = getScript(false);

		retVal = replaceScriptParams(retVal);

		retVal = this.getCannedSearchForm(name, comments, getScriptParams())
				+ "\n" + retVal;
		
		if(rewrite) {
			try {
				retVal = ScriptRewriter.rewriteScript(retVal);

			} catch (Exception e) {
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}

		return retVal;
	}
	
	/**
	 * Generate a param form with the given comments and
	 * param listing.
	 *
	 */
	private String getCannedSearchForm(
			String scriptName, String comments, ScriptParam[] params) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("/*\n");
		buffer.append("params = ");

		// top info section
		buffer.append("{separator, \"" + scriptName + "\"},\n");
		buffer.append("{label, \"<html><body><p>" + comments + "</p></body></html>\", \"<html><i>Comments</i></html>\"},\n");

		// params
		buffer.append("{separator, \"Params\", true}");
		for(ScriptParam param:params) {
			for(String paramId:param.getParamIds()) {
				buffer.append(",\n");
				buffer.append("{label, \"" + param.getValue(paramId).toString() + "\", \"<html><i>" + paramId + "</i></html>\"}");
			}
		}
		buffer.append(";\n");
		buffer.append("*/\n");

		return buffer.toString();
	}

	/**
	 * Convert script params into a variable def
	 * list.
	 */
	private String getParamDef(ScriptParam[] params) {
//		ScriptParam[] params = getScriptParams();

		StringBuffer buffer = new StringBuffer();

		for(ScriptParam param:params) {

			for(String paramId:param.getParamIds()) {

				String varLine = "var " + paramId + " = ";

				if(param.getParamType().equals("string")) {
					varLine += "\"" + param.getValue(paramId).toString() + "\"";
				} else if(param.getParamType().equals("enum")) {
					EnumScriptParam.ReturnValue paramVal =
							(EnumScriptParam.ReturnValue)param.getValue(paramId);
					varLine += "new Object();\n";
					varLine += paramId + ".index = " + paramVal.getIndex() + ";\n";
					varLine += paramId + ".value = \"" + paramVal.toString() + "\"";
				} else {
					varLine += param.getValue(paramId).toString();
				}

				varLine += ";\n";
				
				buffer.append(varLine);
			}

		}

		return buffer.toString();
	}

	/**
	 * Replace script param form with hard-coded parameters.
	 *
	 * @return a new search script with the script params replaced
	 */
	public String replaceScriptParams(String s) {
		String scriptParamsRegex = "/\\*" + //
			"\\s*(params.*;)\\s*" +
			"\\*/";
		Pattern p = Pattern.compile(scriptParamsRegex, Pattern.MULTILINE|Pattern.DOTALL);
		Matcher m = p.matcher(s);

		String retVal = s;

		if(m.find()) {
			retVal = m.replaceFirst(getParamDef(getScriptParams()));
		}

		return retVal;
	}
	
	public boolean hasQueryRecord() {
		return hasFunction("query_record", 1);
	}
	
	public boolean hasBeginSearch() {
		return hasFunction("begin_search", 1);
	}
	
	public boolean hasEndSearch() {
		return hasFunction("end_search", 1);
	}

	@Override
	protected List<String> defaultPackageImports() {
		final ArrayList<String> retVal = new ArrayList<String>();
		retVal.addAll(super.defaultPackageImports());
		retVal.add("Packages.ca.phon.engines.search.script");
		return retVal;
	}

	@Override
	public List<String> scriptFolders() {
		final List<String> retVal = new ArrayList<String>();
		retVal.add(QueryScriptLibrary.SYSTEM_SCRIPT_FOLDER);
		retVal.add(QueryScriptLibrary.USER_SCRIPT_FOLDER);
		return retVal;
	}

	
	
}
