/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.analysis;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import ca.hedlund.tst.*;
import ca.phon.app.log.*;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.nodes.query.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.app.query.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.util.*;
import ca.phon.opgraph.dag.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.opgraph.nodes.reflect.*;
import ca.phon.project.*;
import ca.phon.query.script.*;
import ca.phon.session.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.*;
import ca.phon.util.resources.*;
import ca.phon.worker.*;

/**
 * <p>Library of analysis. These analysis are available
 * from the 'Analysis' menu button in the query and query history
 * dialogs.</p>
 *
 * <p>Reports are stored in <code>~/Documents/Phon/reports</code>
 * by default. Reports can also be stored in the project
 * <code>__res/reports/</code> folder.</p>
 */
public class AnalysisLibrary implements OpGraphLibrary {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(AnalysisLibrary.class.getName());
	
	private final static String QUERY_REPORT_MAP = "analysis/QueryReportMap.txt";
	
	private final static String ANALYSIS_FROM_QUERY_TEMPLATE = "macro/AnalysisFromQueryTemplate.xml";
	
	private final static String PARAMETERS_TEMPLATE = "macro/Parameters Template.xml";

	public final static String ANALYSIS_FOLDER = "analysis";

	/**
	 * Report loader
	 */
	private ResourceLoader<URL> loader = new ResourceLoader<>();

	public AnalysisLibrary() {
		super();
		loader.addHandler(new StockAnalysisHandler());
		loader.addHandler(new UserAnalysisHandler());
	}

	public List<URL> getAvailableAnalysis() {
		List<URL> retVal = new ArrayList<URL>();

		final Iterator<URL> reportIterator = loader.iterator();
		while(reportIterator.hasNext()) {
			final URL url = reportIterator.next();
			if(url == null) continue;
			retVal.add(url);
		}

		return retVal;
	}

	public ResourceLoader<URL> getStockGraphs() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new StockAnalysisHandler());
		return retVal;
	}

	public ResourceLoader<URL> getUserGraphs() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserAnalysisHandler());
		return retVal;
	}

	public ResourceLoader<URL> getProjectGraphs(Project project) {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserAnalysisHandler(getProjectAnalysisFolder(project)));
		return retVal;
	}

	public File getProjectAnalysisFolder(Project project) {
		return new File(project.getResourceLocation(), ANALYSIS_FOLDER);
	}

	public void setupMenu(Project project, List<SessionPath> selectedSessions, MenuElement menu) {
		final MenuBuilder builder = new MenuBuilder(menu);

		for(URL reportURL:getStockGraphs()) {
			final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);

			try {
				final String fullPath = URLDecoder.decode(reportURL.getPath(), "UTF-8");
				final String relativePath =
						fullPath.substring(fullPath.indexOf(ANALYSIS_FOLDER + "/")+ANALYSIS_FOLDER.length()+1);

				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath = relativePath.substring(0, lastFolderIndex);
				}

				builder.addItem(menuPath, act);
			} catch (UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}

		final JMenu userMenu = new JMenu("User Library");
		final MenuBuilder userMenuBuilder = new MenuBuilder(userMenu.getPopupMenu());
		final Iterator<URL> userGraphIterator = getUserGraphs().iterator();
		final TernaryTree<Tuple<String, AnalysisAction>> userActionMap = new TernaryTree<>();
		while(userGraphIterator.hasNext()) {
			try {
				final URL reportURL = userGraphIterator.next();
				final URI relativeURI =
						(new File(UserAnalysisHandler.DEFAULT_USER_ANALYSIS_FOLDER)).toURI().relativize(reportURL.toURI());

				final String relativePath = URLDecoder.decode(relativeURI.getPath(), "UTF-8");
				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath += "/" + relativePath.substring(0, lastFolderIndex);
				}

				final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
				String path = menuPath + "/" + act.getValue(AnalysisAction.NAME);
				
				userActionMap.put(path.toLowerCase(), new Tuple<>(menuPath, act));
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		for(String path:userActionMap.keySet()) {
			var tuple = userActionMap.get(path);
			userMenuBuilder.addItem(tuple.getObj1(), tuple.getObj2());
		}
		if(userMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "user_library");
			final JMenuItem userLibItem = builder.addItem(".@user_library", "-- User Library --");
			userLibItem.setFont(userLibItem.getFont().deriveFont(Font.BOLD));
			final File userLibFolder = new File(UserAnalysisHandler.DEFAULT_USER_ANALYSIS_FOLDER);
			userLibItem.setToolTipText("Show folder " + userLibFolder.getAbsolutePath());
			userLibItem.addActionListener( (e) -> {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(userLibFolder);
					} catch (IOException e1) {
						LogUtil.warning(e1);
						Toolkit.getDefaultToolkit().beep();
					}
				}
			});
			builder.appendSubItems(".@-- User Library --", userMenu.getPopupMenu());
		}

		final JMenu projectMenu = new JMenu("Project Library");
		final MenuBuilder projectMenuBuilder = new MenuBuilder(projectMenu.getPopupMenu());
		final Iterator<URL> projectGraphIterator = getProjectGraphs(project).iterator();
		final TernaryTree<Tuple<String, AnalysisAction>> analysisActionMap = new TernaryTree<>();
		while(projectGraphIterator.hasNext()) {
			try {
				final URL reportURL = projectGraphIterator.next();
				final URI relativeURI =
						getProjectAnalysisFolder(project).toURI().relativize(reportURL.toURI());

				final String relativePath = URLDecoder.decode(relativeURI.getPath(), "UTF-8");
				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath += "/" + relativePath.substring(0, lastFolderIndex);
				}

				final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
				String path = menuPath + "/" + act.getValue(AnalysisAction.NAME);
				analysisActionMap.put(path.toLowerCase(), new Tuple<>(menuPath, act));
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		for(String path:analysisActionMap.keySet()) {
			var tuple = analysisActionMap.get(path);
			projectMenuBuilder.addItem(tuple.getObj1(), tuple.getObj2());
		}
		if(projectMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "project_library");
			final JMenuItem projectSepItem = builder.addItem(".@project_library", "-- Project Library --");
			projectSepItem.setFont(projectSepItem.getFont().deriveFont(Font.BOLD));
			final File projectFolder = getProjectAnalysisFolder(project);
			projectSepItem.addActionListener( (e) -> {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(projectFolder);
					} catch (IOException e1) {
						LogUtil.warning(e1);
						Toolkit.getDefaultToolkit().beep();
					}
				}
			});
			projectSepItem.setToolTipText("Show folder " + projectFolder.getAbsolutePath());
			builder.appendSubItems(".@-- Project Library --", projectMenu.getPopupMenu());
		}

		builder.addSeparator(".", "browse");
		final PhonUIAction<Project> onBrowseAct = PhonUIAction.eventConsumer(AnalysisLibrary::onBrowse, project);
		onBrowseAct.putValue(PhonUIAction.NAME, "Browse...");
		onBrowseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for analysis document.");
		builder.addItem(".@browse", onBrowseAct);
		builder.addItem(".", new OpenSimpleAnalysisComposerAction(project));
	}

	public static void onBrowse(PhonActionEvent<Project> pae) {
		final Project project = pae.getData();
		final FileFilter filter = new FileFilter("Analysis Documents", "xml;opgraph");
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setFileFilter(filter);
		props.setCanChooseDirectories(false);
		props.setCanChooseFiles(true);
		props.setAllowMultipleSelection(false);
		props.setRunAsync(true);
		props.setListener( (e) -> {
			@SuppressWarnings("unchecked")
			final String selectedFile = (e.getDialogData() != null ? e.getDialogData().toString() : null);
			if(selectedFile != null) {
				// attempt to run file as an analysis
				try {
					final OpGraph graph = OpgraphIO.read(new File(selectedFile));
					
					final WizardExtension ext = graph.getExtension(WizardExtension.class);
					if(ext == null || !(ext instanceof AnalysisWizardExtension)) {
						throw new IOException("Selected document is not an anlaysis");
					}
					
					final AnalysisRunner runner = new AnalysisRunner(graph, project);
					PhonWorker.getInstance().invokeLater(runner);
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					LOGGER.error( ex.getLocalizedMessage(), ex);
					
					final MessageDialogProperties mprops = new MessageDialogProperties();
					mprops.setParentWindow(CommonModuleFrame.getCurrentFrame());
					mprops.setTitle("Analysis : Error");
					mprops.setHeader("Unable to run selected analysis");
					mprops.setMessage(ex.getLocalizedMessage());
					mprops.setOptions(MessageDialogProperties.okOptions);
					mprops.setRunAsync(true);
					NativeDialogs.showMessageDialog(mprops);
				}
			}
		});
		NativeDialogs.showOpenDialog(props);
	}
	
	private static Map<String, String> loadReportMap() {
		final Map<String, String> retVal = new HashMap<>();
		
		try(BufferedReader in = new BufferedReader(
				new InputStreamReader(
						AnalysisLibrary.class.getClassLoader().getResourceAsStream(QUERY_REPORT_MAP)))) {
			String line = null;
			while((line = in.readLine()) != null) {
				int i = line.indexOf('=');
				String key = line.substring(0, i).trim();
				String value = line.substring(i+1).trim();
				
				retVal.put(key, value);
			}
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
		return retVal;
	}

	public static String getQueryReport(QueryScript queryScript) {
		final Map<String, String> reportMap = loadReportMap();
		final QueryName queryName = queryScript.getExtension(QueryName.class);
		
		String retVal = reportMap.get("default");
		if(queryName != null) {
			if(reportMap.containsKey(queryName.getName()))
				retVal = reportMap.get(queryName.getName());
		}
		
		return retVal;
	}

	public static MacroNode analysisFromQuery(QueryScript queryScript, OpGraph reportGraph)
		throws IOException, ItemMissingException, VertexNotFoundException, CycleDetectedException, URISyntaxException, InstantiationException, InvalidEdgeException {
		final QueryName queryName = queryScript.getExtension(QueryName.class);
		String reportTitle = "Report";
		if(queryName != null) {
			reportTitle = queryName.getName();
		}
		final URI queryNodeClassURI = new URI("class", QueryNode.class.getName(), queryName.getName());
		final QueryNodeInstantiator queryNodeInstantiator = new QueryNodeInstantiator();
		final QueryNodeData nodeData = new QueryNodeData(queryScript, queryNodeClassURI,
				reportTitle, "", "Query", queryNodeInstantiator);

		final OpGraph graph = OpgraphIO.read(
				AnalysisLibrary.class.getClassLoader().getResourceAsStream(ANALYSIS_FROM_QUERY_TEMPLATE));
		GraphUtils.changeNodeIds(graph);
		
		final MacroNode retVal = new MacroNode(graph);
		retVal.setName(reportTitle);
		
		final QueryNode queryNode = queryNodeInstantiator.newInstance(nodeData);
		graph.add(queryNode);
		
		final WizardExtension wizardExt = graph.getExtension(WizardExtension.class);
		wizardExt.addNode(queryNode);
		wizardExt.setNodeForced(queryNode, true);
		
		// if given an empty graph load default report
		if(reportGraph.getVertices().size() == 0
				&& reportGraph.getExtension(SimpleEditorExtension.class) == null) {
			reportGraph = OpgraphIO.read(QueryAndReportWizard.class.getResourceAsStream("default_report.xml"));
		}
		
		final QueryReportNode queryReportNode = new QueryReportNode(reportGraph);
		graph.add(queryReportNode);
		
		wizardExt.addNode(queryReportNode);
		
		final OpNode reportTitleNode = graph.getVertices().stream()
				.filter( (n) -> n.getName().equals("Get Report Title") ).findFirst().orElse(null);
		if(reportTitleNode == null) {
			throw new IllegalArgumentException("Report title node not found");
		}
		// create links
		final OpNode projectNode = graph.getVertices().stream()
			.filter( (n) -> n.getName().equals("Project")  && n instanceof ObjectNode )
			.findFirst().orElse(null);
		if(projectNode == null) {
			throw new IllegalArgumentException("Graph has no Project node");
		}
		final OpNode sessionsNode = graph.getVertices().stream()
			.filter( (n) -> n.getName().equals("Selected Sessions") && n instanceof ObjectNode )
			.findFirst().orElse(null);
		if(sessionsNode == null) {
			throw new IllegalArgumentException("Graph has no Selected Sessions node");
		}
		final OpNode participantsNode = graph.getVertices().stream()
			.filter( (n) -> n.getName().equals("Selected Participants") && n instanceof ObjectNode )
			.findFirst().orElse(null);
		if(participantsNode == null) {
			throw new IllegalArgumentException("Graph has no Selected Participants node");
		}
		final OpNode reportTreeNode = graph.getVertices().stream()
			.filter( (n) -> n.getName().equals("ReportTree") && n instanceof ObjectNode)
			.findFirst().orElse(null);
		if(reportTreeNode == null) {
			throw new IllegalArgumentException("Graph has no ReportTree node");
		}
		
		// project links
		OpLink projectLink = new OpLink(projectNode, "obj", queryNode, "project");
		graph.add(projectLink);
		
		projectLink = new OpLink(queryNode, "project", queryReportNode, "project");
		graph.add(projectLink);
		
		// session link
		OpLink sessionLink = new OpLink(sessionsNode, "obj", queryNode, "sessions");
		graph.add(sessionLink);
		
		// results link
		OpLink resultsLink = new OpLink(queryNode, "results", queryReportNode, "results");
		graph.add(resultsLink);
		
		// query link
		OpLink queryLink = new OpLink(queryNode, "query", queryReportNode, "query");
		graph.add(queryLink);
		
		// report tree
		OpLink reportTreeLink = new OpLink(queryReportNode, "report", reportTreeNode, "obj");
		graph.add(reportTreeLink);
		
		retVal.publish("project", projectNode, projectNode.getInputFieldWithKey("obj"));
		retVal.publish("selectedSessions", sessionsNode, sessionsNode.getInputFieldWithKey("obj"));
		retVal.publish("selectedParticipants", participantsNode, participantsNode.getInputFieldWithKey("obj"));

		return retVal;
	}

	@Override
	public String getFolderName() {
		return ANALYSIS_FOLDER;
	}

	@Override
	public String getUserFolderPath() {
		return UserAnalysisHandler.DEFAULT_USER_ANALYSIS_FOLDER;
	}

	@Override
	public String getProjectFolderPath(Project project) {
		return getProjectAnalysisFolder(project).getAbsolutePath();
	}

}
