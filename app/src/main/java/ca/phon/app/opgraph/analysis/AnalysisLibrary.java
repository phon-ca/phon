/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.opgraph.analysis;

import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.app.opgraph.editor.OpGraphLibrary;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.nodes.query.QueryNode;
import ca.phon.app.opgraph.nodes.query.QueryNodeData;
import ca.phon.app.opgraph.nodes.query.QueryNodeInstantiator;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpLink;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.dag.CycleDetectedException;
import ca.phon.opgraph.dag.VertexNotFoundException;
import ca.phon.opgraph.exceptions.ItemMissingException;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.opgraph.nodes.reflect.ObjectNode;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.resources.ResourceLoader;
import ca.phon.worker.PhonWorker;

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
				act.setShowWizard(selectedSessions.size() == 0);
				userMenuBuilder.addItem(menuPath, new JMenuItem(act));
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		if(userMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "user_library");
			final JMenuItem userLibItem = builder.addItem(".@user_library", "-- User Library --");
			userLibItem.setFont(userLibItem.getFont().deriveFont(Font.BOLD));
			final File userLibFolder = new File(UserAnalysisHandler.DEFAULT_USER_ANALYSIS_FOLDER);
			userLibItem.setToolTipText("Show folder " + userLibFolder.getAbsolutePath());
			userLibItem.addActionListener( (e) -> {
				try {
					OpenFileLauncher.openURL( userLibFolder.toURI().toURL() );
				} catch (MalformedURLException e1) {
					LOGGER.error( e1.getLocalizedMessage(), e1);
				}
			});
			builder.appendSubItems(".@-- User Library --", userMenu.getPopupMenu());
		}

		final JMenu projectMenu = new JMenu("Project Library");
		final MenuBuilder projectMenuBuilder = new MenuBuilder(projectMenu.getPopupMenu());
		final Iterator<URL> projectGraphIterator = getProjectGraphs(project).iterator();
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
				act.setShowWizard(selectedSessions.size() == 0);
				projectMenuBuilder.addItem(menuPath, act);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		if(projectMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "project_library");
			final JMenuItem projectSepItem = builder.addItem(".@project_library", "-- Project Library --");
			projectSepItem.setFont(projectSepItem.getFont().deriveFont(Font.BOLD));
			final File projectFolder = getProjectAnalysisFolder(project);
			projectSepItem.addActionListener( (e) -> {
				try {
					OpenFileLauncher.openURL( projectFolder.toURI().toURL() );
				} catch (MalformedURLException e1) {
					LOGGER.error( e1.getLocalizedMessage(), e1);
				}
			});
			projectSepItem.setToolTipText("Show folder " + projectFolder.getAbsolutePath());
			builder.appendSubItems(".@-- Project Library --", projectMenu.getPopupMenu());
		}

		builder.addSeparator(".", "browse");
		final PhonUIAction onBrowseAct = new PhonUIAction(AnalysisLibrary.class, "onBrowse", project);
		onBrowseAct.putValue(PhonUIAction.NAME, "Browse...");
		onBrowseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for analysis document.");
		builder.addItem(".@browse", onBrowseAct);
		builder.addItem(".", new OpenSimpleAnalysisComposerAction(project));

//		final PhonUIAction showComposerAct = new PhonUIAction(AnalysisLibrary.class, "showComposer");
//		showComposerAct.putValue(PhonUIAction.NAME, "Composer (advanced)...");
//		showComposerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Create a new analysis using Composer...");
//		builder.addItem(".@Composer (simple)...", showComposerAct);
	}

	public static void onBrowse(PhonActionEvent pae) {
		final Project project = (Project)pae.getData();
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

	public static MacroNode analysisFromQuery(QueryScript queryScript)
		throws IOException, ItemMissingException, VertexNotFoundException, CycleDetectedException, URISyntaxException, InstantiationException {
		final QueryName queryName = queryScript.getExtension(QueryName.class);
		String reportTitle = "Report";
		if(queryName != null) {
			reportTitle = queryName.getName();
		}
		
		final AnalysisEditorModelInstantiator instantiator = new AnalysisEditorModelInstantiator();
		final OpGraph graph = new OpGraph();
		final AnalysisOpGraphEditorModel model = instantiator.createModel(graph);
		final MacroNode retVal = new MacroNode(graph);
		retVal.setName(reportTitle);
		
		final WizardExtension wizardExt = graph.getExtension(WizardExtension.class);

		final URI queryNodeClassURI = new URI("class", QueryNode.class.getName(), queryName.getName());
		final QueryNodeInstantiator queryNodeInstantiator = new QueryNodeInstantiator();
		final QueryNodeData nodeData = new QueryNodeData(queryScript, queryNodeClassURI,
				reportTitle, "", "Query", queryNodeInstantiator);
		
		final QueryNode queryNode = queryNodeInstantiator.newInstance(nodeData);
		graph.add(queryNode);
		
		wizardExt.addNode(queryNode);
		wizardExt.setNodeForced(queryNode, true);

		// add parameters template
		final OpGraph parametersTemplate = OpgraphIO.read(
				AnalysisLibrary.class.getClassLoader().getResourceAsStream(PARAMETERS_TEMPLATE));
		for(OpNode node:parametersTemplate) {
			graph.add(node);
		}
		for(OpNode node:parametersTemplate) {
			for(OpLink link:parametersTemplate.getOutgoingEdges(node)) {
				graph.add(link);
			}
		}
		final OpNode reportTitleNode = parametersTemplate.getVertices().stream()
				.filter( (n) -> n.getName().equals("Get Report Title") ).findFirst().orElse(null);
		if(reportTitleNode == null) {
			throw new IllegalArgumentException("Report title node not found");
		}
		
		final String reportDocument = getQueryReport(queryScript);
		final OpGraph reportGraph = OpgraphIO.read(
				AnalysisLibrary.class.getClassLoader().getResourceAsStream(reportDocument));
		final OpNode reportNode = reportGraph.getVertices().stream().filter( (n) -> n instanceof MacroNode ).findAny().orElse(null);
		graph.add(reportNode);

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
		
		final OpLink projectLink = new OpLink(projectNode, "obj", queryNode, "project");
		final OpLink sessionsLink = new OpLink(sessionsNode, "obj", queryNode, "sessions");
		graph.add(projectLink);
		graph.add(sessionsLink);

		final OpLink reportTitleLink = new OpLink(reportTitleNode, "reportTitle", reportNode, "reportTitle");
		graph.add(reportTitleLink);
		final OpLink projectLink2 = new OpLink(queryNode, "project", reportNode, "project");
		graph.add(projectLink2);
		final OpLink paramLink = new OpLink(queryNode, "parameters", reportNode, "parameters");
		graph.add(paramLink);
		final OpLink resultsLink = new OpLink(queryNode, "results", reportNode, "results");
		graph.add(resultsLink);
		final OpLink scriptLink = new OpLink(queryNode, "script", reportNode, "script");
		graph.add(scriptLink);
		final OpLink sessionsLink2 = new OpLink(sessionsNode, "obj", reportNode, "selected sessions");
		graph.add(sessionsLink2);
		final OpLink participantsLink = new OpLink(participantsNode, "obj", reportNode, "selected participants");
		graph.add(participantsLink);

		retVal.publish("project", projectNode, projectNode.getInputFieldWithKey("obj"));
		retVal.publish("selectedSessions", sessionsNode, sessionsNode.getInputFieldWithKey("obj"));
		retVal.publish("selectedParticipants", participantsNode, participantsNode.getInputFieldWithKey("obj"));

		return retVal;
	}

	public static void showComposer() {
		final AnalysisEditorModelInstantiator instantiator = new AnalysisEditorModelInstantiator();
		final AnalysisOpGraphEditorModel editorModel = instantiator.createModel(new OpGraph());
		final OpgraphEditor editor =  new OpgraphEditor(editorModel);

		final Project project = CommonModuleFrame.getCurrentFrame().getExtension(Project.class);
		if(project != null) {
			editor.putExtension(Project.class, project);
			((AnalysisOpGraphEditorModel)editor.getModel()).getParticipantSelector().getSessionSelector().setProject(project);
		}

		editor.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		editor.pack();
		editor.setSize(1024, 768);
		editor.setVisible(true);
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
