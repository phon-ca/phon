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
package ca.phon.app.opgraph.wizard;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import ca.phon.app.log.MultiBufferPanel;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToFolderAction;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToFolderAction.ExportType;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToWorkbookAction;
import ca.phon.ui.ButtonPopup;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class WizardMultiBufferPanel extends MultiBufferPanel {

	private DropDownButton exportButton;
	
	private NodeWizard wizard;
	
	public WizardMultiBufferPanel(NodeWizard wizard) {
		super();
		
		this.wizard = wizard;
	}
	
	@Override
	protected void setupToolbar(JToolBar toolbar) {
		super.setupToolbar(toolbar);
		
		if(getCurrentBuffer() != null && getCurrentBuffer().getName().equals("Report")) {
			// replace export as excel button with custom button
			final PhonUIAction showExportMenuAct = new PhonUIAction(this, "showExportMenu");
			showExportMenuAct.putValue(PhonUIAction.NAME, "Export tables...");
			
			final JPopupMenu menu = new JPopupMenu();
			Action dropDownAct = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
				}
			};
			dropDownAct.putValue(Action.NAME, "Export tables");
			dropDownAct.putValue(Action.SHORT_DESCRIPTION, "Export tables as excel or CSV");
			dropDownAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
			dropDownAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
			dropDownAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
			dropDownAct.putValue(DropDownButton.BUTTON_POPUP, menu);
			
			exportButton = new DropDownButton(dropDownAct);
			exportButton.setOnlyPopup(true);
			exportButton.getButtonPopup().addPropertyChangeListener(ButtonPopup.POPUP_VISIBLE, (e) -> {
				if(Boolean.parseBoolean(e.getNewValue().toString())) {
					setupExportMenu(menu);
				}
			});
			
			toolbar.removeAll();
			
			toolbar.add(saveBufferButton);
			toolbar.add(exportButton);
			toolbar.add(openFileAfterSavingBox);
			toolbar.addSeparator();
			toolbar.add(new JLabel("Buffer:"));
			toolbar.add(bufferNameBox);
		}
	}
	
	public void setupExportMenu(JPopupMenu menu) {
		menu.removeAll();
		
		final SaveTablesToWorkbookAction saveTablesToWorkbookAct = new SaveTablesToWorkbookAction(wizard);
		saveTablesToWorkbookAct.putValue(Action.NAME, "Export tables as Excel workbook...");
		saveTablesToWorkbookAct.putValue(Action.SHORT_DESCRIPTION, "Export report tables to a single Excel workbook");
		saveTablesToWorkbookAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
				
		final SaveTablesToFolderAction saveTablesCSVAct = new SaveTablesToFolderAction(wizard, ExportType.CSV);
		saveTablesCSVAct.putValue(Action.NAME, "Export tables to folder (CSV)...");
		saveTablesCSVAct.putValue(Action.SHORT_DESCRIPTION, "Export report tables in CSV format to selected folder - one file per table.");
		saveTablesCSVAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));

		final SaveTablesToFolderAction saveTablesExcelAct = new SaveTablesToFolderAction(wizard, ExportType.EXCEL);
		saveTablesExcelAct.putValue(Action.NAME, "Export tables to folder (XLS)...");
		saveTablesExcelAct.putValue(Action.SHORT_DESCRIPTION, "Export report tables in Excel format to selected folder - one file per table.");
		saveTablesExcelAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
		
		menu.add(new JMenuItem(saveTablesToWorkbookAct));
		menu.add(new JMenuItem(saveTablesExcelAct));
		menu.add(new JMenuItem(saveTablesCSVAct));
		
//		JComponent src = (JComponent)pae.getActionEvent().getSource();
//		menu.show(src, 0, src.getHeight());
		
	}
	
}
