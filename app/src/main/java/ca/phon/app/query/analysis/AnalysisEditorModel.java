package ca.phon.app.query.analysis;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.app.query.opgraph.QueryNode;
import ca.phon.app.query.opgraph.QueryNodeData;
import ca.phon.app.query.opgraph.QueryNodeInstantiator;
import ca.phon.app.query.opgraph.SessionSelectorNode;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;
import ca.phon.util.resources.ResourceLoader;

/**
 * 
 */
public class AnalysisEditorModel extends GraphEditorModel {
	
	private final static Logger LOGGER = Logger.getLogger(AnalysisEditorModel.class.getName());
	
	private final Project project;
	
	public AnalysisEditorModel(Project project) {
		super();
		
		this.project = project;
		
		addQueryNodes();
		addReportNodes();
		
		setupDefaultGraph();
	}
	
	public Project getProject() {
		return this.project;
	}
	
	private void setupDefaultGraph() {
		final OpGraph retVal = getDocument().getGraph();
		
		// project node
		final ObjectNode projectNode = new ObjectNode(Project.class);
		projectNode.setContextKey("_project");
		retVal.add(projectNode);
		
		// session selection
		final SessionSelectorNode selectorNode = new SessionSelectorNode();
		retVal.add(selectorNode);
		
		// query node
		final QueryScriptLibrary library = new QueryScriptLibrary();
		final List<QueryScript> phonesScripts = library.findScriptsWithName("Phones");
		if(phonesScripts.size() > 0) {
			final QueryScript phonesScript = phonesScripts.get(0);
			
			try {
				final ScriptParameters scriptParams = 
						phonesScript.getContext().getScriptParameters(phonesScript.getContext().getEvaluatedScope());
				scriptParams.setParamValue("filters.primary.filter", "\\w");
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			
			final QueryNode queryNode = new QueryNode(phonesScript);
			retVal.add(queryNode);
			
			queryNode.setName("Query : Phones");
			
			// add connection
			try {
				final OpLink projectLink = new OpLink(projectNode, projectNode.getOutputFieldWithKey("obj"), 
						selectorNode, selectorNode.getInputFieldWithKey("project"));
				retVal.add(projectLink);

				final OpLink link = new OpLink(selectorNode, selectorNode.getOutputFieldWithKey("selected sessions"), 
						queryNode, queryNode.getInputFieldWithKey("record containers"));
				retVal.add(link);
				
				final OpLink projectLink2 = new OpLink(selectorNode, selectorNode.getOutputFieldWithKey("project"), 
						queryNode, queryNode.getInputFieldWithKey("project"));
				retVal.add(projectLink2);
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	private void addQueryNodes() {
		final QueryScriptLibrary library = new QueryScriptLibrary();
		try {
			final ResourceLoader<QueryScript> stockScriptLoader = library.stockScriptFiles();
			final Iterator<QueryScript> stockScriptItr = stockScriptLoader.iterator();
			while(stockScriptItr.hasNext()) {
				final QueryScript script = stockScriptItr.next();
				
				final QueryName qn = script.getExtension(QueryName.class);
				final String name = (qn != null ? qn.getName() : "<unknown>");
				final URI queryNodeClassURI = new URI("class", QueryNode.class.getName(), qn.getName());
				final QueryNodeInstantiator instantiator = new QueryNodeInstantiator();
				
				final QueryNodeData nodeData = new QueryNodeData(script, queryNodeClassURI, name, "", "Query", instantiator);
				super.getNodeLibrary().getLibrary().put(nodeData);
			}
			for(NodeData data:getNodeLibrary().getLibrary()){
				System.out.println(data.category + " " + data.name);
			}
		} catch (URISyntaxException e) {
			
		}
	}
	
	private void addReportNodes() {
		
	}

}
