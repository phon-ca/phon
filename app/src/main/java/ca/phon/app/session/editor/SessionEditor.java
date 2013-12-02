package ca.phon.app.session.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.project.ProjectFrame;
import ca.phon.app.session.editor.undo.SessionEditorUndoSupport;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
	
	/*---- Menu Setup ------------------------------*/
	/**
	 * Setup editor window menu
	 * 
	 * @param menu
	 */
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		// setup 'View' menu, this menu must be created dynamically
		// as the view model is not available when the menu bar is
		// setup
		final JMenu viewMenu = new JMenu("View");
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
				final PhonUIAction toggleViewAct = new PhonUIAction(view, this, "onToggleView", view);
				final EditorView ev = getViewModel().getView(view);
				if(ev != null) {
					toggleViewAct.putValue(PhonUIAction.SMALL_ICON, ev.getIcon());
				}
				final JCheckBoxMenuItem viewItem = new JCheckBoxMenuItem(toggleViewAct);
				
				if(getViewModel().isShowing(view)) {
					viewItem.setSelected(true);
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
			
			final PhonUIAction showPerspectiveAct = new PhonUIAction(this, "applyPerspective", editorPerspective);
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
