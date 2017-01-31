package ca.phon.app.opgraph.nodes.table;

import ca.gedge.opgraph.library.instantiators.Instantiator;

public class AddColumnNodeInstantiator implements Instantiator<AddColumnNode> {

	@Override
	public AddColumnNode newInstance(Object... params) throws InstantiationException {
		if(params.length < 1) 
			throw new InstantiationException("Incorrect number of parameters");
		final Object obj = params[0];
		if(!(obj instanceof TableScriptNodeData)) 
			throw new InstantiationException("Incorrect node data type");
		final TableScriptNodeData tableScriptNodeData = (TableScriptNodeData)obj;
		
		AddColumnNode retVal = new AddColumnNode(tableScriptNodeData.getPhonScript());
		retVal.setName(tableScriptNodeData.name);
		return retVal;
	}

}
