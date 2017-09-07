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
package ca.phon.app.opgraph.wizard.edits;

import javax.swing.undo.*;

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
