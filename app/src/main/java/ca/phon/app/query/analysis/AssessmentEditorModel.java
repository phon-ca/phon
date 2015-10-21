package ca.phon.app.query.analysis;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.app.opgraph.nodes.project.SessionSelectorNode;
import ca.phon.app.opgraph.nodes.query.QueryNode;
import ca.phon.app.opgraph.nodes.query.QueryNodeData;
import ca.phon.app.opgraph.nodes.query.QueryNodeInstantiator;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;

/**
 * 
 */
public class AssessmentEditorModel extends GraphEditorModel {
	
	private final static Logger LOGGER = Logger.getLogger(AssessmentEditorModel.class.getName());
	
	private final Project project;
	
	public AssessmentEditorModel(Project project) {
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
		Consumer<QueryScript> addToLibrary = (QueryScript script) -> {
			final QueryName qn = script.getExtension(QueryName.class);
			final String name = (qn != null ? qn.getName() : "<unknown>");
			try {
				final URI queryNodeClassURI = new URI("class", QueryNode.class.getName(), qn.getName());
				final QueryNodeInstantiator instantiator = new QueryNodeInstantiator();
				
				final QueryNodeData nodeData = new QueryNodeData(script, queryNodeClassURI, name, "", "Query", instantiator);
				super.getNodeLibrary().getLibrary().put(nodeData);
			} catch (URISyntaxException e) {
				
			}
		};
		
		final QueryScriptLibrary library = new QueryScriptLibrary();
		library.stockScriptFiles().forEach(addToLibrary);
		library.userScriptFiles().forEach(addToLibrary);
		library.pluginScriptFiles(getProject()).forEach(addToLibrary);
	}
	
	private void addReportNodes() {
		
	}

}
