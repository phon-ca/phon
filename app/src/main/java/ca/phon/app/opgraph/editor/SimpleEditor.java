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
package ca.phon.app.opgraph.editor;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.WordUtils;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.JXStatusBar.Constraint.ResizeBehavior;

import ca.hedlund.desktopicons.*;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.opgraph.nodes.*;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.edits.NodeWizardOptionalsEdit;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.edits.graph.*;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.extensions.NodeMetadata;
import ca.phon.opgraph.library.instantiators.Instantiator;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.*;
import ca.phon.util.resources.ResourceLoader;
import ca.phon.worker.PhonWorker;

/**
 * UI for creating new {@link OpgraphEditor} documents using existing
 * documents.  This UI is used for both {@link OpGraph} analysis and
 * report document types.
 */
public class SimpleEditor extends CommonModuleFrame {

	private static final long serialVersionUID = -7170934782795308487L;

	private JMenuBar menuBar;
	
	private SimpleEditorPanel editorPanel;

	/**
	 * Constructor
	 *
	 * @param project if <code>null</code> project graphs will not be displayed
	 * @param library library display in add item dialog
	 * @param modelInstantiator the editor model instantiator
	 * @param nodeInstantiator instantiator for nodes created by adding documents from the library
	 * @param queryNodeInstantiator instantiator for nodes created by adding queries to the doucment
	 * @param runFactory factory for runnables used to execute graphs
	 */
	public SimpleEditor(Project project, OpGraphLibrary library,
			EditorModelInstantiator modelInstantiator, Instantiator<MacroNode> nodeInstantiator,
			Function<QueryScript, MacroNode> queryNodeInstantiator,
			BiFunction<OpGraph, Project, Runnable> runFactory) {
		super();

		this.editorPanel = new SimpleEditorPanel(project, library, 
				modelInstantiator, nodeInstantiator, queryNodeInstantiator, runFactory);
		getModel().getDocument().getUndoSupport().addUndoableEditListener( (e) -> updateTitle() );
		editorPanel.addPropertyChangeListener("currentFile", (e) -> updateTitle() );

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
		final KeyStroke saveKs = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		saveAct.putValue(PhonUIAction.ACCELERATOR_KEY, saveKs);
		final JMenuItem saveItem = new JMenuItem(saveAct);

		final ImageIcon addIcn =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final PhonUIAction addAct = new PhonUIAction(editorPanel, "onAdd");
		addAct.putValue(PhonUIAction.NAME, "Add " + getModel().getNoun().getObj1() + "...");
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add " + getModel().getNoun().getObj1() + "...");
		addAct.putValue(PhonUIAction.SMALL_ICON, addIcn);
		final JMenuItem addItem = new JMenuItem(addAct);

		final ImageIcon removeIcn =
				IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(editorPanel, "onRemove");
		removeAct.putValue(PhonUIAction.NAME, "Remove " + getModel().getNoun().getObj1());
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected " + getModel().getNoun().getObj1());
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcn);
		final KeyStroke removeKs = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		removeAct.putValue(PhonUIAction.ACCELERATOR_KEY, removeKs);
		final JMenuItem removeItem = new JMenuItem(removeAct);

		final ImageIcon settingsIcn =
				IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL);
		final PhonUIAction settingsAct = new PhonUIAction(editorPanel, "onShowSettings");
		settingsAct.putValue(PhonUIAction.NAME, "Settings...");
		settingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show settings for selected " + getModel().getNoun().getObj1());
		settingsAct.putValue(PhonUIAction.SMALL_ICON, settingsIcn);
		final KeyStroke settingsKs = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		settingsAct.putValue(PhonUIAction.ACCELERATOR_KEY, settingsKs);
		final JMenuItem settingsItem = new JMenuItem(settingsAct);

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
		final KeyStroke upKs = KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		upAct.putValue(PhonUIAction.ACCELERATOR_KEY, upKs);
		final JMenuItem upItem = new JMenuItem(upAct);

		final ImageIcon downIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-down", IconSize.SMALL);
		final PhonUIAction downAct = new PhonUIAction(editorPanel, "onMoveDown");
		downAct.putValue(PhonUIAction.NAME, "Move down");
		downAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected " + getModel().getNoun().getObj1() + " down");
		downAct.putValue(PhonUIAction.SMALL_ICON, downIcn);
		final KeyStroke downKs = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
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
		builder.addItem("Table", addItem);
		builder.addItem("Table", removeItem);
		builder.addSeparator("Table", "settings");
		builder.addItem("Table", settingsItem);
		builder.addItem("Table", renameItem);
		builder.addSeparator("Table", "move");
		builder.addItem("Table", upItem);
		builder.addItem("Table", downItem);
	}

	@Override
	public boolean hasUnsavedChanges() {
		return getModel().getDocument().hasModifications();
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
		sb.append("Composer (" + getModel().getNoun().getObj1() + "-simple)");
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

	public boolean chooseFile() {
		return editorPanel.chooseFile();
	}

	@Override
	public boolean saveData() throws IOException {
		return editorPanel.saveData();
	}

}
