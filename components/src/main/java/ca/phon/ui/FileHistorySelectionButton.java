package ca.phon.ui;

import ca.phon.util.RecentFiles;

import java.io.File;

public class FileHistorySelectionButton extends FileSelectionButton {

	private String historyProp;

	public FileHistorySelectionButton(String historyProp) {
		super();

		this.historyProp = historyProp;
		setFiles(new RecentFiles(historyProp));
	}

	/**
	 * Save current selection to history
	 *
	 */
	public void saveSelectionToHistory() {
		if(getSelection() == null) return;
		if(this.getFiles() instanceof RecentFiles)
			((RecentFiles)this.getFiles()).addToHistory(getSelection());
	}

}
