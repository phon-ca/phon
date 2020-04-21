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
package ca.phon.app.session.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import org.apache.logging.log4j.LogManager;
import org.jdesktop.swingx.JXStatusBar;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.StockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.menu.edit.PreferencesCommand;
import ca.phon.app.prefs.EditorPrefsPanel;
import ca.phon.app.project.ProjectFrame;
import ca.phon.app.project.actions.SelectCorpusMediaFolder;
import ca.phon.app.session.editor.actions.AssignMediaAction;
import ca.phon.app.session.editor.actions.CopyRecordAction;
import ca.phon.app.session.editor.actions.CutRecordAction;
import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.actions.DuplicateRecordAction;
import ca.phon.app.session.editor.actions.ExportAdjacencySequenceAction;
import ca.phon.app.session.editor.actions.ExportAsHTMLAction;
import ca.phon.app.session.editor.actions.ExportCustomSegmentAction;
import ca.phon.app.session.editor.actions.ExportSegmentAction;
import ca.phon.app.session.editor.actions.ExportSpeechTurnAction;
import ca.phon.app.session.editor.actions.FirstRecordAction;
import ca.phon.app.session.editor.actions.LastRecordAction;
import ca.phon.app.session.editor.actions.MoveRecordBackwardAction;
import ca.phon.app.session.editor.actions.MoveRecordForwardAction;
import ca.phon.app.session.editor.actions.MoveRecordToBeginningAction;
import ca.phon.app.session.editor.actions.MoveRecordToEndAction;
import ca.phon.app.session.editor.actions.NewRecordAction;
import ca.phon.app.session.editor.actions.NextRecordAction;
import ca.phon.app.session.editor.actions.PasteRecordAction;
import ca.phon.app.session.editor.actions.PlayAdjacencySequenceAction;
import ca.phon.app.session.editor.actions.PlayCustomSegmentAction;
import ca.phon.app.session.editor.actions.PlaySegmentAction;
import ca.phon.app.session.editor.actions.PlaySpeechTurnAction;
import ca.phon.app.session.editor.actions.PreviousRecordAction;
import ca.phon.app.session.editor.actions.SaveAsAction;
import ca.phon.app.session.editor.actions.SaveSessionAction;
import ca.phon.app.session.editor.actions.SortRecordsAction;
import ca.phon.app.session.editor.actions.UnassignMediaAction;
import ca.phon.app.session.editor.undo.SessionEditorUndoSupport;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.SystemTierType;
import ca.phon.session.Transcriber;
import ca.phon.session.io.OriginalFormat;
import ca.phon.session.io.SerializationWarnings;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.SessionWriter;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuManager;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.ByteSize;
import ca.phon.util.Language;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * <p>Main UI for the application.  This window provides the interface for
 * creating and modifying Phon {@link Session}s.</p>
 *
 * <p>This window supports plug-ins.  Plug-ins can provide custom EditorViews.</p>
 */
public class SessionEditor extends ProjectFrame implements ClipboardOwner {

	private final static long serialVersionUID = 2831713307191769522L;

	public final static String BACKUP_WHEN_SAVING =
			SessionEditor.class.getName() + ".backupWhenSaving";
	private boolean backupWhenSaving = PrefHelper.getBoolean(BACKUP_WHEN_SAVING, Boolean.TRUE);

	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SessionEditor.class.getName());

	private final static String WINDOW_NAME = "Session Editor";

	/**
	 * UI Model
	 */
	private final transient AtomicReference<EditorViewModel> viewModelRef;

	/**
	 * Data model
	 */
	private final transient AtomicReference<EditorDataModel> dataModelRef;

	/**
	 * Event manager
	 */
	private final transient AtomicReference<EditorEventManager> eventManagerRef;

	/**
	 * Selection model
	 */
	private final transient AtomicReference<EditorSelectionModel> selectionModelRef;
	
	/**
	 * Media model
	 */
	private final transient AtomicReference<SessionMediaModel> mediaModelRef;

	/**
	 * Index of the current record
	 *
	 */
	private volatile transient int currentRecord = 0;

	/*
	 * Undo/Redo support
	 */
	/**
	 * Undo support for the editor
	 */
	private final SessionEditorUndoSupport undoSupport = new SessionEditorUndoSupport(this);

	/**
	 * Undo manager
	 */
	private final UndoManager undoManager = new UndoManager();

	private final UndoableEditListener undoListener = new UndoableEditListener() {

		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.addEdit(e.getEdit());
			setModified(true);
		}

	};
	
	/**
	 * Toolbar
	 */
	private SessionEditorToolbar toolbar;

	/**
	 * Status bar
	 *
	 */
	private SessionEditorStatusBar statusBar;

	/**
	 * Constructor
	 */
	public SessionEditor(Project project, Session session, Transcriber transcriber) {
		super(project);
		putExtension(Session.class, session);

		this.dataModelRef =
				new AtomicReference<EditorDataModel>(new DefaultEditorDataModel(session));
		getDataModel().setTranscriber(transcriber);
		this.eventManagerRef =
				new AtomicReference<EditorEventManager>(new EditorEventManager(this));
		this.selectionModelRef =
				new AtomicReference<EditorSelectionModel>(new DefaultEditorSelectionModel());
		this.viewModelRef =
				new AtomicReference<EditorViewModel>(new DefaultEditorViewModel(this));
		this.mediaModelRef = 
				new AtomicReference<SessionMediaModel>(new SessionMediaModel(this));

		// check to ensure that the session has a tier view
		if(session.getTierView() == null || session.getTierView().size() == 0) {
			session.setTierView(SessionFactory.newFactory().createDefaultTierView(session));
		}

		// setup title
		final String title = generateTitle();
		setTitle(title);

		// add default undo listener
		undoSupport.addUndoableEditListener(undoListener);

		// setup undo support and manager extensions
		putExtension(UndoManager.class, undoManager);
		putExtension(UndoableEditSupport.class, undoSupport);

		// setup syllabification info
		final SyllabifierInfo info = new SyllabifierInfo(session);
		final Language defaultSyllabifierLanguage = SyllabifierLibrary.getInstance().defaultSyllabifierLanguage();
		if(info.getSyllabifierLanguageForTier(SystemTierType.IPATarget.getName()) == null)
			info.setSyllabifierLanguageForTier(SystemTierType.IPATarget.getName(), defaultSyllabifierLanguage);
		if(info.getSyllabifierLanguageForTier(SystemTierType.IPAActual.getName()) == null)
			info.setSyllabifierLanguageForTier(SystemTierType.IPAActual.getName(), defaultSyllabifierLanguage);
		session.putExtension(SyllabifierInfo.class, info);

		init();

//		PrefHelper.getUserPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
//
//			@Override
//			public void preferenceChange(PreferenceChangeEvent evt) {
//				if(evt.getKey().equals(FontPreferences.TIER_FONT)) {
//					final EditorEvent ee = new EditorEvent(EditorEventType.RECORD_REFRESH_EVT, this);
//					getEventManager().queueEvent(ee);
//				}
//			}
//		});

		final JMenuBar menuBar = MenuManager.createWindowMenuBar(this);
		setJMenuBar(menuBar);
	}

	@Override
	public String getTitle() {
		return generateTitle();
	}

	@RunOnEDT
	private void _dispose() {
		setVisible(false);
		CommonModuleFrame.getOpenWindows().remove(this);
		setJMenuBar(null);
		getEventManager().shutdown();
		getViewModel().cleanup();
		
		undoManager.discardAllEdits();
		undoSupport.removeUndoableEditListener(undoListener);

		eventManagerRef.set(null);
		viewModelRef.set(null);
		selectionModelRef.set(null);
		dataModelRef.set(null);

		System.gc();

		super.dispose();
	}

	@Override
	public void dispose() {
		getEventManager().registerActionForEvent(EditorEventType.EDITOR_CLOSING, new EditorAction() {

			@Override
			public void eventOccured(EditorEvent ee) {
				SwingUtilities.invokeLater( () -> { _dispose(); } );
			}

		});
		// send out closing event
		final EditorEvent ee = new EditorEvent(EditorEventType.EDITOR_CLOSING, this);
		getEventManager().queueEvent(ee);
	}

	private void init() {
		final BorderLayout layout = new BorderLayout();
		final Container contentPane = getContentPane();
		contentPane.setLayout(layout);

		// window has 3 main elements: toolbar, dock area, status bar
		// each element is retrieved using the view model
		final EditorViewModel viewModel = getViewModel();

		// toolbar
		final SessionEditorToolbar tb = getToolbar();
		add(tb, BorderLayout.NORTH);

		// status bar
		final JXStatusBar sb = (JXStatusBar) getStatusBar();
		add(sb, BorderLayout.SOUTH);

		// setup content/dock area
		final Container dock = viewModel.getRoot();
		contentPane.add(dock, BorderLayout.CENTER);

		setupEditorActions();
	}

	private void setupEditorActions() {
		final EditorAction modifiedChangedAct =
				new DelegateEditorAction(this, "onModifiedChanged");
		getEventManager().registerActionForEvent(EditorEventType.MODIFIED_FLAG_CHANGED, modifiedChangedAct);

		final EditorAction recordAddedAct =
				new DelegateEditorAction(this, "onRecordAdded");
		getEventManager().registerActionForEvent(EditorEventType.RECORD_ADDED_EVT, recordAddedAct);

		final EditorAction recordDeletedAct =
				new DelegateEditorAction(this, "onRecordDeleted");
		getEventManager().registerActionForEvent(EditorEventType.RECORD_DELETED_EVT, recordDeletedAct);
	}

	/**
	 * Get the editor toolbar.
	 *
	 * @return toolbar
	 */
	public SessionEditorToolbar getToolbar() {
		if(this.toolbar == null) {
			this.toolbar = new SessionEditorToolbar(this);
		}
		return this.toolbar;
	}

	/**
	 * Get the editor status bar
	 *
	 * @return statusBar
	 */
	public SessionEditorStatusBar getStatusBar() {
		if(this.statusBar == null) {
			this.statusBar = new SessionEditorStatusBar(this);
		}
		return this.statusBar;
	}

	/*---- Menu Setup ------------------------------*/
	/**
	 * Setup editor window menu
	 *
	 * @param menu
	 */
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		if(menuBar != null)
			setupMenu(menuBar);
		super.setJMenuBar(menuBar);
	}
	
	public void setupMenu(JMenuBar menuBar) {
		// get 'File' menu reference
		final JMenu fileMenu = menuBar.getMenu(0);
		final SaveSessionAction saveAct = new SaveSessionAction(this);
		final JMenuItem saveItem = new JMenuItem(saveAct);
		fileMenu.add(saveItem, 0);
		fileMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				saveItem.setEnabled(hasUnsavedChanges());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

		});

		// save as.. menu
		final JMenu saveAsMenu = new JMenu("Save as...");
		final SessionOutputFactory factory = new SessionOutputFactory();
		for(SessionIO sessionIO:factory.availableSessionIOs()) {
			saveAsMenu.add(new JMenuItem(new SaveAsAction(this, sessionIO)));
		}
		fileMenu.add(saveAsMenu, 1);
		fileMenu.add(new JSeparator(), 2);
		
		fileMenu.add(new JMenuItem(new ExportAsHTMLAction(this)), 3);

		// setup 'Session' menu
		final JMenu sessionMenu = new JMenu("Session");
		sessionMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				sessionMenu.removeAll();
								
				sessionMenu.add(new NewRecordAction(SessionEditor.this));
				sessionMenu.add(new DuplicateRecordAction(SessionEditor.this));
				sessionMenu.add(new DeleteRecordAction(SessionEditor.this));
				sessionMenu.addSeparator();

				sessionMenu.add(new MoveRecordToBeginningAction(SessionEditor.this));
				sessionMenu.add(new MoveRecordBackwardAction(SessionEditor.this));
				sessionMenu.add(new MoveRecordForwardAction(SessionEditor.this));
				sessionMenu.add(new MoveRecordToEndAction(SessionEditor.this));
				sessionMenu.add(new SortRecordsAction(SessionEditor.this));
				sessionMenu.addSeparator();

				sessionMenu.add(new CutRecordAction(SessionEditor.this));
				sessionMenu.add(new CopyRecordAction(SessionEditor.this));
				sessionMenu.add(new PasteRecordAction(SessionEditor.this));
				sessionMenu.addSeparator();

				sessionMenu.add(new FirstRecordAction(SessionEditor.this));
				sessionMenu.add(new PreviousRecordAction(SessionEditor.this));
				sessionMenu.add(new NextRecordAction(SessionEditor.this));
				sessionMenu.add(new LastRecordAction(SessionEditor.this));
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});

		// setup 'View' menu, this menu must be created dynamically
		// as the view model is not available when the menu bar is
		// setup
		final JMenu viewMenu = new JMenu("View");
		final MenuListener viewMenuListener = new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				viewMenu.removeAll();
				getViewModel().setupPerspectiveMenu(viewMenu);
				viewMenu.addSeparator();
				getViewModel().setupViewMenu(viewMenu);
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		};
		viewMenu.addMenuListener(viewMenuListener);

		if(getViewModel() != null) {
			final MenuEvent me = new MenuEvent(viewMenu);
			viewMenuListener.menuSelected(me);
		}
		
		final JMenu mediaMenu = new JMenu("Media");
		final MenuListener mediaMenuListener = new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				mediaMenu.removeAll();

				SessionMediaModel mediaModel = getMediaModel();
				mediaMenu.add(new AssignMediaAction(SessionEditor.this));
				mediaMenu.add(new UnassignMediaAction(SessionEditor.this)).setEnabled(mediaModel.isSessionMediaAvailable());
				JMenuItem genAudioItem = new JMenuItem(mediaModel.getGenerateSessionAudioAction());
				genAudioItem.setEnabled(mediaModel.isSessionMediaAvailable());
				mediaMenu.add(genAudioItem);
				mediaMenu.addSeparator();
				
				boolean enabled = (mediaModel.isSessionAudioAvailable() || 
						(mediaModel.isSessionMediaAvailable() && getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)));
				mediaMenu.add(new PlaySegmentAction(SessionEditor.this)).setEnabled(enabled);
				mediaMenu.add(new PlayCustomSegmentAction(SessionEditor.this)).setEnabled(enabled);
				mediaMenu.add(new PlaySpeechTurnAction(SessionEditor.this)).setEnabled(enabled);
				mediaMenu.add(new PlayAdjacencySequenceAction(SessionEditor.this)).setEnabled(enabled);
				mediaMenu.addSeparator();
				
				mediaMenu.add(new ExportSegmentAction(SessionEditor.this)).setEnabled(mediaModel.isSessionAudioAvailable());
				mediaMenu.add(new ExportCustomSegmentAction(SessionEditor.this)).setEnabled(mediaModel.isSessionAudioAvailable());
				mediaMenu.add(new ExportSpeechTurnAction(SessionEditor.this)).setEnabled(mediaModel.isSessionAudioAvailable());
				mediaMenu.add(new ExportAdjacencySequenceAction(SessionEditor.this)).setEnabled(mediaModel.isSessionAudioAvailable());
				mediaMenu.addSeparator();
				
				final StockIcon prefIcon =
						OSInfo.isMacOs() ? MacOSStockIcon.ToolbarCustomizeIcon
								: OSInfo.isWindows() ?  WindowsStockIcon.APPLICATION : null;
				final String defIcn = "categories/preferences";
				ImageIcon prefsIcn = IconManager.getInstance().getSystemStockIcon(prefIcon, defIcn, IconSize.SMALL);
				final PreferencesCommand prefsAct = new PreferencesCommand("Media");
				prefsAct.putValue(PhonUIAction.NAME, "Edit media folders...");
				prefsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Modify global media folders...");
				prefsAct.putValue(PhonUIAction.SMALL_ICON, prefsIcn);
				mediaMenu.add(prefsAct);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			
		};
		mediaMenu.addMenuListener(mediaMenuListener);
		
		menuBar.add(mediaMenu, 3);
		menuBar.add(viewMenu, 3);
		menuBar.add(sessionMenu, 3);
	}

	/**
	 * Retrieve the data model
	 *
	 * @return the editor data model
	 */
	public EditorDataModel getDataModel() {
		return dataModelRef.get();
	}

	/**
	 * Retrieve the view model
	 *
	 * @return the editor view model
	 */
	public EditorViewModel getViewModel() {
		return (viewModelRef != null ? viewModelRef.get() : null);
	}

	/**
	 * Get the event manager
	 *
	 * @return the editor event model
	 */
	public EditorEventManager getEventManager() {
		return (eventManagerRef != null ? eventManagerRef.get() : null);
	}

	/**
	 * Get the selection model
	 *
	 *
	 * @return editor selection model
	 */
	public EditorSelectionModel getSelectionModel() {
		return (selectionModelRef != null ? selectionModelRef.get() : null);
	}

	/**
	 * Get the media model
	 * 
	 * @return session media model
	 */
	public SessionMediaModel getMediaModel() {
		return (mediaModelRef != null ? mediaModelRef.get() : null);
	}
	
	/**
	 * Get session
	 *
	 * @return session
	 */
	public Session getSession() {
		final EditorDataModel dataModel = getDataModel();
		return (dataModel != null ? dataModel.getSession() : null);
	}

	/*
	 * RECORD POSITION
	 */
	/**
	 * Return the index of the current record.
	 *
	 * @return index of current record
	 */
	public int getCurrentRecordIndex() {
		return this.currentRecord;
	}

	/**
	 * Set the index of the current record.
	 *
	 * @return index
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void setCurrentRecordIndex(int index) {
		if(getDataModel().getRecordCount() > 0 && (index < 0 || index >= getDataModel().getRecordCount())) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		this.currentRecord = index;
		final EditorEvent ee = new EditorEvent(EditorEventType.RECORD_CHANGED_EVT, this, currentRecord());
		getEventManager().queueEvent(ee);
	}

	/**
	 * Return the current record
	 *
	 * @return current record
	 */
	public Record currentRecord() {
		final EditorDataModel dataModel = getDataModel();
		if(dataModel == null) return null;
		return (getCurrentRecordIndex() >= 0 && getCurrentRecordIndex() < dataModel.getRecordCount()
				? dataModel.getRecord(getCurrentRecordIndex())
				: null);
	}

	/**
	 * Get undo support for the editor
	 *
	 * @return undo support
	 */
	public SessionEditorUndoSupport getUndoSupport() {
		return this.undoSupport;
	}

	/**
	 * Get undo manager for the editor
	 *
	 * @return undo manager
	 */
	public UndoManager getUndoManager() {
		return this.undoManager;
	}

	/**
	 * Set the modified flag
	 *
	 * @param modified
	 */
	public void setModified(boolean modified) {
		final boolean lastVal = super.hasUnsavedChanges();
		super.setModified(modified);

		if(lastVal != modified) {
			final EditorEvent ee = new EditorEvent(EditorEventType.MODIFIED_FLAG_CHANGED, this);
			getEventManager().queueEvent(ee);

		}
	}

	/**
	 * Generate the window title
	 *
	 * @return window title
	 */
	private String generateTitle() {
		final Session session = getSession();
		String retVal = WINDOW_NAME;
		if(session != null) {
			retVal += " : " + session.getCorpus() + "." + session.getName();
			if(hasUnsavedChanges())
				retVal += "*";
		}
		return retVal;
	}

	/*
	 * Editor actions
	 */
	@RunOnEDT
	public void onModifiedChanged(EditorEvent eee) {
		final String title = generateTitle();
		setTitle(title);
	}

	@RunOnEDT
	public void onRecordAdded(EditorEvent ee) {
		if(ee.getEventData() != null && ee.getEventData() instanceof Record) {
			final Record r = (Record)ee.getEventData();
			final int recordIndex = getSession().getRecordPosition(r);
			setCurrentRecordIndex(recordIndex);
		}
	}

	@RunOnEDT
	public void onRecordDeleted(EditorEvent ee) {
		if(getDataModel().getRecordCount() > 0 && getCurrentRecordIndex() >= getDataModel().getRecordCount()) {
			setCurrentRecordIndex(getDataModel().getRecordCount()-1);
		} else if(getDataModel().getRecordCount() == 0) {
			setCurrentRecordIndex(-1);
		} else {
			final EditorEvent refreshAct = new EditorEvent(EditorEventType.RECORD_REFRESH_EVT, this);
			getEventManager().queueEvent(refreshAct);
		}
	}

	@Override
	public boolean saveData()
			throws IOException {
		final Project project = getProject();
		final Session session = getSession();

		/*
		 * Check for an OriginalFormat extension, if found and not in Phon format
		 * ask if the users wishes to save in the original format or Phon's format.
		 */
		final SessionOutputFactory outputFactory = new SessionOutputFactory();

		// get default session writer
		SessionWriter sessionWriter = outputFactory.createWriter();
		OriginalFormat origFormat = session.getExtension(OriginalFormat.class);

		// check for non-native format
		if(origFormat != null && !origFormat.getSessionIO().group().equals("ca.phon")) {

			// only issue the format warning once...
			if(origFormat.isIssueWarning()) {
				final MessageDialogProperties props = new MessageDialogProperties();
				final String[] opts = {
						"Use original format (" + origFormat.getSessionIO().name() + ")",
						"Use phon format",
						"Cancel"
				};
				props.setOptions(opts);
				props.setDefaultOption(opts[0]);
				props.setHeader("Save session");
				props.setMessage("Use original format or save in Phon format? Some information such as tier font and ordering may not be saved if using the original format.");
				props.setRunAsync(false);
				props.setParentWindow(this);
				props.setTitle(props.getHeader());

				int retVal = NativeDialogs.showMessageDialog(props);
				if(retVal == 0) {
					// save in original format
					sessionWriter = outputFactory.createWriter(origFormat.getSessionIO());
				} else if(retVal == 1) {
					// change original format to new Phon's default SessionIO
					origFormat = new OriginalFormat(sessionWriter.getClass().getAnnotation(SessionIO.class));
					session.putExtension(OriginalFormat.class, origFormat);
				} else {
					// cancelled
					return false;
				}
				origFormat.setIssueWarning(false);
			} else {
				sessionWriter = outputFactory.createWriter(origFormat.getSessionIO());
			}
		}

		UUID writeLock = null;
		try {
			LOGGER.info("Saving " + session.getCorpus() + "." + session.getName() + "...");
			writeLock = project.getSessionWriteLock(session);
			project.saveSession(session.getCorpus(), session.getName(), session, sessionWriter, writeLock);

			final long byteSize = project.getSessionByteSize(session);

			final String msg = "Save finished.  " +
					ByteSize.humanReadableByteCount(byteSize, true) + " written to disk.";
			LOGGER.info(msg);
			
			final SerializationWarnings warnings = session.getExtension(SerializationWarnings.class);
			ToastFactory.makeToast(msg).start(getToolbar());
			setModified(false);
			if(warnings != null && warnings.size() > 0) {
				warnings.clear();
				
				// show message
				int retVal = showMessageDialog("Save", "Session saved with errors, see log for details.", MessageDialogProperties.okCancelOptions);
				if(retVal == 1) return false;
			}

			// show a short messgae next to the save button to indicate save completed
			return true;
		} catch (IOException e) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setRunAsync(false);
			props.setTitle("Save failed");
			props.setMessage(e.getLocalizedMessage());
			props.setHeader("Unable to save session");
			props.setOptions(MessageDialogProperties.okOptions);
			NativeDialogs.showMessageDialog(props);

			throw e;
		} finally {
			if(writeLock != null)
				project.releaseSessionWriteLock(session, writeLock);
		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {}

}
