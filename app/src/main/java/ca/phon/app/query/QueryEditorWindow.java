/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

import ca.phon.app.project.ProjectFrameExtension;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.QueryManager;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.script.params.ScriptParam;
import ca.phon.session.SessionLocation;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.ActionTabComponent;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PathExpander;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;
import ca.phon.worker.PhonWorkerGroup;
import ca.phon.xml.XMLConverters;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
	
	public QueryEditorWindow(String title, Project project) {
		super(title);
		
		final ProjectFrameExtension pfe = new ProjectFrameExtension(project);
		putExtension(ProjectFrameExtension.class, pfe);
		
		undoManager = new UndoManager();
		scriptEditor = new ScriptPanel();
		
		init();
	}
	
	public QueryEditorWindow(String title, Project project, 
			QueryScript script) {
		this(title, project);
		
		undoManager = new UndoManager();
		scriptEditor = new ScriptPanel(script);
		
		init();
	}
	
	private Project getProject() {
		final ProjectFrameExtension pfe = getExtension(ProjectFrameExtension.class);
		return (pfe == null ? null : pfe.getProject());
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
		
		JButton execLabel = new JButton("Run Query");
		execLabel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onRunQuery();
			}
			
		});
		getRootPane().setDefaultButton(execLabel);
		
		CellConstraints cc = new CellConstraints();
		
		bottomPanel.add(positionLabel, cc.xy(1, 1));
		bottomPanel.add(includeExcludedBox, cc.xy(1, 2));
		bottomPanel.add(execLabel, cc.xy(3, 2));
		
		
		final JPanel editorPanel = new JPanel(new BorderLayout());
		editorPanel.add(toolBar, BorderLayout.NORTH);
		editorPanel.add(scriptEditor, BorderLayout.CENTER);
		editorPanel.add(bottomPanel, BorderLayout.SOUTH);

		editorTabs = new JTabbedPane();
		final QueryScript script = scriptEditor.getScript();
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
		
		final JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, selectorPane, form);
		content.setOneTouchExpandable(true);
		content.setDividerLocation(0.4f);
		
		setLayout(new BorderLayout());
		add(header, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);

		updateComponents();
	}
	
	private void updateComponents() {
		final QueryScript script = scriptEditor.getScript();
		final QueryName queryName = script.getExtension(QueryName.class);
		
		final URL location = (queryName != null ? queryName.getLocation() : null);
		String title = "";
		if(location == null) {
			title = "untitled";
		} else {
			title = (new PathExpander()).compressPath(location.getFile());
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
		final QueryScript script = scriptEditor.getScript();
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
					getProject().getLocation(), "__res" + File.separator + "script");
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
//		if(f.getName().endsWith(".js")) {
//			try {
//				queryScript.readFromFile(f);
//			} catch (IOException e) {
//				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
//			}
//		} else if (f.getName().endsWith(".xml")) {
//			// load 'query' description including parameter settings
//			final QueryManager qm = QueryManager.getSharedInstance();
//			final QueryFactory qf = qm.createQueryFactory();
//			try {
//				final Query q = qm.loadQuery(file);
//				
//				queryScript.setScript(q.getScript().getSource());
//				queryScript.setLocation(file);
//				
//				ScriptParam[] params = queryScript.getScriptParams();
//				for(ScriptParam sp:params) {
//					for(String id:sp.getParamIds()) {
//						Object v = q.getScript().getParameters().get(id);
//						if(v != null) {
//							sp.setValue(id, v);
//						}
//					}
//				}
//			} catch (IOException e) {
//				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
//			}
//		}

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
		
		final SaveQueryDialog dialog = new SaveQueryDialog(this, scriptEditor.getScript());
		dialog.setModal(true);
		
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		
		dialog.setVisible(true);
		
		final QueryScript script = scriptEditor.getScript();
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
	
	private void onRunQuery() {
		if(sessionSelector.getSelectedSessions().size() == 0) {
			final Toast toast = ToastFactory.makeToast("Select sessions to search");
			toast.start(header);
			return;
		}
		
        // create ui
        PhonLoggerConsole errDisplay = new PhonLoggerConsole();
        errDisplay.addLogger(LOGGER);
        
        final PhonWorkerGroup workerGroup = new PhonWorkerGroup(1);
        final QueryScript script = scriptEditor.getScript();

        final List<SessionLocation> selectedSessions = sessionSelector.getSelectedSessions();
        final QueryRunnerPanel queryRunnerPanel = new QueryRunnerPanel(getProject(), script, selectedSessions);
        
        final QueryName queryName = script.getExtension(QueryName.class);
        final String name = (queryName != null ? queryName.getName() : "untitled");
//		if(queryName.endsWith(".js")) {
//			queryName = queryName.substring(0, queryName.length()-3);
//		} else if(queryName.endsWith(".xml")) {
//			queryName = queryName.substring(0, queryName.length()-4);
//		}
		
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
		final QueryScript script = scriptEditor.getScript();
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
