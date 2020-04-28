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
package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;

import ca.phon.app.corpus.CorpusTemplateEP;
import ca.phon.app.log.LogUtil;
import ca.phon.app.project.ProjectWindow;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.ui.toast.ToastFactory;

public class OpenCorpusTemplateAction extends ProjectWindowAction {

	private static final long serialVersionUID = 6335879665708654561L;
	
	private String corpus;
	
	public OpenCorpusTemplateAction(ProjectWindow projectWindow) {
		this(projectWindow, null);
	}

	public OpenCorpusTemplateAction(ProjectWindow projectWindow, String corpus) {
		super(projectWindow);
		this.corpus = corpus;
		putValue(NAME, "Open corpus template...");
		putValue(SHORT_DESCRIPTION, "Open template for sesssion in the selected corpus");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final String corpus = 
				(this.corpus == null ? getWindow().getSelectedCorpus() : this.corpus);
		if(corpus == null) {
			ToastFactory.makeToast("Please select a corpus").start(getWindow().getCorpusList());
			return;
		}
		
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getWindow().getProject());
		initInfo.put("corpusName", corpus);
		
		PluginEntryPointRunner.executePluginInBackground(CorpusTemplateEP.EP_NAME, initInfo);
	}

}
