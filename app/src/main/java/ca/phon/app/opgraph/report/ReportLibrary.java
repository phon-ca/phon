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
package ca.phon.app.opgraph.report;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import ca.phon.app.log.*;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.project.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.*;
import ca.phon.util.resources.*;
import ca.phon.worker.*;

/**
 * <p>Library of query reports. These reports are available
 * from the 'Report' menu button in the query and query history
 * dialogs.</p>
 *
 * <p>Reports are stored in <code>~/Documents/Phon/reports</code>
 * by default. Reports can also be stored in the project
 * <code>__res/reports/</code> folder.</p>
 */
public class ReportLibrary implements OpGraphLibrary {

	private final static String LEGACY_REPORT_DOCUMENT = "report/Legacy Report Design.xml";

	public final static String REPORT_FOLDER_NAME = "report";

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ReportLibrary.class.getName());

	/**
	 * Report loader
	 */
	private ResourceLoader<URL> loader = new ResourceLoader<>();

	public ReportLibrary() {
		super();
		loader.addHandler(new StockReportHandler());
		loader.addHandler(new UserReportHandler());
	}

	public List<URL> getAvailableReports() {
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
		retVal.addHandler(new StockReportHandler());
		return retVal;
	}

	public ResourceLoader<URL> getUserGraphs() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserReportHandler());
		return retVal;
	}

	public ResourceLoader<URL> getProjectGraphs(Project project) {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserReportHandler(new File(project.getResourceLocation(), REPORT_FOLDER_NAME)));
		return retVal;
	}

	public File getProjectReportFolder(Project project) {
		return new File(project.getResourceLocation(), REPORT_FOLDER_NAME);
	}

	public void setupMenu(Project project, String queryId, MenuElement menu) {
		final MenuBuilder builder = new MenuBuilder(menu);


		for(URL reportURL:getStockGraphs()) {
			final ReportAction act = new ReportAction(project, queryId, reportURL);

			try {
				final String fullPath = URLDecoder.decode(reportURL.getPath(), "UTF-8");
				final String relativePath =
						fullPath.substring(fullPath.indexOf("/" + REPORT_FOLDER_NAME + "/")+REPORT_FOLDER_NAME.length()+2);

				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath += "/" + relativePath.substring(0, lastFolderIndex);
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
						(new File(UserReportHandler.DEFAULT_USER_REPORT_FOLDER)).toURI().relativize(reportURL.toURI());

				final String relativePath = URLDecoder.decode(relativeURI.getPath(), "UTF-8");
				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath += "/" + relativePath.substring(0, lastFolderIndex);
				}

				final ReportAction act = new ReportAction(project, queryId, reportURL);
				userMenuBuilder.addItem(menuPath, act);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		if(userMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "user_library");
			final JMenuItem userLibItem = builder.addItem(".@user_library", "-- User Library --");
			userLibItem.setFont(userLibItem.getFont().deriveFont(Font.BOLD));
			final File userLibFolder = new File(UserReportHandler.DEFAULT_USER_REPORT_FOLDER);
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
		while(projectGraphIterator.hasNext()) {
			try {
				final URL reportURL = projectGraphIterator.next();
				final URI relativeURI =
						getProjectReportFolder(project).toURI().relativize(reportURL.toURI());

				final String relativePath = URLDecoder.decode(relativeURI.getPath(), "UTF-8");
				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath += "/" + relativePath.substring(0, lastFolderIndex);
				}

				final ReportAction act = new ReportAction(project, queryId, reportURL);
				projectMenuBuilder.addItem(menuPath, act);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		if(projectMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "project_library");
			final JMenuItem projectSepItem = builder.addItem(".@project_library", "-- Project Library --");
			projectSepItem.setFont(projectSepItem.getFont().deriveFont(Font.BOLD));
			final File projectFolder = getProjectReportFolder(project);
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
			projectMenuBuilder.appendSubItems(".@-- Project Library --", projectMenu.getPopupMenu());
		}

		builder.addSeparator(".", "browse");
		final PhonUIAction onBrowseAct = new PhonUIAction(ReportLibrary.class, "onBrowse", new Tuple<String, Project>(queryId, project));
		onBrowseAct.putValue(PhonUIAction.NAME, "Browse...");
		onBrowseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for query report document.");
		builder.addItem(".@browse", onBrowseAct);

		final OpenSimpleReportComposerAction showSimpleComposerAct = new OpenSimpleReportComposerAction(project, queryId);
		showSimpleComposerAct.putValue(Action.NAME, "Composer (simple)...");
		builder.addItem(".", showSimpleComposerAct);
	}

	public static void onBrowse(PhonActionEvent pae) {
		@SuppressWarnings("unchecked")
		final Tuple<String, Project> data = (Tuple<String, Project>)pae.getData();
		final String queryId = data.getObj1();
		final Project project = (Project)data.getObj2();
		final FileFilter filter = new FileFilter("Query Report Documents", "xml;opgraph");
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setFileFilter(filter);
		props.setCanChooseDirectories(false);
		props.setCanChooseFiles(true);
		props.setAllowMultipleSelection(false);
		props.setRunAsync(false);

		final List<String> selectedFiles =
				NativeDialogs.showOpenDialog(props);
		if(selectedFiles != null && selectedFiles.size() == 1) {
			final File selectedFile = new File(selectedFiles.get(0));

			// attempt to run file as an analysis
			try {
				final OpGraph graph = OpgraphIO.read(selectedFile);

				final WizardExtension ext = graph.getExtension(WizardExtension.class);
				if(ext == null || !(ext instanceof ReportWizardExtension)) {
					throw new IOException("Selected document is not a query report");
				}

				final ReportRunner runner = new ReportRunner(graph, project, queryId);
				PhonWorker.getInstance().invokeLater(runner);
			} catch (IOException e) {
				Toolkit.getDefaultToolkit().beep();
				LOGGER.error( e.getLocalizedMessage(), e);

				final MessageDialogProperties mprops = new MessageDialogProperties();
				mprops.setParentWindow(CommonModuleFrame.getCurrentFrame());
				mprops.setTitle("Report : Error");
				mprops.setHeader("Unable to run selected query report");
				mprops.setMessage(e.getLocalizedMessage());
				mprops.setOptions(MessageDialogProperties.okOptions);
				NativeDialogs.showMessageDialog(mprops);
			}
		}
	}

	@Override
	public String getFolderName() {
		return REPORT_FOLDER_NAME;
	}

	@Override
	public String getUserFolderPath() {
		return UserReportHandler.DEFAULT_USER_REPORT_FOLDER;
	}

	@Override
	public String getProjectFolderPath(Project project) {
		return getProjectReportFolder(project).getAbsolutePath();
	}

}
