package ca.phon.app.query.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.io.OpGraphSerializer;
import ca.gedge.opgraph.io.OpGraphSerializerFactory;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.project.Project;

/**
 * Represents and assessment implements using an {@link OpGraph}.
 */
public class Assessment implements IExtendable, Runnable {
	
	private final static Logger LOGGER = Logger.getLogger(Assessment.class.getName());
	
	private File opGraphFile;
	
	private OpGraph opGraph;
	
	private String name;
	
	private OpContext resultContext;
	
	private Map<String, Object> bindings = new LinkedHashMap<>();
	
	private final ExtensionSupport extSupport = new ExtensionSupport(Assessment.class, this);
	
	public static OpGraph createDefaultOpGraph() {
		OpGraph graph = new OpGraph();
		
		// add a project node
		final ObjectNode projectNode = new ObjectNode(Project.class);
		projectNode.setContextKey("_project");
		
		graph.add(projectNode);
		
		return graph;
	}

	public Assessment() {
		this(createDefaultOpGraph());
	}
	
	public Assessment(File file) {
		super();
		this.opGraphFile = file;
	}
	
	public Assessment(OpGraph graph) {
		super();
		this.opGraph = graph;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public OpGraph getOpGraph() {
		if(this.opGraph == null && this.opGraphFile != null) {
			try {
				OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
				this.opGraph = serializer.read(new FileInputStream(this.opGraphFile));
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		return this.opGraph;
	}

	public void setOpGraph(OpGraph graph) {
		this.opGraph = graph;
	}
	
	public Map<String, Object> getBindings() {
		return this.bindings;
	}
	
	/**
	 * Context resulting from last call to {@link #run()}
	 * 
	 * @return context
	 */
	public OpContext getResultContext() {
		return this.resultContext;
	}
	
	@Override
	public void run() {
		try {
			resultContext = exec();
		} catch (ProcessingException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Execute assessment and return resulting context.
	 * 
	 * @returns context
	 */
	public OpContext exec() throws ProcessingException {
		final OpGraph graph = getOpGraph();
		final Processor processor = new Processor(graph);
		final OpContext ctx = processor.getContext();
		
		for(String key:getBindings().keySet()) {
			ctx.put(key, getBindings().get(key));
		}
	
		processor.stepAll();
		
		return ctx;
	}
	
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
}
