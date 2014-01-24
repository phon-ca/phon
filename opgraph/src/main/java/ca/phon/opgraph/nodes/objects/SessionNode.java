package ca.phon.opgraph.nodes.objects;

import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.nodes.reflect.ClassNode;
import ca.phon.session.Session;

@OpNodeInfo(
		name="Session",
		description="ca.phon.session.Session object",
		category="Objects")
public class SessionNode extends ClassNode {

	public SessionNode() {
		super(Session.class);
	}
	
}
