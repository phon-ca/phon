/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.opgraph.library;

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

import org.apache.commons.io.FilenameUtils;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.analysis.AnalysisLibrary;
import ca.phon.app.opgraph.analysis.StockAnalysisHandler;
import ca.phon.app.opgraph.macro.MacroLibrary;
import ca.phon.app.opgraph.macro.StockMacroHandler;
import ca.phon.app.opgraph.nodes.AnalysisNodeInstantiator;
import ca.phon.app.opgraph.nodes.PhonScriptNode;
import ca.phon.app.opgraph.nodes.ReportNodeInstantiator;
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
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.app.components.canvas.NodeStyle;
import ca.phon.opgraph.library.NodeLibrary;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.opgraph.nodes.general.MacroNodeData;
import ca.phon.opgraph.nodes.general.MacroNodeInstantiator;
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

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PhonNodeLibrary.class.getName());

	private final NodeLibrary nodeLibrary;

	public PhonNodeLibrary(NodeLibrary nodeLibrary) {
		super();
		this.nodeLibrary = nodeLibrary;
		
		this.nodeLibrary.addURIHandler(new MacroURIHandler());
		this.nodeLibrary.addURIHandler(new ClasspathMacroURIHandler());
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

		StockMacroHandler stockHandler = new StockMacroHandler();
		for(String macroRef:stockHandler.getResourcePaths()) {
			addMacroNodeToLibrary(macroRef, "Macro");
		}
		library.getUserGraphs().forEach( (url) -> {
			try {
				addMacroNodeToLibrary(url, url.toURI(), "Macro (User Library)");
			} catch (URISyntaxException e) {
				LogUtil.warning(e);
			}
		} );
	}

	public void addAnalysisNodes() {
		final AnalysisLibrary library = new AnalysisLibrary();
		
		StockAnalysisHandler stockHandler = new StockAnalysisHandler();
		for(String macroRef:stockHandler.getResourcePaths()) {
			addAnalysisNodeToLibrary(macroRef, "Analysis");
		}
		library.getUserGraphs().forEach( (url) -> {
			try {
				addAnalysisNodeToLibrary(url, url.toURI(), "Analysis (User Library)");
			} catch (URISyntaxException e) {
				LogUtil.warning(e);
			}
		} );
	}

	public void addReportNodes() {
		final ReportLibrary library = new ReportLibrary();
		library.getStockGraphs().forEach( (url) -> addReportNodeToLibrary(url, "Reports") );
		library.getUserGraphs().forEach( (url) -> addReportNodeToLibrary(url, "Reports (User Library)") );
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
				LOGGER.warn( e.getLocalizedMessage(), e);
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
				LOGGER.warn( e.getLocalizedMessage(), e);
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
		scriptStyle.NodeBorderColor = new Color(100, 155, 100);
		scriptStyle.NodeBackgroundColor = new Color(250, 250, 100, 200);
		scriptStyle.NodeNameTopColor = new Color(220, 220, 100, 255);
		scriptStyle.NodeNameBottomColor = new Color(175, 175, 50, 255);
		
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
			LOGGER.warn( e.getLocalizedMessage(), e);
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
			LOGGER.error( e.getLocalizedMessage(), e);
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
			LOGGER.warn( e.getLocalizedMessage(), e);
		}
	}

	private void addMacroNodeToLibrary(String cpLocation, String category) {
		URL cpURL = ClassLoader.getSystemResource(cpLocation);
		if(cpURL != null) {
			try {
				URI uri = new URI("classpath", cpLocation, null);
				addMacroNodeToLibrary(cpURL, uri, category);
			} catch (URISyntaxException e) {
				LogUtil.warning(e);
			}
			
		}
	}
	
	private void addMacroNodeToLibrary(URL url, URI uri, String category) {
		try {
			String filename = URLDecoder.decode(url.getPath(), "UTF-8");
			String name = FilenameUtils.getBaseName(filename);
			
			final MacroNodeData nodeData = new MacroNodeData(url, uri, name, "", category, new MacroNodeInstantiator(), false);
			getNodeLibrary().put(nodeData);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}
	
	private void addAnalysisNodeToLibrary(String cpLocation, String category) {
		URL cpURL = ClassLoader.getSystemResource(cpLocation);
		if(cpURL != null) {
			try {
				URI uri = new URI("classpath", cpLocation, null);
				addAnalysisNodeToLibrary(cpURL, uri, category);
			} catch (URISyntaxException e) {
				LogUtil.warning(e);
			}
		}
	}

	private void addAnalysisNodeToLibrary(URL url, URI uri, String category) {
		try {
			String filename = URLDecoder.decode(url.getFile(), "UTF-8");
			String name = FilenameUtils.getBaseName(filename);

			final MacroNodeData nodeData = new MacroNodeData(url, uri, name, "", category, new AnalysisNodeInstantiator(), false);
			getNodeLibrary().put(nodeData);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

	private void addReportNodeToLibrary(URL url, String category) {
		try {
			String filename = URLDecoder.decode(url.getFile(), "UTF-8");
			String name = FilenameUtils.getBaseName(filename);

			final URI uri = new URI("class", MacroNode.class.getName(), name);

			final MacroNodeData nodeData = new MacroNodeData(url, uri, name, "", category, new ReportNodeInstantiator(), false);
			getNodeLibrary().put(nodeData);
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}
	
}
