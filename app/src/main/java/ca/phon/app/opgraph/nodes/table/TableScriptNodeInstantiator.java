package ca.phon.app.opgraph.nodes.table;

import ca.gedge.opgraph.library.instantiators.Instantiator;

public class TableScriptNodeInstantiator implements Instantiator<TableScriptNode> {

	@Override
	public TableScriptNode newInstance(Object... params) throws InstantiationException {
		if(params.length < 1) 
			throw new InstantiationException("Incorrect number of parameters");
		final Object obj = params[0];
		if(!(obj instanceof TableScriptNodeData)) 
			throw new InstantiationException("Incorrect node data type");
		final TableScriptNodeData tableScriptNodeData = (TableScriptNodeData)obj;
		
		TableScriptNode retVal = new TableScriptNode(tableScriptNodeData.getPhonScript());
		retVal.setName("Table Script : " + tableScriptNodeData.name);
		return retVal;
	}

}
