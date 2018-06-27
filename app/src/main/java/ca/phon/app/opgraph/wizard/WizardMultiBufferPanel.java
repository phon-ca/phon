package ca.phon.app.opgraph.wizard;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import ca.phon.app.log.MultiBufferPanel;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToFolderAction;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToWorkbookAction;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToFolderAction.ExportType;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class WizardMultiBufferPanel extends MultiBufferPanel {

	private JButton exportButton;
	
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
			
			
			showExportMenuAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
			exportButton = new JButton(showExportMenuAct);
			
			toolbar.removeAll();
			
			toolbar.add(saveBufferButton);
			toolbar.add(exportButton);
			toolbar.add(openFileAfterSavingBox);
			toolbar.addSeparator();
			toolbar.add(new JLabel("Buffer:"));
			toolbar.add(bufferNameBox);
		}
	}
	
	public void showExportMenu(PhonActionEvent pae) {
		final JPopupMenu menu = new JPopupMenu();
		
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
		
		JComponent src = (JComponent)pae.getActionEvent().getSource();
		menu.show(src, 0, src.getHeight());
		
	}
	
}
