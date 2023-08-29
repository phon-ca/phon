/*
 * Copyright (C) 2012-2023 Gregory Hedlund
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
package ca.phon.app.csv;

import java.awt.*;

import javax.swing.JMenuBar;

import ca.phon.app.project.ProjectWindow;
import ca.phon.plugin.*;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.MenuBuilder;

/**
 * Add CSV import/export menu items to the 'Tools' menu
 */
@PhonPlugin(name="phon-csv-plugin",
	version="1",
	minPhonVersion="4.0.0")
public class CSVMenuHandler
	implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return (args) -> this;
	}

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menuBar) {
		if(!(owner instanceof ProjectWindow pw)) return;

		final MenuBuilder builder = new MenuBuilder(menuBar);
		builder.addSeparator("./Tools", "phon-csv-plugin");
		
		final PhonUIAction<ProjectWindow> importAct = PhonUIAction.eventConsumer(CSVMenuHandler::showImportWizard, pw);
		importAct.putValue(PhonUIAction.NAME, "Import from CSV...");
		importAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Import CSV files as new Sessions in the current project.");
		builder.addItem("./Tools@phon-csv-plugin", importAct);

		final PhonUIAction<ProjectWindow> exportAct = PhonUIAction.eventConsumer(CSVMenuHandler::showExportWizard, pw);
		exportAct.putValue(PhonUIAction.NAME, "Export to CSV...");
		exportAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export Sessions from the current project as CSV files.");
		builder.addItem("./Tools@phon-csv-plugin", exportAct);
	}
	
	public static void showImportWizard(PhonActionEvent<ProjectWindow> pae) {
		final ProjectWindow pw = pae.getData();
		final Project project = pw.getProject();
		if(project == null) return;
		
		final CSVImportWizard wizard = new CSVImportWizard(project, pw.getSelectedCorpus());
		wizard.pack();
		wizard.setSize(new Dimension(640, wizard.getPreferredSize().height));
		wizard.centerWindow();
		wizard.setVisible(true);
	}

	public static void showExportWizard(PhonActionEvent<ProjectWindow> pae) {
		final ProjectWindow pw = pae.getData();
		final Project project = pw.getProject();
		if(project == null) return;

		final CSVExportWizard wizard = new CSVExportWizard(project);
		wizard.pack();
		wizard.setSize(new Dimension(640, wizard.getPreferredSize().height));
		wizard.centerWindow();
		wizard.setVisible(true);
	}
}
