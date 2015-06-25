package ca.phon.app.query.analysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.commands.core.SaveCommand;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.components.PathAddressableMenuImpl;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

/**
 * Editor for query chaining and reporting using the opgraph
 * library.  This window acts as an editor for Phon query analysis
 * and reporting.
 *
 */
public class AssessmentEditor extends CommonModuleFrame {

	private static final long serialVersionUID = -2020857319722692172L;

	private final Logger LOGGER = Logger.getLogger(AssessmentEditor.class.getName());
	
	private AssessmentEditorModel model;
	
	private JMenuBar menuBar;
	
	/**
	 * Docking view controller
	 */
	private final CControl dockControl;
	
	/**
	 * Dockable views
	 */
	private enum DockableView {
		CANVAS("Canvas"),
		INSPECTOR("Node Settings"),
		LIBRARY("Library"),
		DEFAULTS("Node Input Defaults");
		
		String title;
		
		private DockableView(String title) {
			this.title = title;
		}
		
		public static DockableView fromString(String title) {
			DockableView retVal = null;
			for(DockableView dv:DockableView.values()) {
				if(dv.title.equalsIgnoreCase(title)) {
					retVal = dv;
					break;
				}
			}
			return retVal;
		}
	}
	
	public AssessmentEditor(Project project) {
		super();
		super.setWindowName("Assessment Editor : Untitled");
		
		putExtension(Project.class, project);
		
		model = new AssessmentEditorModel(project);
		dockControl = new CControl(this);
		setupLayout();
		setupDockingPerspective();
		
		addWindowFocusListener(focusListener);
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		this.menuBar = menuBar;
	}

	@Override
	public boolean saveData() throws IOException {
		final ActionEvent ae = new ActionEvent(this, -1, "save");
		GraphEditorModel.setActiveEditorModel(model);
		final SaveCommand saveCommand = new SaveCommand();
		saveCommand.actionPerformed(ae);
		return true;
	}

	private void setupLayout() {
		setLayout(new BorderLayout());
		
		GraphEditorModel.setActiveEditorModel(model);
		menuBar.removeAll();
		final PathAddressableMenu pmenu = 
				new PathAddressableMenuImpl(this.menuBar);
		for(MenuProvider menuProvider:model.getMenuProviders()) {
			menuProvider.installItems(model, pmenu);
		}
		
		add(dockControl.getContentArea(), BorderLayout.CENTER);
	}
	
	private void setupDockingPerspective() {
		dockControl.addSingleDockableFactory(new DockableViewFilter(), 
				new DockableViewFactory());
		dockControl.createWorkingArea("work");
		
		final Map<DockableView, DockingViewPerspective> dockables =
				collectDockingViewPerspectives();
		final CControlPerspective perspectives = dockControl.getPerspectives();
		final CPerspective defaultPerspective = perspectives.createEmptyPerspective();
		
		defaultPerspective.getContentArea().getWest().add(
				dockables.get(DockableView.LIBRARY));
		
		final CWorkingPerspective workPerspective = (CWorkingPerspective)defaultPerspective.getStation("work");
		workPerspective.gridAdd( 0, 0, 600, 600, dockables.get(DockableView.CANVAS));
		workPerspective.gridAdd( 600, 0, 200, 200, dockables.get(DockableView.INSPECTOR));
		workPerspective.gridAdd( 600, 200, 200, 200, dockables.get(DockableView.DEFAULTS));
		
		final CGridPerspective center = defaultPerspective.getContentArea().getCenter();
		center.gridAdd(0, 0, 600, 800, workPerspective);
		
		defaultPerspective.storeLocations();
		defaultPerspective.shrink();
		perspectives.setPerspective(defaultPerspective, true);
	}
	
	private Map<DockableView, DockingViewPerspective> collectDockingViewPerspectives() {
		 Map<DockableView, DockingViewPerspective> retVal = 
				 new LinkedHashMap<>();
		 for(DockableView dv:DockableView.values()) {
			 final DockingViewPerspective dvp = new DockingViewPerspective(dv);
			 retVal.put(dv, dvp);
		 }
		 return retVal;
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
	
	private class DockableViewFilter implements Filter<String> {

		@Override
		public boolean includes(String arg0) {
			return (DockableView.fromString(arg0) != null);
		}
		
	}
	
	private class DockableViewFactory implements SingleCDockableFactory {

		@Override
		public SingleCDockable createBackup(String arg0) {
			final DockableView dv = DockableView.fromString(arg0);
			if(dv != null) {
				Component comp = null;
				switch(dv) {
				case CANVAS:
					final JPanel cPanel = new JPanel(new BorderLayout());
					final JScrollPane canvasScroller = new JScrollPane(model.getCanvas());
					cPanel.add(model.getBreadcrumb(), BorderLayout.NORTH);
					cPanel.add(canvasScroller, BorderLayout.CENTER);
					comp = cPanel;
					break;
					
				case INSPECTOR:
					comp = model.getNodeSettings();
					break;
					
				case LIBRARY:
					comp = model.getNodeLibrary();
					break;
					
				case DEFAULTS:
					comp = model.getNodeDefaults();
					break;
					
				}
				final DefaultSingleCDockable retVal = new DefaultSingleCDockable( dv.title , comp , new CAction[0] );
				retVal.setTitleText(dv.title);
				return retVal;
			} else {
				return null;
			}
		}
		
	}
	
	private class DockingViewPerspective extends SingleCDockablePerspective {
		
		public DockingViewPerspective(DockableView view) {
			super(view.title);
		}
		
	}
}
