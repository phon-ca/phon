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
package ca.phon.app.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.debugger.Main;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.ActionTabComponent;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

/**
 * Editor for query scripts.  Includes tabs for viewing the query form
 * as well as options for executing the currently edited query script.
 */
public class QueryEditorWindow extends CommonModuleFrame {
	
	private final static Logger LOGGER = Logger.getLogger(QueryEditorWindow.class.getName());
	
	/** Undo manager */
	private UndoManager undoManager;
	
	/**
	 * Modified ?
	 */
	private boolean modified = false;
	
	/** Form */
	private JComponent form;
	
	/** The header */
	private DialogHeader header;
	
	/** The tabbed form/editor pane */
	private JTabbedPane editorTabs;
	
	private JSplitPane contentPane;
	
	/** Session selector */
	private SessionSelector sessionSelector;

	/** The position label */
	private JLabel positionLabel;
	
	/** Include excluded records */
	private JCheckBox includeExcludedBox;
	
	/* Form components */
	private ScriptPanel scriptEditor;
	
	private JButton saveButton;
	private JButton saveAsButton;
	private JButton openButton;
	private JButton execButton;
	
	public QueryEditorWindow(String title, Project project) {
		super(title);
		
		putExtension(Project.class, project);
		
		undoManager = new UndoManager();
		scriptEditor = new ScriptPanel();
		
		init();
	}
	
	public QueryEditorWindow(String title, Project project, 
			QueryScript script) {
		super(title);
		
		putExtension(Project.class, project);
		
		undoManager = new UndoManager();
		scriptEditor = new ScriptPanel(script);
		
		init();
	}
	
	private Project getProject() {
		return getExtension(Project.class);
	}
	
	private JComponent createForm() {
		JComponent retVal = new JPanel();
		retVal.setLayout(new BorderLayout());
		
		ImageIcon saveIcon = 
			IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		saveButton = new JButton(saveIcon);
		saveButton.setToolTipText("Save script");
		saveButton.putClientProperty("JButton.buttonType", "textured");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(getCurrentFile() != null) {
					saveScriptToFile(getCurrentFile());
				} else {
					saveScriptAs();
				}
			}
			
		});
		
		ImageIcon saveAsIcon = 
				IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL);
		saveAsButton = new JButton(saveAsIcon);
		saveAsButton.setToolTipText("Save script as...");
		saveAsButton.putClientProperty("JButton.buttonType", "textured");
		saveAsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveScriptAs();
			}
			
		});
		
		ImageIcon openIcon = 
			IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		openButton = new JButton(openIcon);
		openButton.setToolTipText("Open script");
		openButton.putClientProperty("JButton.buttonType", "textured");
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Runnable run = new Runnable() {
					@Override
					public void run() {
						openScript();
					}
				};
				PhonWorker.getInstance().invokeLater(run);
			}
		});
		
		
		final JToolBar toolBar = new JToolBar();
//		toolBar.disableBackgroundPainter();
		toolBar.setFloatable(false);
		toolBar.add(openButton);
		toolBar.add(saveButton);
		toolBar.add(saveAsButton);
//		toolBar.addComponentToLeft(openButton);
//		toolBar.addComponentToLeft(saveButton);
//		toolBar.addComponentToLeft(saveAsButton);
		
		FormLayout bottomLayout = new FormLayout(
				"left:pref, fill:pref:grow, right:pref", "pref, pref");
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(bottomLayout);
		
		includeExcludedBox = new JCheckBox("Include excluded records");
		
		positionLabel = new JLabel();
		positionLabel.setFont(positionLabel.getFont().deriveFont(10.0f));
		
		execButton = new JButton("Run Query");
		execButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onRunQuery();
			}
			
		});
		getRootPane().setDefaultButton(execButton);
		
		CellConstraints cc = new CellConstraints();
		
		bottomPanel.add(positionLabel, cc.xy(1, 1));
		bottomPanel.add(includeExcludedBox, cc.xy(1, 2));
		bottomPanel.add(execButton, cc.xy(3, 2));
		
		
		final JPanel editorPanel = new JPanel(new BorderLayout());
		editorPanel.add(toolBar, BorderLayout.NORTH);
		editorPanel.add(scriptEditor, BorderLayout.CENTER);
		editorPanel.add(bottomPanel, BorderLayout.SOUTH);

		editorTabs = new JTabbedPane();
		final QueryScript script = (QueryScript)scriptEditor.getScript();
		final QueryName queryName = script.getExtension(QueryName.class);
		final String name = (queryName != null ? queryName.getName() : "untitled");
		editorTabs.add("Script : " + name, editorPanel);
		
		retVal.add(editorTabs, BorderLayout.CENTER);
		return retVal;
	}
	
	public void init() {
		scriptEditor.addPropertyChangeListener(ScriptPanel.SCRIPT_PROP, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// set modification flag
				if(!isModified()) {
					setModified(true);
					updateComponents();
				}
				
			}
		});
		
		header = new DialogHeader("Query Project", "Perform a search on selected corpora/sessions.");
		
		form = createForm();
		final Dimension formSize = form.getPreferredSize();
		formSize.width = 800-230;
		formSize.height = 550;
		form.setPreferredSize(formSize);
		
		sessionSelector = new SessionSelector(getProject()) {
			@Override
			public Dimension getPreferredSize() {
				Dimension retVal = super.getPreferredSize();
				retVal.width = 230;
				return retVal;
			}
		};
		
		JScrollPane selectorPane = new JScrollPane(sessionSelector);
		
		contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, selectorPane, form);
		contentPane.setOneTouchExpandable(true);
		contentPane.setDividerLocation(0.4f);
		
		setLayout(new BorderLayout());
		add(header, BorderLayout.NORTH);
		add(contentPane, BorderLayout.CENTER);

		updateComponents();
	}
	
	private void updateComponents() {
		final QueryScript script = (QueryScript)scriptEditor.getScript();
		final QueryName queryName = script.getExtension(QueryName.class);
		
		final URL location = (queryName != null ? queryName.getLocation() : null);
		String title = "";
		if(location == null) {
			title = "untitled";
		} else {
			try {
				title = location.toURI().toASCIIString();
//					(new PathExpander()).compressPath(location.getFile());
			} catch (URISyntaxException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		setWindowName(title + (hasUnsavedChanges() ? " *" : ""));
		
		final String name = (queryName != null ? queryName.getName() : "untitled");
		final String scriptTitle = "Query : " + name + (hasUnsavedChanges() ? " *" : "");
		
		editorTabs.setTitleAt(0, scriptTitle);
		saveButton.setEnabled(isModified());
	}
	
	/**
	 * Path to the current file
	 * @return current file path or null if not set
	 * 
	 */
	public String getCurrentFile() {
		final QueryScript script = (QueryScript)scriptEditor.getScript();
		final QueryName queryName = script.getExtension(QueryName.class);
		
		final URL url = (queryName != null ? queryName.getLocation() : null);
		final String retVal = (url != null ? url.getPath() : null);
		return retVal;
	}
	
	/**
	 * Folder for current script.
	 * 
	 * @return parent folder path or path to the
	 * project script directory if getCurrentFile()
	 * is null
	 */
	public String getParentFolder() {
		File parentFolder = null;
		
		if(getCurrentFile() != null) {
			File f = new File(getCurrentFile());
			parentFolder = f.getParentFile();
		} else {
			parentFolder = new File(
					getProject().getResourceLocation(), "script");
		}
		
		return parentFolder.getAbsolutePath();
	}
	
	/**
	 * Return name for current file.
	 * 
	 * @return name of current file or 'Untitled' if
	 * getCurrentFile() is null
	 */
	public String getFilename() {
		String retVal = "Untitled";
		
		if(getCurrentFile() != null) {
			File f = new File(getCurrentFile());
			retVal = f.getName();
		}
		
		return retVal;
	}

	public void openScript() {
		FileFilter[] filters = new FileFilter[2];
		filters[1] = FileFilter.jsFilter;
		filters[0] = FileFilter.xmlFilter;
		
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setInitialFolder(getParentFolder());
		props.setFileFilter(new FileFilter(filters));
		props.setTitle("Open Query Script");
		props.setRunAsync(false);
		
		final List<String> selectedFiles = NativeDialogs.showOpenDialog(props);
		
		if(selectedFiles.size() > 0) {
			openFromFile(selectedFiles.get(0), false);
		}
	}

	public void openFromFile(String file, boolean suppressWarnings) {
		final File f = new File(file);
		
		QueryScript queryScript;
		try {
			queryScript = new QueryScript(f.toURI().toURL());
			scriptEditor.setScript(queryScript);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		setModified(false);
		updateComponents();
	}
	
	public void setModified(boolean modified) {
		this.modified = modified;
		
		JRootPane root = super.getRootPane();
		root.putClientProperty( "Window.documentModified", new Boolean(modified) );
	}
	
	public boolean isModified() {
		return this.modified;
	}
	
	@Override
	public boolean hasUnsavedChanges() {
		return isModified();
	}
	
	@Override
	public boolean saveData() 
		throws IOException {
		
		if(getCurrentFile() != null && (new File(getCurrentFile())).canWrite()) {
			return saveScriptToFile(getCurrentFile());
		} else {
			return saveScriptAs();
		}
	}
	
	/**
	 * Save script to file, first asking for location.
	 */
	public boolean saveScriptAs() {
//		FileFilter[] filters = new FileFilter[2];
//		filters[1] = FileFilter.jsFilter;
//		filters[0] = FileFilter.xmlFilter;
//		
//		String file = 
//			NativeDialogs.showSaveFileDialogBlocking(this,
//					getParentFolder(), getFilename(),
//					".xml", filters, "Save Query");
//		if(file != null) {
//			scriptEditor.getScript().setLocation(file);
//			
//			return saveScriptToFile(file);
//		} else {
//			return false;
//		}
		
		final QueryScript script = (QueryScript)scriptEditor.getScript();
		
		final SaveQueryDialog dialog = new SaveQueryDialog(this, script);
		dialog.setModal(true);
		
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		
		dialog.setVisible(true);
		
		final QueryName queryName = script.getExtension(QueryName.class);
		final URL location = (queryName != null ? queryName.getLocation() : null);
		
		if(location != null) {
			setModified(false);
			updateComponents();
		}
		
		return true;
	}
	
	public boolean saveScriptToFile(String file) {
		return saveScriptToFile(file, false);
	}
	
	public boolean saveScriptToFile(String file, boolean suppressWarnings) {
		boolean retVal = false;
		
		try {
			final QueryScript qs = new QueryScript(getScript());
			QueryScriptLibrary.saveScriptToFile(qs, file);
			
			setModified(false);
			updateComponents();
			retVal = true;
		} catch (IOException e) {
			e.printStackTrace();
			if(!suppressWarnings) {
				NativeDialogs.showMessageDialogBlocking(this, null, "Save failed", e.getMessage());
			}
		}
		
		return retVal;
	}
	
	private void onDebugQuery() {
		final QueryScript script = (QueryScript)scriptEditor.getScript();
		final QueryName queryName = script.getExtension(QueryName.class);
		final String name = (queryName != null ? queryName.getName() : "untitled");
		
		final Main debugger = Main.mainEmbedded("Debugger : " + name);
		debugger.setBreakOnEnter(false);
		debugger.setBreakOnExceptions(true);
		
		PhonScriptContext scriptContext = script.getContext();
		try {
			final ScriptParameters scriptParams = scriptContext.getScriptParameters(scriptContext.getEvaluatedScope());
			
			// we need to reset the context to activate debugging
			script.resetContext();
			scriptContext = script.getContext();
			
			final Context ctx = scriptContext.enter();
			final ScriptableObject debugScope = ctx.initStandardObjects();
			ctx.setOptimizationLevel(-1);
			debugger.attachTo(ctx.getFactory());
			debugger.setScope(debugScope);
			scriptContext.exit();
			
			final ScriptParameters newParams = scriptContext.getScriptParameters(scriptContext.getEvaluatedScope(debugScope));
			ScriptParameters.copyParams(scriptParams, newParams);
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		debugger.setExitAction(new Runnable() {
			
			@Override
			public void run() {
				debugger.detach();
				debugger.setVisible(false);
			}
			
		});
		// break on entering main query script
		debugger.doBreak();
		debugger.setSize(500, 600);
		debugger.setVisible(true);
		debugger.go();
		
		onRunQuery();
	}
	
	private void onRunQuery() {
		if(sessionSelector.getSelectedSessions().size() == 0) {
			final Toast toast = ToastFactory.makeToast("Select sessions to search");
			toast.start(header);
			return;
		}
		
		if(!scriptEditor.checkParams()) {
			final Toast toast = ToastFactory.makeToast("Invalid parameters, please check query form");
			toast.start(execButton);
			return;
		}
		
        // create ui
        PhonLoggerConsole errDisplay = new PhonLoggerConsole();
        errDisplay.addLogger(LOGGER);
        
        final QueryScript editorScript = (QueryScript)scriptEditor.getScript();
        final QueryScript script = (QueryScript)editorScript.clone();
        
        final List<SessionPath> selectedSessions = sessionSelector.getSelectedSessions();
        final QueryRunnerPanel queryRunnerPanel = new QueryRunnerPanel(getProject(), script, selectedSessions, includeExcludedBox.isSelected());
        
        final QueryName queryName = script.getExtension(QueryName.class);
        final String name = (queryName != null ? queryName.getName() : "untitled");
        
		String tabName = "Results : " + name;
		editorTabs.addTab(tabName, queryRunnerPanel);
		editorTabs.setSelectedIndex(editorTabs.getTabCount()-1);
		
		queryRunnerPanel.startQuery();
		final ImageIcon closeIcon = IconManager.getInstance().getIcon("actions/process-stop", IconSize.XSMALL);
		final PhonUIAction closeAction = new PhonUIAction(this, "discardResults", queryRunnerPanel);
		closeAction.putValue(PhonUIAction.SMALL_ICON, closeIcon);
		closeAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Discard results and close tab");
		final ActionTabComponent atc = new ActionTabComponent(editorTabs, closeAction);
		final int tabIdx = editorTabs.getTabCount()-1;
		editorTabs.setTabComponentAt(tabIdx, atc);
		
		final QueryNameListener nameListener = new QueryNameListener(tabIdx);
		queryRunnerPanel.addPropertyChangeListener(QueryRunnerPanel.QUERY_SAVED_PROP, nameListener);
	}
	
	/**
	 * Discard results tab
	 *
	 * @return <code>true</code> if query was canceled/removed, 
	 *  <code>false</code> otherwise.
	 */
	public boolean discardResults(QueryRunnerPanel panel) {
		final int idx = editorTabs.indexOfComponent(panel);
		if(idx > 0) {
			if(panel.isRunning()) {
				// XXX causing thread lock on EDT - use non-block dialog or background thread
				int result = 
						NativeDialogs.showOkCancelDialogBlocking(QueryEditorWindow.this, null, "Cancel Query", "Stop query?");
				if(result == 0) {
					if(idx >= 0 && idx < editorTabs.getTabCount()) {
						panel.stopQuery();
						return true;
					}
				} else {
					return false;
				}
			} else {
				if(idx >= 0 && idx < editorTabs.getTabCount()) {
					editorTabs.removeTabAt(idx);
					return true;
				}
			}
		}
		// should never really get here
		return false;
	}
	
	@Override
	public void setJMenuBar(JMenuBar menu) {
		super.setJMenuBar(menu);
		
		JMenuItem debugItem = new JMenuItem("Debug query");
		KeyStroke debugKs = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		debugItem.setAccelerator(debugKs);
		debugItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onDebugQuery();
			}
			
		});
		
		JMenuItem execItem = new JMenuItem("Run query");
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
		execItem.setAccelerator(ks);
		execItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onRunQuery();
			}
		});
		
		JMenuItem saveItem = new JMenuItem("Save script");
		KeyStroke ks1 = KeyStroke.getKeyStroke(KeyEvent.VK_S, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		saveItem.setAccelerator(ks1);
		saveItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(getCurrentFile() != null) {
					saveScriptToFile(getCurrentFile());
				} else {
					saveScriptAs();
				}
			}
		});
		
		JMenuItem saveAsItem = new JMenuItem("Save script as...");
		KeyStroke ks2 = KeyStroke.getKeyStroke(KeyEvent.VK_S, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);
		saveAsItem.setAccelerator(ks2);
		saveAsItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saveScriptAs();
			}
		});
		
		final JMenu fileMenu = menu.getMenu(0);
		
		fileMenu.add(saveItem, 1);
		fileMenu.add(saveAsItem, 2);
		fileMenu.add(execItem, 3);
		if(PrefHelper.getBoolean("phon.debug", false)) {
			fileMenu.add(debugItem, 4);
		}
	}
	
	private class PipeListenerTask extends PhonTask {
		
		// the pipe to listen on
		private PipedReader pipe;
		
		// the colour to use in the console display
		private String style;
		
		private StyledDocument doc;
		
		public PipeListenerTask(PipedReader p,StyledDocument doc) {
			this(p, doc, "scriptDefault");
		}
		
		public PipeListenerTask(PipedReader p, StyledDocument doc, String s) {
			super();
			
			this.doc = doc;
			this.pipe = p;
			this.style = s;
		}

		@Override
		public void performTask() {
			BufferedReader in = new BufferedReader(pipe);
			
			while(!super.isShutdown()) {
				try {
					String line = in.readLine() + "\n";
					
					try {
						doc.insertString(doc.getLength(), line, 
								doc.getStyle(style));
					} catch (BadLocationException e) {
						
					}
				} catch (IOException e) {
					break;
				}
			}
		}
		
	}
	
	public String getScript() {
		final QueryScript script = (QueryScript)scriptEditor.getScript();
		return script.getScript();
	}
	
	@Override
	public void close() {
		boolean okToClose = true;
		for(int tabIdx = 0; tabIdx < editorTabs.getTabCount(); tabIdx++) {
			final Component tabComp = editorTabs.getComponentAt(tabIdx);
			if(tabComp instanceof QueryRunnerPanel) {
				final QueryRunnerPanel qtp = QueryRunnerPanel.class.cast(tabComp);
				okToClose &= qtp.isSaved();
			}
		}
		
		if(!okToClose) {
			final String msgTitle = "Close window?";
			final String msgText = "Any unsaved results will be lost.";
			
			final int retVal = NativeDialogs.showOkCancelDialogBlocking(this, null, msgTitle, msgText);
			if(retVal == 0)
				okToClose = true;
		}
		
		if(okToClose) {
			// make sure to shutdown any working threads
			for(int tabIdx = 0; tabIdx < editorTabs.getTabCount(); tabIdx++) {
				final Component tabComp = editorTabs.getComponentAt(tabIdx);
				if(tabComp instanceof QueryRunnerPanel) {
					final QueryRunnerPanel qtp = QueryRunnerPanel.class.cast(tabComp);
					if(qtp.isRunning()) {
						qtp.stopQuery();
					}
				}
			}
			super.close();
		}
	}

	/**
	 * Property change listener for changing tab names when
	 * result sets are saved in query history.
	 */
	private class QueryNameListener implements PropertyChangeListener {
		
		private final int tabIdx;
		
		public QueryNameListener(int tabIdx) {
			this.tabIdx = tabIdx;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String tabName = "Results : ";
			final QueryRunnerPanel taskPanel = 
					(QueryRunnerPanel)evt.getSource();
			tabName += taskPanel.getQuery().getName();
			
			editorTabs.setTitleAt(tabIdx, tabName);
			editorTabs.revalidate();
		}

	}
}
