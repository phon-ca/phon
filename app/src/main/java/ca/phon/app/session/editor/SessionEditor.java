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
package ca.phon.app.session.editor;

import ca.hedlund.desktopicons.*;
import ca.phon.app.log.LogUtil;
import ca.phon.app.menu.edit.*;
import ca.phon.app.project.ProjectFrame;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.undo.SessionEditorUndoSupport;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.media.VolumeModel;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.io.*;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuManager;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.OSInfo;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import org.apache.logging.log4j.LogManager;
import org.jdesktop.swingx.JXStatusBar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Main UI for the application.  This window provides the interface for
 * creating and modifying Phon {@link Session}s.</p>
 *
 * <p>This window supports plug-ins.  Plug-ins can provide custom EditorViews.</p>
 */
public class SessionEditor extends ProjectFrame implements ClipboardOwner {

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

		final JMenuBar menuBar = MenuManager.createWindowMenuBar(this);
		setJMenuBar(menuBar);
	}

	@Override
	public String getTitle() {
		return generateTitle();
	}

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
		getEventManager().registerActionForEvent(EditorEventType.EditorClosing, (ee) -> SwingUtilities.invokeLater(this::_dispose));
		// send out closing event
		final EditorEvent<Void> ee = new EditorEvent<>(EditorEventType.EditorClosing, this, null);
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

		this.addWindowFocusListener(new SessionEditorModificationListener(this));
	}

	private void setupEditorActions() {
		final EditorAction<Session> sessionChangedAct = this::onSessionChanged;
		getEventManager().registerActionForEvent(EditorEventType.SessionChanged, sessionChangedAct, EditorEventManager.RunOn.AWTEventDispatchThread);

		final EditorAction<Boolean> modifiedChangedAct = this::onModifiedChanged;
		getEventManager().registerActionForEvent(EditorEventType.ModifiedFlagChanged, modifiedChangedAct, EditorEventManager.RunOn.AWTEventDispatchThread);

		final EditorAction<EditorEventType.RecordDeletedData> recordDeletedAct = this::onRecordDeleted;
		getEventManager().registerActionForEvent(EditorEventType.RecordDeleted, recordDeletedAct, EditorEventManager.RunOn.AWTEventDispatchThread);

		final EditorAction<Void> reloadFromDiskAct = this::onReloadSessionFromDisk;
		getEventManager().registerActionForEvent(EditorEventType.EditorReloadFromDisk, reloadFromDiskAct, EditorEventManager.RunOn.AWTEventDispatchThread);

		final EditorAction<Void> onClosingAct = this::onEditorClosing;
		getEventManager().registerActionForEvent(EditorEventType.EditorClosing, onClosingAct, EditorEventManager.RunOn.AWTEventDispatchThread);
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
	 * @param menuBar
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
		
		putExtension(EditMenuModifier.class, (editMenu) -> {
			editMenu.add(new JMenuItem(new FindAndReplaceAction(SessionEditor.this)), 3);
			editMenu.add(new JSeparator(), 4);
		});

		// setup 'Session' menu
		final JMenu sessionMenu = new JMenu("Session");
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

		if(getSession() != null) {
			sessionMenu.addSeparator();
			JMenuItem itrItem = new JMenuItem(new ITRAction(SessionEditor.this));
			itrItem.setEnabled(getDataModel().getTranscriber() == null && getSession().getTranscriberCount() > 1);
			sessionMenu.add(itrItem);
		}

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
				mediaMenu.add(new ShowMediaInfoAction(SessionEditor.this)).setEnabled(mediaModel.isSessionMediaAvailable());;
				mediaMenu.addSeparator();

				JMenu volumeMenu = new JMenu("Volume");
				volumeMenu.add(new JCheckBoxMenuItem(new ToggleMuteAction(SessionEditor.this)));
				volumeMenu.addSeparator();
				for(float level = 0.25f; level <= VolumeModel.MAX_LEVEL; level += 0.25f) {
					volumeMenu.add(new JMenuItem(new AdjustVolumeAction(SessionEditor.this, level)));
				}
				mediaMenu.add(volumeMenu);

				JMenu playbackRateMenu = new JMenu("Playback rate");
				for(float rate = 0.25f; rate <= 2.0f; rate += 0.25f) {
					playbackRateMenu.add(new JCheckBoxMenuItem(new AdjustPlaybackRate(SessionEditor.this, rate)));
				}
				mediaMenu.add(playbackRateMenu);

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
		final EditorEvent<EditorEventType.RecordChangedData> ee = new EditorEvent<>(EditorEventType.RecordChanged, this,
				new EditorEventType.RecordChangedData(this.currentRecord, currentRecord()));
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
			final EditorEvent<Boolean> ee = new EditorEvent(EditorEventType.ModifiedFlagChanged, this, modified);
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
	private void onEditorClosing(EditorEvent<Void> ee) {
		if(getMediaModel().isSessionAudioAvailable()) {
			try {
				getMediaModel().getSharedSessionAudio().close();
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
	}

	private void onSessionChanged(EditorEvent<Session> ee) {
		// reset media model
		this.mediaModelRef.set(new SessionMediaModel(this));

		// clear undo model
		undoManager.discardAllEdits();

		// update toolbar and record index
		if(this.currentRecord >= getDataModel().getRecordCount()) {
			this.currentRecord = getDataModel().getRecordCount()-1;
		}
		if(this.currentRecord < 0) {
			this.currentRecord = 0;
		}
		remove(getToolbar());
		this.toolbar = new SessionEditorToolbar(this);
		add(this.toolbar, BorderLayout.NORTH);

		setTitle(generateTitle());
	}

	private void onModifiedChanged(EditorEvent<Boolean> eee) {
		final String title = generateTitle();
		setTitle(title);
	}

	private void onRecordDeleted(EditorEvent ee) {
		if(getDataModel().getRecordCount() > 0 && getCurrentRecordIndex() >= getDataModel().getRecordCount()) {
			setCurrentRecordIndex(getDataModel().getRecordCount()-1);
		} else if(getDataModel().getRecordCount() == 0) {
			setCurrentRecordIndex(-1);
		} else {
			final EditorEvent<EditorEventType.RecordChangedData> refreshAct = new EditorEvent<>(EditorEventType.RecordRefresh, this,
					new EditorEventType.RecordChangedData(this.currentRecord, currentRecord()));
			getEventManager().queueEvent(refreshAct);
		}
	}

	/**
	 * Reload session data from  disk, this method does not display a warning dialog
	 * This method is called when EditorEventType.SESSION_CHANGED_ON_DISK is fired
	 *
	 * @param ee
	 */
	private void onReloadSessionFromDisk(EditorEvent<Void> ee) {
		final Project project = getProject();
		final Session currentSession = getSession();

		try {
			final Session reloadedSession = project.openSession(currentSession.getCorpus(), currentSession.getName());
			getDataModel().setSession(reloadedSession);
			getEventManager().queueEvent(new EditorEvent<>(EditorEventType.SessionChanged, this, reloadedSession));

			setModified(false);
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
			showMessageDialog("Unable to reload session", e.getLocalizedMessage(), MessageDialogProperties.okOptions);
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

			final EditorEvent<Session> ee = new EditorEvent<>(EditorEventType.SessionSaved, this, session);
			getEventManager().queueEvent(ee);

			// show a short message next to the save button to indicate save completed
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
