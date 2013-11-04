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
	 * Undo support for the editor
	 */
	private final SessionEditorUndoSupport undoSupport = new SessionEditorUndoSupport();
	
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
		
		// TODO toolbar
		
		// setup content/dock area
		final Container dock = viewModel.getRoot();
		contentPane.add(dock, BorderLayout.CENTER);
		
		viewModel.showView("Session Info");
		viewModel.showView("Media Player");
		viewModel.showView("Waveform");
		// TODO statusbar
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
	
}
