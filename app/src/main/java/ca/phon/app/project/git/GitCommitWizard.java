/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.project.git;

import ca.phon.app.project.git.CommitTableModel.FileStatus;
import ca.phon.app.project.git.actions.GitProgressBuffer;
import ca.phon.project.Project;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.ui.wizard.*;
import ca.phon.util.PrefHelper;
import ca.phon.worker.PhonWorker;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jdesktop.swingx.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class GitCommitWizard extends WizardFrame {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(GitCommitWizard.class.getName());

	private static final long serialVersionUID = -3292049153895936174L;
	
	private final static String PUSH_CHANGES_KEY = "GitCommitWizard.pushChanges";
	private final static Boolean DEFAULT_PUSH_CHANGES = Boolean.TRUE;
	
	private Project project;
	
	private CommitTableModel unindexedModel;
	private JXTable unindexedTable;
	
	private CommitTableModel indexedModel;
	private JXTable indexedTable;
	
	private JButton addAllButton;
	
	private JButton addToIndexButton;
	
	private JButton removeAllFromIndexButton;
	
	private JButton removeFromIndexButton;
	
	private JTextArea commitArea;
	
	private GitProgressBuffer buffer;
	
	private JCheckBox pushImmediatelyBox;
	
	private WizardStep step1;
	
	private WizardStep step2;

	public GitCommitWizard(Project project) {
		super(project.getName() + " : Commit Changes");
		this.project = project;
		putExtension(Project.class, project);
		init();
		
		btnFinish.setVisible(false);
		btnNext.setText("Commit");
	}
	
	private void init() {
		step1 = createStep1();
		step1.setNextStep(1);
		addWizardStep(step1);
		
		step2 = createStep2();
		step2.setPrevStep(0);
		addWizardStep(step2);
	}
	
	private DialogHeader createHeader() {
		final DialogHeader header = 
				new DialogHeader("Commit", "Commit changes");
		return header;
	}
	
	private WizardStep createStep1() {
		final WizardStep step = new WizardStep();
		step.setLayout(new BorderLayout());
		
		step.add(createHeader(), BorderLayout.NORTH);

		final ProjectGitController gitController = new ProjectGitController(project);
		Status status = null;
		try(Git git = gitController.open()) {
			status = gitController.status();
		} catch (IOException | GitAPIException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		unindexedModel = new CommitTableModel(status, false);
		unindexedTable = new JXTable(unindexedModel);
		unindexedTable.setVisibleRowCount(5);
		final JScrollPane unindexedScroller = new JScrollPane(unindexedTable);
		
		indexedModel = new CommitTableModel(status, true);
		indexedTable = new JXTable(indexedModel);
		indexedTable.setVisibleRowCount(5);
		final JScrollPane indexedScroller = new JScrollPane(indexedTable);
		
		final PhonUIAction<Void> addToIndexAction = PhonUIAction.runnable(this::onAddToIndex);
		addToIndexAction.putValue(PhonUIAction.NAME, "Add");
		addToIndexAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add selected changes to index");
		addToIndexButton = new JButton(addToIndexAction);
		
		final PhonUIAction<Void> addAllAction = PhonUIAction.runnable(this::onAddAllToIndex);
		addAllAction.putValue(PhonUIAction.NAME, "Add all");
		addAllAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add all changes to index");
		addAllButton = new JButton(addAllAction);
		
		final PhonUIAction<Void> removeAllFromIndexAction = PhonUIAction.runnable(this::onRemoveAllFromIndex);
		removeAllFromIndexAction.putValue(PhonUIAction.NAME, "Remove all");
		removeAllFromIndexAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove all changes from index");
		removeAllFromIndexButton = new JButton(removeAllFromIndexAction);
		
		final PhonUIAction<Void> removeFromIndexAction = PhonUIAction.runnable(this::onRemoveFromIndex);
		removeFromIndexAction.putValue(PhonUIAction.NAME, "Remove");
		removeFromIndexAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected changes from index");
		removeFromIndexButton = new JButton(removeFromIndexAction);
	
		commitArea = new JTextArea();
		commitArea.setRows(3);
		final JScrollPane commitScroller = new JScrollPane(commitArea);
		
		final PhonUIAction pushChangesAct = PhonUIAction.runnable(() -> {});
		pushChangesAct.putValue(PhonUIAction.NAME, "Immediately push changes");
		pushChangesAct.putValue(PhonUIAction.SELECTED_KEY, 
				PrefHelper.getBoolean(PUSH_CHANGES_KEY, DEFAULT_PUSH_CHANGES));
		pushImmediatelyBox = new JCheckBox(pushChangesAct);
		
		final JPanel unstagedPanel = new JPanel(new VerticalLayout());
		unstagedPanel.add(unindexedScroller);
		unstagedPanel.add(ButtonBarBuilder.buildOkCancelBar(addAllButton, addToIndexButton));
		unstagedPanel.setBorder(BorderFactory.createTitledBorder("Unstaged Changes"));
		
		final JPanel stagedPanel = new JPanel(new VerticalLayout());
		stagedPanel.add(indexedScroller);
		stagedPanel.add(ButtonBarBuilder.buildOkCancelBar(removeAllFromIndexButton, removeFromIndexButton));
		stagedPanel.setBorder(BorderFactory.createTitledBorder("Staged Changes"));
		
		final JPanel commitPanel = new JPanel(new VerticalLayout());
		commitPanel.add(commitScroller);
		commitPanel.add(pushImmediatelyBox);
		commitPanel.setBorder(BorderFactory.createTitledBorder("Commit Message"));
		
		final JPanel contentPane = new JPanel(new VerticalLayout());
		contentPane.add(unstagedPanel);
		contentPane.add(stagedPanel);
		contentPane.add(commitPanel);
		
		step.add(contentPane, BorderLayout.CENTER);
		
		return step;
	}
	
	private WizardStep createStep2() {
		final WizardStep step = new WizardStep();
		step.setLayout(new BorderLayout());

		buffer = new GitProgressBuffer(getTitle());
		step.add(createHeader(), BorderLayout.NORTH);
		step.add(buffer, BorderLayout.CENTER);
		
		return step;
	}
	
	public void updateStatus(Status newStatus) {
		unindexedModel.setStatus(newStatus);
		indexedModel.setStatus(newStatus);
	}
	
	public void onAddAllToIndex() {
		final ProjectGitController gitController = new ProjectGitController(project);
		try(Git git = gitController.open()) {
			git.add().addFilepattern(".").call();
			git.add().addFilepattern(".").setUpdate(true).call();
			updateStatus(gitController.status());
		} catch (IOException | GitAPIException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast(e.getLocalizedMessage()).start(addAllButton);
		}
	}
	
	public void onAddToIndex() {
		final ProjectGitController gitController = new ProjectGitController(project);
		List<String> unindexedFiles = unindexedModel.getList();
		
		try(Git git = gitController.open()) {
			int[] selectedRows = unindexedTable.getSelectedRows();
			for(int selectedRow:selectedRows) {
				int selectedIndex = unindexedTable.convertRowIndexToModel(selectedRow);
				String fileToAdd = unindexedFiles.get(selectedIndex);
				
				if(unindexedModel.getStatus(fileToAdd) == 
						FileStatus.MISSING) {
					git.rm().addFilepattern(fileToAdd).call();
				} else {
					git.add().addFilepattern(fileToAdd).call();
				}
			}
			updateStatus(gitController.status());
		} catch (IOException | GitAPIException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast(e.getLocalizedMessage()).start(addToIndexButton);
		}
	}
	
	public void onRemoveAllFromIndex() {
		final ProjectGitController gitController = new ProjectGitController(project);
		
		try(Git git = gitController.open()) {
			git.reset().setRef("HEAD").call();
			updateStatus(gitController.status());
		} catch (IOException | GitAPIException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast(e.getLocalizedMessage()).start(removeFromIndexButton);
		}
	}
	
	public void onRemoveFromIndex() {
		final ProjectGitController gitController = new ProjectGitController(project);
		List<String> indexedFiles = indexedModel.getList();
		
		try(Git git = gitController.open()) {
			int[] selectedRows = indexedTable.getSelectedRows();
			for(int selectedRow:selectedRows) {
				int selectedIndex = indexedTable.convertRowIndexToModel(selectedRow);
				String fileToRemove = indexedFiles.get(selectedIndex);
				git.reset().addPath(fileToRemove).setRef("HEAD").call();
			}
			updateStatus(gitController.status());
		} catch (IOException | GitAPIException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast(e.getLocalizedMessage()).start(removeFromIndexButton);
		}
	}
	
	private void doCommit() {
		final ProjectGitController gitController = new ProjectGitController(project);
		final String commitMessage = commitArea.getText().trim();
		final boolean doPush = pushImmediatelyBox.isSelected();
		
		Runnable doCommit = () -> {
			try(Git git = gitController.open()) {
				gitController.commit(commitMessage);
				if(doPush) {
					gitController.push(buffer);
				}
			} catch (IOException | GitAPIException e) {

			}
		};
		PhonWorker.getInstance().invokeLater(doCommit);
	}

	@Override
	protected void next() {
		if(getCurrentStep() == step1) {
			// make sure we have at least one change staged and a commit comment
			if(indexedModel.getRowCount() == 0) {	
				ToastFactory.makeToast("").start(indexedTable);
				return;
			}
			if(commitArea.getText().trim().length() == 0) {
				ToastFactory.makeToast("Please enter a commit comment").start(commitArea);
				return;
			}
			doCommit();
		}
		super.next();
	}
	
	
	
}
