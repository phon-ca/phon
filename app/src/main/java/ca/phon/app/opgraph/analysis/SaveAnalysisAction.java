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
package ca.phon.app.opgraph.analysis;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.tree.TreePath;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.app.OpgraphIO;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.PrefHelper;

public class SaveAnalysisAction extends HookableAction {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SaveAnalysisAction.class.getName());
	
	private final static String DEFAULT_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "analysis";
	
	public final static String TXT = "Save analysis...";
	
	public final static String DESC = "Save analysis with current setup to file.";
	
	private final AnalysisWizard wizard;
	
	public SaveAnalysisAction(AnalysisWizard wizard) {
		super();
		
		this.wizard = wizard;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	public AnalysisWizard getWizard() {
		return this.wizard;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		File defaultFolder = new File(DEFAULT_FOLDER);
		if(!defaultFolder.exists()) {
			defaultFolder.mkdir();
		}
		
		final OpGraph graph = getWizard().getGraph();
		graph.setId("root");
		final WizardExtension ext = getWizard().getWizardExtension();
		
		for(OpNode optionalNode:ext.getOptionalNodes()) {
			TreePath tp = getWizard().getOptionalsTree().getNodePath(optionalNode);
			boolean enabled = getWizard().getOptionalsTree().isPathChecked(tp);
			
			ext.setOptionalNodeDefault(optionalNode, enabled);
		}
		
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setInitialFolder(DEFAULT_FOLDER);
		props.setInitialFile(getWizard().getWizardExtension().getWizardTitle() + ".xml");
		props.setParentWindow(getWizard());
		props.setTitle("Save analysis to file");
		props.setPrompt("Save analysis");
		props.setListener( (e) -> {
			if(e.getDialogData() != null) {
				String saveAs = (String)e.getDialogData();
				try {
					OpgraphIO.write(graph, new File(saveAs));
				} catch (Exception e1) {
					e1.printStackTrace();
					LOGGER.error( e1.getLocalizedMessage(), e1);
					final MessageDialogProperties msgProps = new MessageDialogProperties();
					msgProps.setOptions(MessageDialogProperties.okOptions);
					msgProps.setTitle("Unable to save analysis");
					msgProps.setMessage(e1.getLocalizedMessage());
					msgProps.setParentWindow(getWizard());
					NativeDialogs.showMessageDialog(msgProps);
				}
			}
		});
		NativeDialogs.showSaveDialog(props);
	}

}
