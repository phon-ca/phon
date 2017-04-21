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
package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.OSInfo;

/**
 * Delete corpus/corpora which are currently selected
 * in the project window.
 * 
 */
public class DeleteCorpusAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(DeleteCorpusAction.class.getName());
	
	private static final long serialVersionUID = -6953043638785028830L;

	public DeleteCorpusAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(Action.NAME, "Delete corpus");
		putValue(Action.SHORT_DESCRIPTION, "Delete selected corpora");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final List<String> corpora = getWindow().getSelectedCorpora();
		if(corpora.size() == 0) return;
		
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setRunAsync(false);
		if(corpora.size() > 1) {
			props.setHeader("Delete selected corpora?");
		} else {
			props.setHeader("Delete corpus: " + corpora.get(0));
		}
		props.setMessage("Move" +
				(corpora.size() > 1 ? " corpora" : " this corpus") + " to " +
				(OSInfo.isWindows() ? "Recycle Bin" : "Trash") + "?");
		props.setOptions(MessageDialogProperties.okCancelOptions);
		int retVal = NativeDialogs.showMessageDialog(props);
		
		final Project project = getWindow().getProject();
		if(retVal == 0) {
			for(String corpus:corpora) {
				try {
					project.removeCorpus(corpus);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					Toolkit.getDefaultToolkit().beep();
					showMessage("Delete Corpus", e.getLocalizedMessage());
				}
			}
			getWindow().getCorpusList().clearSelection();
		}
	}

}
