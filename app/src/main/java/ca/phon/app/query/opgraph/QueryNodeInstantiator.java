package ca.phon.app.query.opgraph;

import ca.gedge.opgraph.library.instantiators.Instantiator;
import ca.phon.query.script.QueryName;

public class QueryNodeInstantiator implements Instantiator<QueryNode> {

	@Override
	public QueryNode newInstance(Object... params)
			throws InstantiationException {
		if(params.length < 1) 
			throw new InstantiationException("Incorrect number of parameters");
		final Object obj = params[0];
		if(!(obj instanceof QueryNodeData)) 
			throw new InstantiationException("Incorrect node data type");
		final QueryNodeData queryNodeData = (QueryNodeData)obj;
		
		QueryNode retVal = new QueryNode(queryNodeData.getQueryScript());
		retVal.setName("Query : " + queryNodeData.getQueryScript().getExtension(QueryName.class).getName());
		return retVal;
	}

}
