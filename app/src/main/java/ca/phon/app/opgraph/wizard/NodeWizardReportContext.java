package ca.phon.app.opgraph.wizard;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;

public class NodeWizardReportContext {
	
	private final VelocityContext context;
	
	public NodeWizardReportContext() {
		super();
		
		final ToolManager toolManager = new ToolManager();
		final ToolContext toolContext = toolManager.createContext();
		
		this.context = new VelocityContext(toolContext);
	}

	public boolean containsKey(Object key) {
		return context.containsKey(key);
	}

	public Object get(String key) {
		return context.get(key);
	}

	public Object[] getKeys() {
		return context.getKeys();
	}

	public Object put(String key, Object value) {
		return context.put(key, value);
	}

	public Object remove(Object key) {
		return context.remove(key);
	}
	
	VelocityContext velocityContext() {
		return this.context;
	}

}
