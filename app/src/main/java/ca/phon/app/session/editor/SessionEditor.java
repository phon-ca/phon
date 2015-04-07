package ca.phon.app.session.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;

import ca.phon.app.project.ProjectFrame;
import ca.phon.app.session.editor.actions.CopyRecordAction;
import ca.phon.app.session.editor.actions.CutRecordAction;
import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.actions.DuplicateRecordAction;
import ca.phon.app.session.editor.actions.FirstRecordAction;
import ca.phon.app.session.editor.actions.LastRecordAction;
import ca.phon.app.session.editor.actions.MoveRecordBackwardAction;
import ca.phon.app.session.editor.actions.MoveRecordForwardAction;
import ca.phon.app.session.editor.actions.MoveRecordToBeginningAction;
import ca.phon.app.session.editor.actions.MoveRecordToEndAction;
import ca.phon.app.session.editor.actions.NewRecordAction;
import ca.phon.app.session.editor.actions.NextRecordAction;
import ca.phon.app.session.editor.actions.PasteRecordAction;
import ca.phon.app.session.editor.actions.PreviousRecordAction;
import ca.phon.app.session.editor.actions.SaveSessionAction;
import ca.phon.app.session.editor.actions.SortRecordsAction;
import ca.phon.app.session.editor.undo.SessionEditorUndoSupport;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.SystemTierType;
import ca.phon.session.Transcriber;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MenuManager;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.Language;
import ca.phon.util.PrefHelper;

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
	
	private final static Logger LOGGER = Logger
			.getLogger(SessionEditor.class.getName());
	
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
	 * Index of the current record
	 * 
	 */
	private volatile transient int currentRecord = 0;
	
	/**
	 * Has data been modified
	 */
	private volatile transient boolean modified = false;
	
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
	 * Constructor
	 */
	public SessionEditor(Project project, Session session, Transcriber transcriber) {
		super(project);
		
		this.dataModelRef = 
				new AtomicReference<EditorDataModel>(new DefaultEditorDataModel(session));
		getDataModel().setTranscriber(transcriber);
		this.viewModelRef = 
				new AtomicReference<EditorViewModel>(new DefaultEditorViewModel(this));
		this.eventManagerRef = 
				new AtomicReference<EditorEventManager>(new EditorEventManager(this));
		this.selectionModelRef = 
				new AtomicReference<EditorSelectionModel>(new DefaultEditorSelectionModel());
		
		
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
				saveItem.setEnabled(isModified());
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			
		});
		
		
		// setup 'Session' menu
		final JMenu sessionMenu = new JMenu("Session");
		sessionMenu.add(new NewRecordAction(this));
		sessionMenu.add(new DuplicateRecordAction(this));
		sessionMenu.add(new DeleteRecordAction(this));
		sessionMenu.addSeparator();
		
		sessionMenu.add(new MoveRecordToBeginningAction(this));
		sessionMenu.add(new MoveRecordBackwardAction(this));
		sessionMenu.add(new MoveRecordForwardAction(this));
		sessionMenu.add(new MoveRecordToEndAction(this));
		sessionMenu.add(new SortRecordsAction(this));
		sessionMenu.addSeparator();
		
		sessionMenu.add(new CutRecordAction(this));
		sessionMenu.add(new CopyRecordAction(this));
		sessionMenu.add(new PasteRecordAction(this));
		sessionMenu.addSeparator();
		
		sessionMenu.add(new FirstRecordAction(this));
		sessionMenu.add(new PreviousRecordAction(this));
		sessionMenu.add(new NextRecordAction(this));
		sessionMenu.add(new LastRecordAction(this));
		
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
		if(index < 0 || index >= getDataModel().getRecordCount()) {
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
		return dataModel.getRecord(getCurrentRecordIndex());
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
	 * Has session data been modified
	 * 
	 * @return <code>true</code> if modified flag is set, 
	 *  <code>false</code> otherwise
	 */
	public boolean isModified() {
		return this.modified;
	}
	
	/**
	 * Set the modified flag
	 * 
	 * @param modified
	 */
	public void setModified(boolean modified) {
		final boolean lastVal = this.modified;
		this.modified = modified;
		
		if(lastVal != modified) {
			final EditorEvent ee = new EditorEvent(EditorEventType.MODIFIED_FLAG_CHANGED, this);
			getEventManager().queueEvent(ee);
			
			getRootPane().putClientProperty(SubstanceLookAndFeel.WINDOW_MODIFIED, modified);
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
			if(isModified())
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
		if(getCurrentRecordIndex() >= getDataModel().getRecordCount()) {
			setCurrentRecordIndex(getDataModel().getRecordCount()-1);
		} else {
			final EditorEvent refreshAct = new EditorEvent(EditorEventType.RECORD_REFRESH_EVT, this);
			getEventManager().queueEvent(refreshAct);
		}
	}
	
	@Override
	public boolean hasUnsavedChanges() {
		return this.isModified();
	}
	
	@Override
	public boolean saveData() 
			throws IOException {
		final Project project = getProject();
		final Session session = getSession();
		
		final UUID writeLock = project.getSessionWriteLock(session);
		if(writeLock != null) {
			try {
				project.saveSession(session, writeLock);
			} catch (IOException e) {
				throw e;
			} finally {
				project.releaseSessionWriteLock(session, writeLock);
			}
			
			setModified(false);
			
			return true;
		}
				
		return false;
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {}
	
}
