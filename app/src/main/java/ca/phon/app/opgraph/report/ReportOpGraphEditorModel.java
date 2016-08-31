package ca.phon.app.opgraph.report;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.analysis.AnalysisOpGraphEditorModel;
import ca.phon.app.opgraph.analysis.AnalysisWizardExtension;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.query.QueryHistoryTableModel;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.util.PrefHelper;
import ca.phon.workspace.Workspace;

public class ReportOpGraphEditorModel extends AnalysisOpGraphEditorModel {

	private JPanel debugSettings;
	
	private JComboBox<Project> projectList;
	
	private JTable queryTable;
	
	public ReportOpGraphEditorModel() {
		this(new OpGraph());
	}
	
	public ReportOpGraphEditorModel(OpGraph graph) {
		super(graph);
		
		WizardExtension wizardExt = graph.getExtension(WizardExtension.class);
		if(wizardExt == null || (wizardExt instanceof AnalysisWizardExtension && wizardExt.size() == 0)) {
			wizardExt = new ReportWizardExtension(graph);
			graph.putExtension(WizardExtension.class, wizardExt);
		}
	}

	@Override
	public Rectangle getInitialViewBounds(String viewName) {
		Rectangle retVal = super.getInitialViewBounds(viewName);
		if(viewName.equals("Report Wizard")) {
			retVal = new Rectangle(0, 0, 200, 200);
		}
		return retVal;
	}

	@Override
	protected JComponent getDebugSettings() {
		if(debugSettings == null) {
			debugSettings = new JPanel(new BorderLayout());
			
			final Workspace workspace = Workspace.userWorkspace();
			projectList = new JComboBox<Project>(workspace.getProjects().toArray(new Project[0]));
			projectList.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Project"), 
					projectList.getBorder()));
			
			projectList.addItemListener( (ItemEvent e) -> {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					final QueryHistoryTableModel model = new QueryHistoryTableModel((Project)projectList.getSelectedItem());
					queryTable.setModel(model);
					model.update();
				}
			});
			
			queryTable = new JTable();
			final JScrollPane queryScroller = new JScrollPane(queryTable);
			queryScroller.setBorder(BorderFactory.createTitledBorder("Query History"));
			
			debugSettings.add(projectList, BorderLayout.NORTH);
			debugSettings.add(queryScroller, BorderLayout.CENTER);
		}
		return debugSettings;
	}
	
	@Override
	public void setupContext(OpContext context) {
		if(projectList.getSelectedItem() != null) {
			context.put("_project", projectList.getSelectedItem());
			final QueryHistoryTableModel model = (QueryHistoryTableModel)queryTable.getModel();
			final int selectedIdx = queryTable.convertRowIndexToModel(queryTable.getSelectedRow());
			if(selectedIdx >= 0) {
				final Query selectedQuery = model.getQueryForRow(selectedIdx);
				context.put("_queryId", selectedQuery.getUUID().toString());
			}
		}
	}

	@Override
	public String getDefaultFolder() {
		return PrefHelper.getUserDataFolder() + File.separator + "reports";
	}
	
	@Override
	public String getTitle() {
		return "Report Editor";
	}
}
