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
package ca.phon.query.analysis;

import java.io.*;
import java.util.logging.*;

import org.mozilla.javascript.*;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.functor.AnalysisStep;
import ca.phon.script.*;

public class ScriptReportStep implements AnalysisStep<String, QueryAnalysisResult> {

	private static final Logger LOGGER = Logger
			.getLogger(ScriptReportStep.class.getName());
	
	private final PhonScript script;
	
	public ScriptReportStep(PhonScript script) {
		this.script = script;
	}
	
	public ScriptReportStep(String script) {
		this(new BasicScript(script));
	}
	
	@Override
	public String op(QueryAnalysisResult obj) {
		final PhonScriptContext ctx = script.getContext();
		String retVal = "";
		
		try {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ctx.redirectStdOut(new PrintStream(bout, false, "UTF-8"));
			
			// create scope with default imports and require() function
			final Scriptable scope = ctx.createImporterScope();
			ctx.enter();
			ScriptableObject.putProperty(scope, "out", ctx.getStdOut());
			ScriptableObject.putProperty(scope, "csvWriter", new CSVWriter(new OutputStreamWriter(ctx.getStdOut(), "UTF-8")));
			ScriptableObject.putProperty(scope, "queryAnalysisResult", obj);
			ctx.exit();
			
			ctx.exec(scope);
			
			retVal = bout.toString("UTF-8");
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return retVal;
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public Class<QueryAnalysisResult> getParameterType() {
		return QueryAnalysisResult.class;
	}

}
