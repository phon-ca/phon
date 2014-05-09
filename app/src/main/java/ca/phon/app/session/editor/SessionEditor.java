package ca.phon.app.session.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MenuElement;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import ca.phon.app.project.ProjectFrame;
import ca.phon.app.session.editor.actions.CopyRecordAction;
import ca.phon.app.session.editor.actions.CutRecordAction;
import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.actions.FirstRecordAction;
import ca.phon.app.session.editor.actions.LastRecordAction;
import ca.phon.app.session.editor.actions.NewRecordAction;
import ca.phon.app.session.editor.actions.NextRecordAction;
import ca.phon.app.session.editor.actions.PasteRecordAction;
import ca.phon.app.session.editor.actions.PreviousRecordAction;
import ca.phon.app.session.editor.actions.SaveSessionAction;
import ca.phon.app.session.editor.undo.SessionEditorUndoSupport;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.SystemTierType;
import ca.phon.session.Transcriber;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.Language;
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
	private final SessionEditorUndoSupport undoSupport = new SessionEditorUndoSupport();
	
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
	
	private JMenu viewMenu;
	
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
	}
	
	@Override
	public String getTitle() {
		return generateTitle();
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
		
		// don't do this here - wait until window has been initialized
		
		
		setupViewMenu(viewMenu);

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
		// get 'File' menu reference
		final JMenu fileMenu = menuBar.getMenu(0);
		final SaveSessionAction saveAct = new SaveSessionAction(this);
		final JMenuItem saveItem = new JMenuItem(saveAct);
		fileMenu.add(saveItem, 0);
		fileMenu.add(new JSeparator(), 1);
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
		sessionMenu.add(new DeleteRecordAction(this));
		sessionMenu.addSeparator();
		
		sessionMenu.add(new CutRecordAction(this));
		sessionMenu.add(new CopyRecordAction(this));
		sessionMenu.add(new PasteRecordAction(this));
		sessionMenu.addSeparator();
		
		sessionMenu.add(new FirstRecordAction(this));
		sessionMenu.add(new PreviousRecordAction(this));
		sessionMenu.add(new NextRecordAction(this));
		sessionMenu.add(new LastRecordAction(this));
		menuBar.add(sessionMenu, 3);
		
		// setup 'View' menu, this menu must be created dynamically
		// as the view model is not available when the menu bar is
		// setup
		viewMenu = new JMenu("View");
		viewMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				viewMenu.removeAll();
				setupLayoutManagmentMenuItems(viewMenu);
				setupViewMenu(viewMenu);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			
		});
		menuBar.add(viewMenu, 3);
		
		super.setJMenuBar(menuBar);
	}
	
	private void setupLayoutManagmentMenuItems(JMenu viewMenu) {
		final JMenu layoutMenu = new JMenu("Load layout");
		ImageIcon loadLayoutIcon = IconManager.getInstance().getIcon("actions/layout-content", IconSize.SMALL);
		layoutMenu.setIcon(loadLayoutIcon);
		layoutMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				layoutMenu.removeAll();
				setupLayoutMenu(layoutMenu);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		viewMenu.add(layoutMenu);
		
		final JMenu deleteMenu = new JMenu("Delete layout");
		ImageIcon deleteLayoutIcon = IconManager.getInstance().getIcon("actions/layout-delete", IconSize.SMALL);
		deleteMenu.setIcon(deleteLayoutIcon);
		deleteMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent arg0) {
				setupDeleteLayoutMenu(deleteMenu);
			}
			
			@Override
			public void menuDeselected(MenuEvent arg0) {
			}
			
			@Override
			public void menuCanceled(MenuEvent arg0) {
			}
		});
		viewMenu.add(deleteMenu);
		
		// save current layout
		final PhonUIAction saveLayoutAct = new PhonUIAction(SessionEditor.this, "onSaveLayout");
		ImageIcon saveLayoutIcon = IconManager.getInstance().getIcon("actions/layout-add", IconSize.SMALL);
		saveLayoutAct.putValue(PhonUIAction.SMALL_ICON, saveLayoutIcon);
		saveLayoutAct.putValue(PhonUIAction.NAME, "Save current layout...");
		saveLayoutAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save current layout as a preset.");
		final JMenuItem saveLayoutItem = new JMenuItem(saveLayoutAct);
		viewMenu.add(saveLayoutItem);
		
		viewMenu.addSeparator();
	}
	
	/**
	 * Adds all available views - by category - to the given
	 * {@link MenuElement}
	 * 
	 * @param ele
	 */
	private void setupViewMenu(MenuElement ele) {
		final Map<EditorViewCategory, List<String>> viewsByCategory = 
				getViewModel().getViewsByCategory();
		for(EditorViewCategory category:viewsByCategory.keySet()) {
			final JMenuItem categoryItem = new JMenuItem("-- " + category.title + " --");
			categoryItem.setEnabled(false);
			if(ele.getComponent() instanceof JMenu) {
				final JMenu menu = (JMenu)ele;
				menu.add(categoryItem);
			} else if(ele.getComponent() instanceof JPopupMenu) {
				final JPopupMenu menu = (JPopupMenu)ele;
				menu.add(categoryItem);
			}
			
			for(String view:viewsByCategory.get(category)) {
				final PhonUIAction toggleViewAct = new PhonUIAction(view, getViewModel(), "showView", view);
				toggleViewAct.putValue(PhonUIAction.SMALL_ICON, getViewModel().getViewIcon(view));
				
				JComponent viewItem = new JMenuItem(toggleViewAct);
				
				if(getViewModel().isShowing(view)) {
					JMenu menu = getViewModel().getView(view).getMenu();
					if(menu != null) {
						menu.addSeparator();
					} else {
						menu = new JMenu();
					}
					menu.setText(toggleViewAct.getValue(PhonUIAction.NAME).toString());
					menu.setIcon(getViewModel().getViewIcon(view));
					final Action closeAct = getViewModel().getCloseAction(view);
					menu.add(closeAct);
					
					viewItem = menu;
				}
				
				if(ele.getComponent() instanceof JMenu) {
					final JMenu menu = (JMenu)ele;
					menu.add(viewItem);
				} else if(ele.getComponent() instanceof JPopupMenu) {
					final JPopupMenu menu = (JPopupMenu)ele;
					menu.add(viewItem);
				}
			}
		}
	}
	
	/**
	 * Adds a menu item for all available editor perspecitves.
	 * 
	 * @param menu
	 */
	private void setupLayoutMenu(MenuElement menu) {
		if(menu.getComponent() instanceof JMenu) {
			((JMenu)menu).removeAll();
		} else if(menu.getComponent() instanceof JPopupMenu) {
			((JPopupMenu)menu).removeAll();
		}
		
		for(RecordEditorPerspective editorPerspective:RecordEditorPerspective.availablePerspectives()) {
			
			final PhonUIAction showPerspectiveAct = new PhonUIAction(getViewModel(), "applyPerspective", editorPerspective);
			showPerspectiveAct.putValue(PhonUIAction.NAME, editorPerspective.getName());
			showPerspectiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Load perspective: " + editorPerspective.getName());
			final JMenuItem showPerspectiveItem = new JMenuItem(showPerspectiveAct);
			
			
			if(menu.getComponent() instanceof JMenu) {
				final JMenu m = (JMenu)menu;
				m.add(showPerspectiveItem);
			} else if(menu.getComponent() instanceof JPopupMenu) {
				final JPopupMenu m = (JPopupMenu)menu;
				m.add(showPerspectiveItem);
			}
		}
	}
	
	/**
	 * Add a menu to delete user-defined editor perspectives.
	 * 
	 * @param ele
	 */
	private void setupDeleteLayoutMenu(MenuElement ele) {
		if(ele.getComponent() instanceof JMenu) {
			((JMenu)ele).removeAll();
		} else if(ele.getComponent() instanceof JPopupMenu) {
			((JPopupMenu)ele).removeAll();
		}
		for(RecordEditorPerspective editorPerspective:RecordEditorPerspective.availablePerspectives()) {
			try {
				final File perspectiveFile = new File(editorPerspective.getLocation().toURI());
				if(perspectiveFile.canWrite()) {
					// add delete item
					final PhonUIAction delPerspectiveAct = 
							new PhonUIAction(this, "onDeleteLayout", editorPerspective);
					delPerspectiveAct.putValue(PhonUIAction.NAME, editorPerspective.getName());
					delPerspectiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Delete layout " + editorPerspective.getName());
					final JMenuItem delPerspectiveItem = new JMenuItem(delPerspectiveAct);
					
					if(ele.getComponent() instanceof JMenu) {
						final JMenu menu = (JMenu)ele;
						menu.add(delPerspectiveItem);
					} else if(ele.getComponent() instanceof JPopupMenu) {
						final JPopupMenu menu = (JPopupMenu)ele;
						menu.add(delPerspectiveItem);
					}
				}
			} catch (URISyntaxException e) {
				
			} catch (IllegalArgumentException e) {
				// thrown when URI is not heirarchical (i.e., is in a jar)
			}
		}
	}
	
	public void onSaveLayout() {
		// get a perspective name
		final String layoutName = JOptionPane.showInputDialog(this, "Enter layout name:");
		if(RecordEditorPerspective.getPerspective(layoutName) != null) {
			final Toast toast = ToastFactory.makeToast("Layout named " + layoutName + " already exists.");
			toast.start(this.getRootPane());
			return;
		}
		
		final File perspectiveFile = new File(RecordEditorPerspective.PERSPECTIVES_FOLDER, layoutName + ".xml");
		try {
			final RecordEditorPerspective perspective = new RecordEditorPerspective(layoutName, perspectiveFile.toURI().toURL());
			getViewModel().savePerspective(perspective);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}	
	}
	
	public void onDeleteLayout(RecordEditorPerspective perspective) {
		RecordEditorPerspective.deletePerspective(perspective);
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
		return viewModelRef.get();
	}
	
	/**
	 * Get the event manager
	 * 
	 * @return the editor event model
	 */
	public EditorEventManager getEventManager() {
		return eventManagerRef.get();
	}
	
	/**
	 * Get session
	 * 
	 * @return session
	 */
	public Session getSession() {
		final EditorDataModel dataModel = getDataModel();
		return dataModel.getSession();
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
			project.saveSession(session, writeLock);
			project.releaseSessionWriteLock(session, writeLock);
			
			setModified(false);
			
			return true;
		}
				
		return false;
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {}
	
}
