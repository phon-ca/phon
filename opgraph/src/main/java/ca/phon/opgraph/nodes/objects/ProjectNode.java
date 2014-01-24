package ca.phon.opgraph.nodes.objects;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.reflect.ClassNode;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;

@OpNodeInfo(
		name="Project",
		description="ca.phon.project.Project object",
		category="Objects")
public class ProjectNode extends ClassNode {

	public ProjectNode() {
		super(Project.class);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		super.operate(context);
	}
	
}
