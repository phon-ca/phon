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
package ca.phon.app.opgraph.editor;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.function.*;

import javax.swing.*;

import org.apache.commons.lang.*;

import ca.phon.opgraph.*;
import ca.phon.opgraph.library.instantiators.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.project.*;
import ca.phon.query.script.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.*;
import ca.phon.util.icons.*;

/**
 * UI for creating new {@link OpgraphEditor} documents using existing
 * documents.  This UI is used for both {@link OpGraph} analysis and
 * report document types.
 */
public class SimpleEditor extends CommonModuleFrame {

	private static final long serialVersionUID = -7170934782795308487L;

	private JMenuBar menuBar;
	
	private SimpleEditorPanel editorPanel;
	
	public SimpleEditor(Project project, OpGraphLibrary library,
			EditorModelInstantiator modelInstantiator, Instantiator<MacroNode> nodeInstantiator,
			BiFunction<QueryScript, OpGraph, MacroNode> queryNodeInstantiator,
			BiFunction<OpGraph, Project, Runnable> runFactory) {
		this(project, library, new OpGraph(), modelInstantiator, nodeInstantiator, queryNodeInstantiator, runFactory);
	}		

	/**
	 * Constructor
	 *
	 * @param project if <code>null</code> project graphs will not be displayed
	 * @param library library display in add item dialog
	 * @param graph the graph to be used
	 * @param modelInstantiator the editor model instantiator
	 * @param nodeInstantiator instantiator for nodes created by adding documents from the library
	 * @param queryNodeInstantiator instantiator for nodes created by adding queries to the doucment
	 * @param runFactory factory for runnables used to execute graphs
	 */
	public SimpleEditor(Project project, OpGraphLibrary library, OpGraph graph,
			EditorModelInstantiator modelInstantiator, Instantiator<MacroNode> nodeInstantiator,
			BiFunction<QueryScript, OpGraph, MacroNode> queryNodeInstantiator,
			BiFunction<OpGraph, Project, Runnable> runFactory) {
		super();

		this.editorPanel = new SimpleEditorPanel(project, library, graph,
				modelInstantiator, nodeInstantiator, queryNodeInstantiator, runFactory);
		PropertyChangeListener titleListener = (e) -> updateTitle();
		editorPanel.addPropertyChangeListener("currentFile", titleListener );
		editorPanel.addPropertyChangeListener("modified", titleListener );
		
		putExtension(Project.class, project);

		init();
		updateTitle();
	}
	
	public SimpleEditorPanel getEditor() {
		return this.editorPanel;
	}

	private void init() {
		setLayout(new BorderLayout());
		
		add(editorPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		this.menuBar = menuBar;
		setupMenu();
	}

	protected void setupMenu() {
		final ImageIcon saveIcn =
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		final PhonUIAction saveAct = new PhonUIAction(this, "saveData");
		saveAct.putValue(PhonUIAction.NAME, "Save");
		saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save " + getModel().getNoun().getObj1());
		saveAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
		final KeyStroke saveKs = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		saveAct.putValue(PhonUIAction.ACCELERATOR_KEY, saveKs);
		final JMenuItem saveItem = new JMenuItem(saveAct);
		
		final ImageIcon dupIcn =
				IconManager.getInstance().getIcon("actions/insert_table_row", IconSize.SMALL);
		final PhonUIAction dupAct = new PhonUIAction(editorPanel, "onDuplicate");
		dupAct.putValue(PhonUIAction.NAME, "Duplicate " + getModel().getNoun().getObj1());
		dupAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Duplicate selected " + getModel().getNoun().getObj1());
		dupAct.putValue(PhonUIAction.SMALL_ICON, dupIcn);
		final JMenuItem dupItem = new JMenuItem(dupAct);

		final ImageIcon removeIcn =
				IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(editorPanel, "onRemove");
		removeAct.putValue(PhonUIAction.NAME, "Remove " + getModel().getNoun().getObj1());
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected " + getModel().getNoun().getObj1());
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcn);
		final KeyStroke removeKs = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		removeAct.putValue(PhonUIAction.ACCELERATOR_KEY, removeKs);
		final JMenuItem removeItem = new JMenuItem(removeAct);

		final ImageIcon renameIcn =
				IconManager.getInstance().getIcon("actions/edit-rename", IconSize.SMALL);
		final PhonUIAction renameAct = new PhonUIAction(editorPanel, "onRename");
		renameAct.putValue(PhonUIAction.NAME, "Rename");
		renameAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Rename selected " + getModel().getNoun().getObj1());
		renameAct.putValue(PhonUIAction.SMALL_ICON, renameIcn);
		final JMenuItem renameItem = new JMenuItem(renameAct);

		final ImageIcon upIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-up", IconSize.SMALL);
		final PhonUIAction upAct = new PhonUIAction(editorPanel, "onMoveUp");
		upAct.putValue(PhonUIAction.NAME, "Move up");
		upAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected " + getModel().getNoun().getObj1() + " up");
		upAct.putValue(PhonUIAction.SMALL_ICON, upIcn);
		final KeyStroke upKs = KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		upAct.putValue(PhonUIAction.ACCELERATOR_KEY, upKs);
		final JMenuItem upItem = new JMenuItem(upAct);

		final ImageIcon downIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-down", IconSize.SMALL);
		final PhonUIAction downAct = new PhonUIAction(editorPanel, "onMoveDown");
		downAct.putValue(PhonUIAction.NAME, "Move down");
		downAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected " + getModel().getNoun().getObj1() + " down");
		downAct.putValue(PhonUIAction.SMALL_ICON, downIcn);
		final KeyStroke downKs = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		downAct.putValue(PhonUIAction.ACCELERATOR_KEY, downKs);
		final JMenuItem downItem = new JMenuItem(downAct);

		final ImageIcon runIcn =
				IconManager.getInstance().getIcon("actions/media-playback-start-7", IconSize.SMALL);
		final PhonUIAction runAct = new PhonUIAction(editorPanel, "onRun");
		runAct.putValue(PhonUIAction.NAME, "Run " + getModel().getNoun().getObj1() + "...");
		runAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Run " + getModel().getNoun().getObj1());
		runAct.putValue(PhonUIAction.SMALL_ICON, runIcn);
		final KeyStroke runKs = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
		runAct.putValue(PhonUIAction.ACCELERATOR_KEY, runKs);
		final JMenuItem runItem = new JMenuItem(runAct);

		final ImageIcon graphIcn =
				IconManager.getInstance().getIcon("opgraph/graph", IconSize.SMALL);
		final PhonUIAction openInComposerAct = new PhonUIAction(editorPanel, "onOpenInComposer");
		openInComposerAct.putValue(PhonUIAction.NAME, "Open in Composer (advanced)");
		openInComposerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open in Composer (advanced)");
		openInComposerAct.putValue(PhonUIAction.SMALL_ICON, graphIcn);
		final JMenuItem openInComposerItem = new JMenuItem(openInComposerAct);

		final MenuBuilder builder = new MenuBuilder(this.menuBar);

		builder.addItem("File@^", saveItem);
		builder.addSeparator("File@Save", "composer");
		builder.addItem("File@composer", runItem);
		builder.addItem("File@Run analysis...", openInComposerItem);
		builder.addSeparator("File@Open in Composer (advanced)", "remainder");

		builder.addMenu(".@Edit", "Table");
		builder.addItem("Table", renameItem);
		builder.addItem("Table", dupItem);
		builder.addItem("Table", removeItem);
		builder.addSeparator("Table", "move");
		builder.addItem("Table", upItem);
		builder.addItem("Table", downItem);
	}

	@Override
	public boolean hasUnsavedChanges() {
		return editorPanel.isModified();
	}

	public File getCurrentFile() {
		return editorPanel.getCurrentFile();
	}

	public void setCurrentFile(File source) {
		editorPanel.setCurrentFile(source);
		updateTitle();
	}

	protected void updateTitle() {
		final StringBuffer sb = new StringBuffer();
//		sb.append("Composer (" + getModel().getNoun().getObj1() + "-simple)");
		sb.append(StringUtils.capitalize(getModel().getNoun().getObj1())).append(" Composer");
		sb.append(" : ");
		if(getCurrentFile() != null)
			sb.append(getCurrentFile().getAbsolutePath());
		else
			sb.append("Untitled");
		if(hasUnsavedChanges()) {
			sb.append("*");
		}
		setWindowName(sb.toString());

		// also update modification status
		setModified(hasUnsavedChanges());
	}
	
	public OpgraphEditorModel getModel() {
		return editorPanel.getModel();
	}

	public Project getProject() {
		return getExtension(Project.class);
	}

	@Override
	public boolean saveData() throws IOException {
		return editorPanel.saveData();
	}

}
