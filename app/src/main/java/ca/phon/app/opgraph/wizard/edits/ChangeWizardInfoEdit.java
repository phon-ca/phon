/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.WizardInfoMessageFormat;

public class ChangeWizardInfoEdit extends WizardExtensionUndoableEdit {
	
	private static final long serialVersionUID = -5317622356377461585L;

	private OpNode node;

	private String oldTitle;
	
	private String oldMessage;
	
	private WizardInfoMessageFormat oldFormat;
	
	private String title;
	
	private String message;
	
	private WizardInfoMessageFormat format;
	
	public ChangeWizardInfoEdit(NodeWizardPanel wizardPanel, String title, String message, WizardInfoMessageFormat format,
			String oldTitle, String oldMessage, WizardInfoMessageFormat oldFormat) {
		this(wizardPanel, null, title, message, format, oldTitle, oldMessage, oldFormat);
	}

	public ChangeWizardInfoEdit(NodeWizardPanel wizardPanel, OpNode node, String title, String message, WizardInfoMessageFormat format,
			String oldTitle, String oldMessage, WizardInfoMessageFormat oldFormat) {
		super(wizardPanel);
		
		this.node = node;
		this.title = title;
		this.format = format;
		this.message = message;
		this.oldTitle = oldTitle;
		this.oldMessage = oldMessage;
		this.oldFormat = oldFormat;
	}
	
	public void doIt() {
		if(node == null) {
			getWizardExtension().setWizardTitle(this.title);
			getWizardExtension().setWizardMessage(this.message, this.format);
		} else {
			getWizardExtension().setNodeTitle(node, this.title);
			getWizardExtension().setNodeMessage(node, this.message, this.format);
		}
		getWizardPanel().updateTable();
	}
	
	@Override
	public void undo() {
		if(node == null) {
			getWizardExtension().setWizardTitle(this.oldTitle);
			getWizardExtension().setWizardMessage(this.oldMessage, this.oldFormat);
		} else {
			getWizardExtension().setNodeTitle(node, this.oldTitle);
			getWizardExtension().setNodeMessage(node, this.oldMessage, this.oldFormat);
		}
		getWizardPanel().updateTable();
	}
	
	@Override
	public void redo() {
		doIt();
	}
	
}
