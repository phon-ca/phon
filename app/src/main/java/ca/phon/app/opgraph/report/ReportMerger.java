package ca.phon.app.opgraph.report;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.phon.app.opgraph.editor.NodeEditorSettings;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.project.Project;

/**
 * Class to produce a single, mega-report, from all available
 * report documents.
 *
 */
public class ReportMerger {

	private final static String TEMPLATE_FILE = "ca/phon/app/opgraph/report/AllReportsTemplate.xml";
	
	private Project project;
	
	public ReportMerger() {
		super();
	}
	
	public ReportMerger(Project project) {
		super();
		this.project = project;
	}
	
	public void setProject(Project project) {
		this.project = project;
	}
	
	public Project getProject() {
		return this.project;
	}
	
	// recursive method to update all node ids in a graph
	private void updateIds(OpGraph graph) {
		for(OpNode node:graph.getVertices()) {
			final UUID uuid = UUID.randomUUID();
			node.setId(Long.toHexString(uuid.getLeastSignificantBits()));
			
			if(node instanceof MacroNode) {
				updateIds(((MacroNode)node).getGraph());
			}
		}
	}
	
	private boolean checkName(String name) {
		if(name.contains("legacy") || name.contains("deprecated")) return false;
		if(name.startsWith(".") || name.startsWith("~") || name.startsWith("__")) return false;
		return true;
	}
	
	private OpGraph loadTemplate() throws IOException {
		final InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_FILE);
		if(is != null) {
			OpGraph retVal = OpgraphIO.read(is);
			updateIds(retVal);
			return retVal;
		} else {
			throw new FileNotFoundException(TEMPLATE_FILE);
		}
	}
	
	/**
	 * Used to create a macro node for a category of reports (e.g., Stock, User, Project)
	 * 
	 * @param title
	 * @return
	 */
	private MacroNode createReportCategoryMacroNode() throws IOException {
		final OpGraph graph = loadTemplate();
		
		final MacroNode node = new MacroNode(graph);
		
		// publish inputs
		final OpNode queryHistoryNode = 
				graph.getNodesByName("Query History").stream().findFirst().orElse(null);
		// should not happen
		if(queryHistoryNode == null)
			throw new IllegalArgumentException("Report template does not contain 'Query History' node");

		return node;
	}
	
	private MacroNode addReportCategoryMacroNode(OpGraph document, String title) throws IOException, ItemMissingException, VertexNotFoundException, CycleDetectedException {
		final MacroNode retVal = createReportCategoryMacroNode();
		retVal.setName(title);
		
		// add macro node to document
		document.add(retVal);
		
		connectMacroNode(document, retVal);
		
		// make macro node optional
		final WizardExtension ext = document.getExtension(WizardExtension.class);
		if(ext != null) {
			ext.addOptionalNode(retVal);
			ext.setOptionalNodeDefault(retVal, true);
		}
		
		return retVal;
	}
	
	private void connectMacroNode(OpGraph parent, MacroNode macroNode) throws ItemMissingException, VertexNotFoundException, CycleDetectedException {
		final OpGraph macroGraph = macroNode.getGraph();
		
		// find object nodes in reportA
		final OpNode queryHistoryNodeA = 
				parent.getNodesByName("Query History")
					.stream().findFirst().orElse(null);
		if(queryHistoryNodeA == null)
			throw new IllegalArgumentException("No 'Query History' node found in destination graph");
		
		final OpNode queryHistoryNodeB = 
				macroGraph.getNodesByName("Query History")
				.stream().findFirst().orElse(null);
		if(queryHistoryNodeB == null)
			throw new IllegalArgumentException("No 'Query History' node found in source graph");
		
		// find 'Query' node in reportA
		final OpNode uuidNode = parent.getNodesByName("UUID#toString()")
				.stream().findFirst().orElse(null);
		if(uuidNode == null)
			throw new IllegalArgumentException("'UUID#toString()' node not found in source graph");
		
		// collect fields
		final InputField queryHistoryProjectInputField = queryHistoryNodeB.getInputFieldWithKey("project");
		final InputField queryHistoryUUIDInputField = queryHistoryNodeB.getInputFieldWithKey("queryId");
		
		final InputField projectInputField = macroNode.publish("project", queryHistoryNodeB, queryHistoryProjectInputField);
		final InputField queryIdInputField = macroNode.publish("queryId", queryHistoryNodeB, queryHistoryUUIDInputField);

		final OutputField projectOutputField = queryHistoryNodeA.getOutputFieldWithKey("project");
		final OutputField queryUUIDField = uuidNode.getOutputFieldWithKey("value");
		
		// create and add links
		final OpLink projectLink = new OpLink(queryHistoryNodeA, projectOutputField, macroNode, projectInputField);
		final OpLink uuidLink = new OpLink(uuidNode, queryUUIDField, macroNode, queryIdInputField);
		parent.add(projectLink);
		parent.add(uuidLink);
	}
	
	public OpGraph createAllReportsGraph() throws IOException, IllegalArgumentException, ItemMissingException, VertexNotFoundException, CycleDetectedException {
		final OpGraph retVal = loadTemplate();
		
		NodeEditorSettings nes = retVal.getExtension(NodeEditorSettings.class);
		if(nes == null) {
			nes = new NodeEditorSettings();
			retVal.putExtension(NodeEditorSettings.class, nes);
		}
		nes.setGenerated(true);
		
		final WizardExtension ext = retVal.getExtension(WizardExtension.class);
		if(ext != null)
			ext.setWizardTitle("All Reports");
		
		retVal.setId("root");
		
		final ReportLibrary library = new ReportLibrary();
		final MacroNode stockReportsNode = addReportCategoryMacroNode(retVal, "Stock Reports");
		for(URL reportURL:library.getStockGraphs()) {
			final String name = reportURL.getFile();
			if(!checkName(name)) continue;
			
			final OpGraph graph = OpgraphIO.read(reportURL.openStream());
			if(graphGenerated(graph)) continue;
			
			updateIds(graph);
			
			addReport(retVal, stockReportsNode.getGraph(), graph);
		}
		
		if(library.getUserGraphs().iterator().hasNext()) {
			final MacroNode userReportsNode = addReportCategoryMacroNode(retVal, "User Reports");
			for(URL reportURL:library.getUserGraphs()) {
				final String name = reportURL.getFile();
				if(!checkName(name)) continue;
				
				final OpGraph graph = OpgraphIO.read(reportURL.openStream());
				if(graphGenerated(graph)) continue;
				updateIds(graph);
				
				addReport(retVal, userReportsNode.getGraph(), graph);
			}
		}
		
		final Project proj = getProject();
		if(proj != null && library.getProjectGraphs(proj).iterator().hasNext()) {
			final MacroNode projectReportsNode = addReportCategoryMacroNode(retVal, "Project Reports");
			for(URL reportURL:library.getProjectGraphs(proj)) {
				final String name = reportURL.getFile();
				if(!checkName(name)) continue;
				
				final OpGraph graph = OpgraphIO.read(reportURL.openStream());
				if(graphGenerated(graph)) continue;
				updateIds(graph);
				
				addReport(retVal, projectReportsNode.getGraph(), graph);
			}
		}
		
		return retVal;
	}
	
	private boolean graphGenerated(OpGraph graph) {
		final NodeEditorSettings settings = graph.getExtension(NodeEditorSettings.class);
		return (settings != null && settings.isGenerated());
	}
	
	/**
	 * Wrap reportB in a {@link MacroNode} and add it to reportA
	 * as a new optional node.  Any nodes marked as optional or
	 * available in reportB will also be marked as such in the
	 * {@link MacroNode} of reportA.  Reports templates are not
	 * merged, instead the default report template is available.
	 * 
	 * @param document
	 * @param report
	 * @return reference to reportA
	 * @throws IllegalArgumentException
	 * @throws ItemMissingException
	 * @throws CycleDetectedException 
	 * @throws VertexNotFoundException 
	 */
	private OpGraph addReport(OpGraph document, OpGraph macroGraph, OpGraph report)
		throws IllegalArgumentException, ItemMissingException, VertexNotFoundException, CycleDetectedException {
		final WizardExtension extA = document.getExtension(WizardExtension.class);
		if(extA == null || !(extA instanceof ReportWizardExtension))
			throw new IllegalArgumentException("Destination graph is not a report document");
		
		final WizardExtension extB = report.getExtension(WizardExtension.class);
		if(extB == null || !(extB instanceof ReportWizardExtension))
			throw new IllegalArgumentException("Source graph is not a report document");
		
		// publish input fields of Query History node in macro
		final MacroNode macroNode = new MacroNode(report);
		macroNode.setName(extB.getWizardTitle());

		// add macroNode to reportA
		macroGraph.add(macroNode);
		extA.addOptionalNode(macroNode);
		extA.setOptionalNodeDefault(macroNode, true);
		
		connectMacroNode(macroGraph, macroNode);
		
		// setup optionals for reportA
		for(OpNode optionalNode:extB.getOptionalNodes()) {
			extA.addOptionalNode(optionalNode);
			extA.setOptionalNodeDefault(optionalNode, extB.getOptionalNodeDefault(optionalNode));
		}
		
		// setup settings nodes
		for(OpNode settingsNode:extB) {
			extA.addNode(settingsNode);
			extA.setNodeTitle(settingsNode, macroNode.getName() + ": " + extB.getNodeTitle(settingsNode));
			extA.setNodeMessage(settingsNode, extB.getNodeMessage(settingsNode));
			extA.setNodeForced(settingsNode, extB.isNodeForced(settingsNode));
		}
		
		return document;
	}

}
