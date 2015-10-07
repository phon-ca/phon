package ca.phon.opgraph.editor;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.SingleCDockableFactory;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.perspective.CControlPerspective;
import bibliothek.gui.dock.common.perspective.CGridPerspective;
import bibliothek.gui.dock.common.perspective.CPerspective;
import bibliothek.gui.dock.common.perspective.CWorkingPerspective;
import bibliothek.gui.dock.common.perspective.SingleCDockablePerspective;
import bibliothek.util.Filter;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.canvas.GridLayer;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.opgraph.editor.actions.file.NewAction;
import ca.phon.opgraph.editor.actions.file.OpenAction;
import ca.phon.opgraph.editor.actions.file.SaveAction;
import ca.phon.opgraph.editor.actions.file.SaveAsAction;
import ca.phon.opgraph.editor.actions.graph.AutoLayoutAction;
import ca.phon.opgraph.editor.actions.graph.DeleteAction;
import ca.phon.opgraph.editor.actions.graph.DuplicateAction;
import ca.phon.opgraph.editor.actions.graph.MoveNodeAction;
import ca.phon.opgraph.editor.actions.view.ResetViewAction;
import ca.phon.opgraph.editor.actions.view.ToggleViewAction;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.menu.MenuManager;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;

/**
 * Generic opgragh editor.
 * 
 * @author Greg
 *
 */
public class OpgraphEditor extends CommonModuleFrame {
	
	private final static String WINDOW_TITLE = "Node Editor";

	private static final long serialVersionUID = 311253647756696496L;
	
	/**
	 * Docking view controller
	 */
	private CControl dockControl;
	
	private OpgraphEditorModel model;
	
	private JMenuBar menuBar;

	public OpgraphEditor() {
		this(new DefaultOpgraphEditorModel());
	}
	
	public OpgraphEditor(OpgraphEditorModel model) {
		super();
		setModel(model);
		
		// rebuild menu so edit undo/redo commands work properly
		final JMenuBar menuBar = MenuManager.createWindowMenuBar(this);
		setJMenuBar(menuBar);
		
		initDockingView();
		addWindowFocusListener(focusListener);
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		this.menuBar = menuBar;
	}
	
	@Override
	public boolean hasUnsavedChanges() {
		return getModel().getDocument().hasModifications();
	}

	public OpgraphEditorModel getModel() {
		return this.model;
	}
	
	public void setModel(OpgraphEditorModel model) {
		if(this.model != null)
			this.model.getDocument().getUndoSupport().removeUndoableEditListener(undoListener);
		this.model = model;
		// set undo manager for edit menu commands
		putExtension(UndoManager.class, this.model.getDocument().getUndoManager());
		this.model.getDocument().getUndoSupport().addUndoableEditListener(undoListener);
		if(dockControl != null)
			resetView();
		else
			updateTitle();
	}
	
	public void resetView() {
		for(String view:model.getAvailableViewNames()) {
			dockControl.removeSingleDockable(view);
		}
		setupDefaultPerspective();
		updateTitle();
	}
	
	public File getCurrentFile() {
		return getModel().getDocument().getSource();
	}
	
	public void setCurrentFile(File source) {
		getModel().getDocument().setSource(source);
		updateTitle();
	}
	
	public boolean isViewVisible(String viewName) {
		return true;
	}
	
	public void showView(String viewName) {
		
	}
	
	public void hideView(String viewName) {
		
	}
	
	protected void updateTitle() {
		final StringBuffer sb = new StringBuffer();
		sb.append(WINDOW_TITLE);
		sb.append(" : ");
		if(getCurrentFile() != null)
			sb.append(getCurrentFile().getAbsolutePath());
		else
			sb.append("Untitled");
		if(hasUnsavedChanges()) {
			sb.append("*");
		}
		super.getRootPane().putClientProperty("Window.documentModified", hasUnsavedChanges());
		setWindowName(sb.toString());
	}
	
	public boolean chooseFile() {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(this);
		props.setCanCreateDirectories(true);
		props.setFileFilter(new OpgraphFileFilter());
		props.setRunAsync(false);
		props.setTitle("Save graph");
		
		final String saveAs = NativeDialogs.showSaveDialog(props);
		if(saveAs != null) {
			setCurrentFile(new File(saveAs));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean saveData() throws IOException {
		if(!hasUnsavedChanges()) return true;
		if(getCurrentFile() == null) {
			if(!chooseFile()) return false;
		}
		OpgraphIO.write(getModel().getDocument().getGraph(), getCurrentFile());
		getModel().getDocument().markAsUnmodified();
		updateTitle();
		return true;
	}

	protected void initDockingView() {
		setLayout(new BorderLayout());
		dockControl = new CControl(this);
		
		dockControl.addSingleDockableFactory(new DockableViewFilter(), 
				new DockableViewFactory());
		dockControl.createWorkingArea("work");
		
		add(dockControl.getContentArea(), BorderLayout.CENTER);
			
		setupDefaultPerspective();
		setupMenu();
	}
	
	protected void setupMenu() {
		final MenuBuilder menuBuilder = new MenuBuilder(this.menuBar);
	
		menuBuilder.addMenuItem("File@^", new NewAction(this));
		menuBuilder.addMenuItem("File@New...", new OpenAction(this));
		menuBuilder.addSeparator("File@Open...", "sep1");
		menuBuilder.addMenuItem("File@sep1", new SaveAction(this));
		menuBuilder.addMenuItem("File@Save", new SaveAsAction(this));
		menuBuilder.addSeparator("File@Save as...", "sep2");
		
		menuBuilder.addMenu(".@Edit", "Graph");
		menuBuilder.addMenuItem("Graph", new DeleteAction(this));
		menuBuilder.addMenuItem("Graph", new DuplicateAction(this));
		menuBuilder.addSeparator("Graph", "sep1");
		menuBuilder.addMenuItem("Graph", new AutoLayoutAction(this));
		menuBuilder.addMenuItem("Graph", new MoveNodeAction(this, 0, GridLayer.DEFAULT_GRID_SPACING / 2));
		menuBuilder.addMenuItem("Graph", new MoveNodeAction(this, 0, -GridLayer.DEFAULT_GRID_SPACING / 2));
		menuBuilder.addMenuItem("Graph", new MoveNodeAction(this, GridLayer.DEFAULT_GRID_SPACING/2, 0));
		menuBuilder.addMenuItem("Graph", new MoveNodeAction(this, -GridLayer.DEFAULT_GRID_SPACING / 2, 0));
		
		final JMenu viewMenu = menuBuilder.addMenu(".@Graph", "View");
		viewMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				viewMenu.removeAll();
				
				viewMenu.add(new ResetViewAction(OpgraphEditor.this));
				viewMenu.addSeparator();
				
				for(String viewName:getModel().getAvailableViewNames()) {
					final ToggleViewAction viewAct = new ToggleViewAction(OpgraphEditor.this, viewName);
					viewAct.putValue(ToggleViewAction.SELECTED_KEY, isViewVisible(viewName));
					viewMenu.add(new JCheckBoxMenuItem(viewAct));
				}
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
	}
	
	protected void setupDefaultPerspective() {
		final CControlPerspective perspectives = dockControl.getPerspectives();
		final CPerspective defaultPerspective = perspectives.createEmptyPerspective();
		
		final CWorkingPerspective workPerspective = (CWorkingPerspective)defaultPerspective.getStation("work");
		workPerspective.gridAdd( 0, 0, 200, 200, new SingleCDockablePerspective("Library") );
		workPerspective.gridAdd( 0, 200, 200, 200, new SingleCDockablePerspective("Console") );
		workPerspective.gridAdd( 0, 200, 200, 200, new SingleCDockablePerspective("Debug") );
		workPerspective.gridAdd( 200, 0, 600, 600, new SingleCDockablePerspective("Canvas") );
		workPerspective.gridAdd( 800, 0, 200, 200, new SingleCDockablePerspective("Settings") );
		workPerspective.gridAdd( 800, 200, 200, 200, new SingleCDockablePerspective("Defaults") );
		
		final CGridPerspective center = defaultPerspective.getContentArea().getCenter();
		center.gridAdd( 0, 0, 600, 800, workPerspective );
		
		defaultPerspective.storeLocations();
		defaultPerspective.shrink();
		perspectives.setPerspective(defaultPerspective, true);
	}
	
	private final WindowFocusListener focusListener = new WindowFocusListener() {
		
		@Override
		public void windowLostFocus(WindowEvent e) {
			
		}
		
		@Override
		public void windowGainedFocus(WindowEvent e) {
			GraphEditorModel.setActiveEditorModel(model);
		}
	};
	
	private final UndoableEditListener undoListener = (e) -> {
		updateTitle();
	};
	
	private class DockableViewFilter implements Filter<String> {

		@Override
		public boolean includes(String viewName) {
			return (model.getView(viewName) != null);
		}
		
	}
	
	private class DockableViewFactory implements SingleCDockableFactory {

		@Override
		public SingleCDockable createBackup(String viewName) {
			final JComponent view = model.getView(viewName);
			
			final DefaultSingleCDockable retVal = new DefaultSingleCDockable( viewName , view , new CAction[0] );
			retVal.setTitleText(viewName);
			return retVal;
		}
		
	}
	
	public static void main(String[] args) {
		OpgraphEditor editor = new OpgraphEditor();
		editor.pack();
		editor.setVisible(true);
	}
	
}
