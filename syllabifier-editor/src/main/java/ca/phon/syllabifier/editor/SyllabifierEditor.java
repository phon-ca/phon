package ca.phon.syllabifier.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.MouseInputAdapter;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.SingleCDockableFactory;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.perspective.CControlPerspective;
import bibliothek.gui.dock.common.perspective.CGridPerspective;
import bibliothek.gui.dock.common.perspective.CMinimizePerspective;
import bibliothek.gui.dock.common.perspective.CPerspective;
import bibliothek.gui.dock.common.perspective.CWorkingPerspective;
import bibliothek.gui.dock.common.perspective.SingleCDockablePerspective;
import bibliothek.util.Filter;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.ConsolePanel;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.components.PathAddressableMenuImpl;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllabifier.editor.commands.syllabifier.SyllabifierMenuProvider;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.ipa.SyllabificationDisplay;

/**
 * 
 */
public class SyllabifierEditor extends JFrame {
	
	private static final long serialVersionUID = -5325257904174579337L;

	private final Logger LOGGER = 
			Logger.getLogger(SyllabifierEditor.class.getName());
	
	/**
	 * Graph editor
	 */
	private final GraphEditorModel graphEditor = new GraphEditorModel();
	
	/**
	 * Menu
	 */
	private final JMenuBar menuBar = new JMenuBar();
	private final PathAddressableMenu menu = new PathAddressableMenuImpl(menuBar);
	
	private final SyllabificationDisplay display = new SyllabificationDisplay();
	
	private final JTextField ipaField = new JTextField();
	
	/**
	 * Docking view controls
	 */
	private final CControl dockControl;
	
	/**
	 * Dockable views
	 */
	private enum DockableView {
		CANVAS("Canvas"),
		CONSOLE("Console"),
		DEBUG("Debug View"),
		INSPECTOR("Inspector"),
		LIBRARY("Library");
		
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
	
	public SyllabifierEditor() {
		super("Syllabifier Editor");
		dockControl = new CControl(this);
		setupLayout();
		setupDockingWindow();
		
		graphEditor.getConsolePanel().addMouseListener(new ContextPanelContextListener());
		
		graphEditor.getDocument().addPropertyChangeListener(GraphDocument.UNDO_STATE, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				getRootPane().putClientProperty("Window.documentModified", graphEditor.getDocument().hasModifications());
			}
		});
		
		super.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				boolean canClose = true;
				if(graphEditor.getDocument().hasModifications()) {
					canClose = graphEditor.getDocument().checkForReset();
				}
				if(canClose) {
					dockControl.destroy();
					dispose();
					System.exit(0);
				}
			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {
				
			}
		});
	}
	
	private void setupLayout() {
		setLayout(new BorderLayout());
		
		GraphEditorModel.setActiveEditorModel(graphEditor);
		// setup menu
		for(MenuProvider menuProvider:graphEditor.getMenuProviders()) {
			if(!(menuProvider instanceof ca.gedge.opgraph.app.commands.debug.DebugMenuProvider))
				menuProvider.installItems(graphEditor, menu);
		}
		SyllabifierMenuProvider provider = new SyllabifierMenuProvider(this);
		provider.installItems(graphEditor, menu);
		
		setJMenuBar(menuBar);
		
		add(dockControl.getContentArea(), BorderLayout.CENTER);
	}
	
	/**
	 * Setup views
	 */
	private void setupDockingWindow() {
		// add view factory
		dockControl.addSingleDockableFactory(new DockableViewFilter(), new DockableViewFactory());
		
		dockControl.createWorkingArea("work");
		
		setupDockingPerspectives();
	}
	
	private void setupDockingPerspectives() {
		final CControlPerspective perspectives = dockControl.getPerspectives();
		final CPerspective defaultPerspective = perspectives.createEmptyPerspective();
		
		final Map<DockableView, DockingViewPerspective> dockables = collectDockingViewPerspectives();
		
		// setup default minimized layout
		final CMinimizePerspective defMinWest = defaultPerspective.getContentArea().getWest();
		defMinWest.add( dockables.get(DockableView.LIBRARY) );
		defMinWest.add( dockables.get(DockableView.CONSOLE) );
		
		final CMinimizePerspective defMinEast = defaultPerspective.getContentArea().getEast();
		defMinEast.add( dockables.get(DockableView.INSPECTOR) );
		defMinEast.add( dockables.get(DockableView.DEBUG) );
		
		defaultPerspective.storeLocations();
		
		// setup default normalized layout
		final CGridPerspective defCenter = defaultPerspective.getContentArea().getCenter();
		final CWorkingArea canvasArea = (CWorkingArea)dockControl.getStation("work");
		final CWorkingPerspective canvas = canvasArea.createPerspective();
		
		defCenter.gridAdd(  0,   0,  30,  50, dockables.get(DockableView.LIBRARY) );
		defCenter.gridAdd(  0,  50,  30,  50, dockables.get(DockableView.CONSOLE) );
		defCenter.gridAdd( 30,   0,  70, 100, canvas );
		
		canvas.gridAdd( 0, 0, 100, 100, dockables.get(DockableView.CANVAS));
		defaultPerspective.storeLocations();
		
		// setup initial configuration
		final CMinimizePerspective iniWest = defaultPerspective.getContentArea().getWest();
		iniWest.add(dockables.get(DockableView.LIBRARY));
		iniWest.add(dockables.get(DockableView.CONSOLE));
		
		defaultPerspective.shrink();
		perspectives.setPerspective(defaultPerspective, true);
		
		// TODO debug perspective
		// setup debug normalized layout
		
		// setup debug minimized layout
		
		// setup debug initial configuration
	}
	
	private Map<DockableView, DockingViewPerspective> collectDockingViewPerspectives() {
		final Map<DockableView, DockingViewPerspective> retVal =
				new TreeMap<DockableView, SyllabifierEditor.DockingViewPerspective>();
		
		for(DockableView dv:DockableView.values()) {
			final DockingViewPerspective dvp = new DockingViewPerspective(dv);
			retVal.put(dv, dvp);
		}
		
		return retVal;
	}
	
	public IPATranscript getIPA() {
		return display.getPhonesForGroup(0);
	}
	
	public SyllabificationDisplay getIPADisplay() {
		return this.display;
	}
	
	/**
	 * Console panel context listener
	 */
	public class ContextPanelContextListener extends MouseInputAdapter {
		
		@Override
		public void mousePressed(MouseEvent me) {
			if(me.isPopupTrigger())
				showPopup(me);
		}
		
		@Override
		public void mouseReleased(MouseEvent me) {
			if(me.isPopupTrigger())
				showPopup(me);
		}
		
		private void showPopup(MouseEvent me) {
			final JPopupMenu popupMenu = new JPopupMenu();
			
			final ConsolePanel cp = (ConsolePanel)(me.getSource());
			final PhonUIAction clearAct = new PhonUIAction(cp, "setText", new String());
			clearAct.putValue(PhonUIAction.NAME, "Clear Console");
			clearAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear console text");
			popupMenu.add(clearAct);
			
			popupMenu.show(cp, me.getX(), me.getY());
		}
		
	}
	
	/*
	 * UI Actions
	 */
	public void onClearConsole(PhonActionEvent pae) {
		graphEditor.getConsolePanel().setText("");
	}
	
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
					final JScrollPane canvasScroller = new JScrollPane(graphEditor.getCanvas());
					cPanel.add(graphEditor.getBreadcrumb(), BorderLayout.NORTH);
					cPanel.add(canvasScroller, BorderLayout.CENTER);
					comp = cPanel;
					break;
					
				case CONSOLE:
					comp = graphEditor.getConsolePanel();
					break;
					
				case DEBUG:
					comp = graphEditor.getDebugInfoPanel();
					break;
					
				case INSPECTOR:
					comp = graphEditor.getNodeSettings();
					break;
					
				case LIBRARY:
					comp = graphEditor.getNodeLibrary();
					break;
				}
				final DefaultSingleCDockable retVal = new DefaultSingleCDockable( dv.title , comp , new CAction[0] );
				retVal.setTitleText(dv.title);
//				retVal.setCloseable(false);
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
	
	public static void main(String[] args) throws Exception {
		SwingUtilities.invokeLater(
				new Runnable() { public void run() {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (UnsupportedLookAndFeelException e) {
						e.printStackTrace();
					}
		SyllabifierEditor editor = new SyllabifierEditor();
		editor.setDefaultCloseOperation(SyllabifierEditor.DO_NOTHING_ON_CLOSE);
		editor.setSize(new Dimension(1024, 768));
		editor.setVisible(true); }});
	}
}
