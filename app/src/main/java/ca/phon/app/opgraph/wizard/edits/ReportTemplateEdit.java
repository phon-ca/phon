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
