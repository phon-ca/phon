package ca.phon.app.opgraph.wizard.edits;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.phon.app.opgraph.wizard.WizardExtension;

public class ReportTemplateEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = 8603335131039825465L;

	private WizardExtension wizardExtension;
	
	private String reportName;
	
	private String reportContent;
	
	private String oldContent;
	
	public ReportTemplateEdit(WizardExtension ext, String reportName, String reportContent) {
		super();
		
		this.wizardExtension = ext;
		this.reportName = reportName;
		this.reportContent = reportContent;
		this.oldContent = ext.getReportTemplate(reportName).getTemplate();
		
		ext.putReportTemplate(reportName, reportContent);
	}
	
	public WizardExtension getWizardExtension() {
		return wizardExtension;
	}

	public void setWizardExtension(WizardExtension wizardExtension) {
		this.wizardExtension = wizardExtension;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getReportContent() {
		return reportContent;
	}

	public void setReportContent(String reportContent) {
		this.reportContent = reportContent;
	}

	@Override
	public void undo() throws CannotUndoException {
		this.wizardExtension.putReportTemplate(reportName, oldContent);
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void redo() throws CannotRedoException {
		this.wizardExtension.putReportTemplate(reportName, reportContent);
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	@Override
	public String getPresentationName() {
		return "Edit Template " + reportName;
	}
	
}
