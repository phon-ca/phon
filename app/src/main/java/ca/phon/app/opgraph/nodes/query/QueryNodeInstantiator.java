/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.nodes.query;

import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.opgraph.library.instantiators.Instantiator;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;

public class QueryNodeInstantiator implements Instantiator<QueryNode> {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(QueryNodeInstantiator.class.getName());

	@Override
	public QueryNode newInstance(Object... params)
			throws InstantiationException {
		if(params.length < 1)
			throw new InstantiationException("Incorrect number of parameters");
		final Object obj = params[0];
		if(!(obj instanceof QueryNodeData))
			throw new InstantiationException("Incorrect node data type");
		final QueryNodeData queryNodeData = (QueryNodeData)obj;

		final QueryScript templateScript = queryNodeData.getQueryScript();
		final QueryScript queryScript = new QueryScript(templateScript.getScript());
		queryScript.putExtension(QueryName.class, templateScript.getExtension(QueryName.class));

		// load parameters
		try {
			final ScriptParameters templateParams =
					templateScript.getContext().getScriptParameters(templateScript.getContext().getEvaluatedScope());
			final ScriptParameters scriptParams =
					queryScript.getContext().getScriptParameters(queryScript.getContext().getEvaluatedScope());

			for(ScriptParam param:scriptParams) {
				for(String paramId:param.getParamIds()) {
					scriptParams.setParamValue(paramId, templateParams.getParamValue(paramId));
				}
			}
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		QueryNode retVal = new QueryNode(queryScript);
		retVal.setName("Query : " + queryScript.getExtension(QueryName.class).getName());
		return retVal;
	}

}
