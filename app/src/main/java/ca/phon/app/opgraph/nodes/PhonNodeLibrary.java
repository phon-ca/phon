package ca.phon.app.opgraph.nodes;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.components.canvas.NodeStyle;
import ca.gedge.opgraph.library.NodeLibrary;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.phon.app.opgraph.analysis.AnalysisLibrary;
import ca.phon.app.opgraph.macro.MacroLibrary;
import ca.phon.app.opgraph.nodes.query.QueryNode;
import ca.phon.app.opgraph.nodes.query.QueryNodeData;
import ca.phon.app.opgraph.nodes.query.QueryNodeInstantiator;
import ca.phon.app.opgraph.nodes.table.AddColumnNode;
import ca.phon.app.opgraph.nodes.table.AddColumnNodeInstantiator;
import ca.phon.app.opgraph.nodes.table.TableOpNode;
import ca.phon.app.opgraph.nodes.table.TableScriptNode;
import ca.phon.app.opgraph.nodes.table.TableScriptNodeData;
import ca.phon.app.opgraph.nodes.table.TableScriptNodeInstantiator;
import ca.phon.app.opgraph.report.ReportLibrary;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.util.resources.ResourceLoader;

/**
 * Adds Phon's custom nodes to the {@link OpGraph} {@link NodeLibrary}.
 *
 */
public class PhonNodeLibrary {

	public static void install(NodeLibrary library) {
		final PhonNodeLibrary nodeLib = new PhonNodeLibrary(library);
		nodeLib.addAllNodes();
		nodeLib.setupNodeStyles();
	}

	private final static Logger LOGGER = Logger.getLogger(PhonNodeLibrary.class.getName());

	private final NodeLibrary nodeLibrary;

	public PhonNodeLibrary(NodeLibrary nodeLibrary) {
		super();
		this.nodeLibrary = nodeLibrary;
	}

	public void addAllNodes() {
		addMacroNodes();
		addAnalysisNodes();
		addReportNodes();
		addQueryNodes();
		addTableScriptNodes();
		addAddColumnScriptNodes();
	}

	public void addMacroNodes() {
		final MacroLibrary library = new MacroLibrary();
		library.getStockGraphs().forEach(this::addMacroNodeToLibrary);
		library.getUserGraphs().forEach(this::addMacroNodeToLibrary);
	}

	public void addAnalysisNodes() {
		final AnalysisLibrary library = new AnalysisLibrary();
		library.getStockGraphs().forEach(this::addAnalysisNodeToLibrary);
		library.getUserGraphs().forEach(this::addAnalysisNodeToLibrary);
	}

	public void addReportNodes() {
		final ReportLibrary library = new ReportLibrary();
		library.getStockGraphs().forEach(this::addReportNodeToLibrary);
		library.getUserGraphs().forEach(this::addAnalysisNodeToLibrary);
	}

	public void addQueryNodes() {
		final QueryScriptLibrary library = new QueryScriptLibrary();
		library.stockScriptFiles().forEach(this::addQueryScriptToLibrary);
		library.userScriptFiles().forEach(this::addQueryScriptToLibrary);
	}

	public void addTableScriptNodes() {
		final ResourceLoader<URL> tableScripts = TableScriptNode.getTableScriptResourceLoader();
		final Iterator<URL> itr = tableScripts.iterator();
		while(itr.hasNext()) {
			final URL tableScriptURL = itr.next();
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(tableScriptURL.openStream(), "UTF-8"))) {
				StringBuffer buffer = new StringBuffer();
				String line = null;
				while((line = reader.readLine()) != null) {
					buffer.append(line).append("\n");
				}
				final PhonScript script = new BasicScript(buffer.toString());

				final String name = FilenameUtils.getBaseName(
						URLDecoder.decode(tableScriptURL.getFile(), "UTF-8"));
				addTableScriptToLibrary(name, "", script);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
	}

	public void addAddColumnScriptNodes() {
		final ResourceLoader<URL> addColumnScripts = AddColumnNode.getAddColumnScriptResourceLoader();
		final Iterator<URL> itr = addColumnScripts.iterator();
		while(itr.hasNext()) {
			final URL addColumnScriptURL = itr.next();
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(addColumnScriptURL.openStream(), "UTF-8"))) {
				StringBuffer buffer = new StringBuffer();
				String line = null;
				while((line = reader.readLine()) != null) {
					buffer.append(line).append("\n");
				}
				final PhonScript script = new BasicScript(buffer.toString());

				final String name = FilenameUtils.getBaseName(
						URLDecoder.decode(addColumnScriptURL.getFile(), "UTF-8"));
				addAddColumnScriptToLibrary("Add column: " + name, "", script);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
	}

	public NodeLibrary getNodeLibrary() {
		return this.nodeLibrary;
	}

	public void setupNodeStyles() {
		final NodeStyle queryStyle = new NodeStyle(NodeStyle.DEFAULT);
		queryStyle.NodeIcon = IconManager.getInstance().getIcon("actions/system-search", IconSize.SMALL);
		queryStyle.NodeBackgroundColor = new Color(144, 195, 212, 180);
		queryStyle.NodeNameTopColor = new Color(144, 195, 212);
		queryStyle.NodeNameBottomColor = queryStyle.NodeNameTopColor.darker();

		final NodeStyle scriptStyle = new NodeStyle(NodeStyle.DEFAULT);
		scriptStyle.NodeIcon = IconManager.getInstance().getIcon("mimetypes/text-x-script", IconSize.SMALL);

		final NodeStyle tableStyle = new NodeStyle(NodeStyle.DEFAULT);
		tableStyle.NodeIcon = IconManager.getInstance().getIcon("misc/table", IconSize.SMALL);

		NodeStyle.installStyleForNode(QueryNode.class, queryStyle);
		NodeStyle.installStyleForNode(PhonScriptNode.class, scriptStyle);
		NodeStyle.installStyleForNode(TableScriptNode.class, scriptStyle);
		NodeStyle.installStyleForNode(TableOpNode.class, tableStyle);
	}

	private void addTableScriptToLibrary(String name, String comment, PhonScript tableScript) {
		try {
			final URI tableNodeClassURI = new URI("class", TableScriptNode.class.getName(), name);
			final TableScriptNodeInstantiator instantiator = new TableScriptNodeInstantiator();

			final TableScriptNodeData nodeData = new TableScriptNodeData(tableScript, tableNodeClassURI, name,
					comment, "Table", instantiator);
			getNodeLibrary().put(nodeData);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	private void addAddColumnScriptToLibrary(String name, String comment, PhonScript addColumnScript) {
		try {
			final URI addColumnNodeClassURI = new URI("class", AddColumnNode.class.getName(), name);
			final AddColumnNodeInstantiator instantiator = new AddColumnNodeInstantiator();

			final TableScriptNodeData nodeData = new TableScriptNodeData(addColumnScript, addColumnNodeClassURI, name,
					comment, "Table", instantiator);
			getNodeLibrary().put(nodeData);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private void addQueryScriptToLibrary(QueryScript script) {
		final QueryName qn = script.getExtension(QueryName.class);
		final String name = (qn != null ? qn.getName() : "<unknown>");
		try {
			final URI queryNodeClassURI = new URI("class", QueryNode.class.getName(), name);
			final QueryNodeInstantiator instantiator = new QueryNodeInstantiator();

			final String description =
					"Add " + name + " query to graph.";

			final QueryNodeData nodeData = new QueryNodeData(script, queryNodeClassURI,
					name, description, "Query", instantiator);
			getNodeLibrary().put(nodeData);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	private void addMacroNodeToLibrary(URL url) {
		try {
			String filename = URLDecoder.decode(url.getFile(), "UTF-8");
			String name = FilenameUtils.getBaseName(filename);

			final URI uri = new URI("class", MacroNode.class.getName(), name);

			final MacroNodeData nodeData = new MacroNodeData(url, uri, name, "", "Macro", new MacroNodeInstantiator());
			getNodeLibrary().put(nodeData);
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private void addAnalysisNodeToLibrary(URL url) {
		try {
			String filename = URLDecoder.decode(url.getFile(), "UTF-8");
			String name = FilenameUtils.getBaseName(filename);

			final URI uri = new URI("class", MacroNode.class.getName(), name);

			final MacroNodeData nodeData = new MacroNodeData(url, uri, name, "", "Analysis", new AnalysisNodeInstantiator());
			getNodeLibrary().put(nodeData);
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private void addReportNodeToLibrary(URL url) {
		try {
			String filename = URLDecoder.decode(url.getFile(), "UTF-8");
			String name = FilenameUtils.getBaseName(filename);

			final URI uri = new URI("class", MacroNode.class.getName(), name);

			final MacroNodeData nodeData = new MacroNodeData(url, uri, name, "", "Report", new ReportNodeInstantiator());
			getNodeLibrary().put(nodeData);
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}


}
