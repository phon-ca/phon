package ca.phon.opgraph.nodes.objects;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.reflect.ClassNode;
import ca.phon.workspace.Workspace;

@OpNodeInfo(
		name="Workspace",
		description="ca.phon.workspace.Workspace",
		category="Objects")
public class WorkspaceNode extends ClassNode {

	public WorkspaceNode() {
		super(Workspace.class);
		
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final InputField valueInput = super.getInputFieldWithKey("value");
		context.put(valueInput, Workspace.userWorkspace());
		
		super.operate(context);
	}
	
}
