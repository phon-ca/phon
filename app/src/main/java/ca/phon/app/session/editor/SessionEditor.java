package ca.phon.app.session.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicReference;

import ca.phon.app.project.ProjectFrame;
import ca.phon.app.session.editor.undo.SessionEditorUndoSupport;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;

/**
 * <p>Main UI for the application.  This window provides the interface for
 * creating and modifying Phon {@link Session}s.</p>
 * 
 * <p>This window supports plug-ins.  Plug-ins can provide custom EditorViews.</p>
 */
public class SessionEditor extends ProjectFrame {

	private final static long serialVersionUID = 2831713307191769522L;
	
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
	
	/**
	 * Undo support for the editor
	 */
	private final SessionEditorUndoSupport undoSupport = new SessionEditorUndoSupport();
	
	/**
	 * Toolbar
	 */
	private SessionEditorToolbar toolbar;
	
	/**
	 * Constructor
	 */
	public SessionEditor(Project project, Session session) {
		super(project);
		
		this.dataModelRef = 
				new AtomicReference<EditorDataModel>(new DefaultEditorDataModel(session));
		this.viewModelRef = 
				new AtomicReference<EditorViewModel>(new DefaultEditorViewModel(this));
		this.eventManagerRef = 
				new AtomicReference<EditorEventManager>(new EditorEventManager(this));
		init();
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
		
		final RecordEditorPerspective perspective = RecordEditorPerspective.getPerspective("Default");
		viewModel.applyPerspective(perspective);
		
		// TODO statusbar
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
	
}
