package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;

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
		putValue(NAME, "Open Corpus Template...");
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

		PluginEntryPointRunner.executePluginInBackground("CorpusTemplate", initInfo);
	}

}
