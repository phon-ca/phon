/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.MouseInputAdapter;

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
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.commands.core.NewCommand;
import ca.gedge.opgraph.app.components.ConsolePanel;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.components.PathAddressableMenuImpl;
import ca.phon.ipa.IPATranscript;
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
	private final SyllabifierGraphEditorModel graphEditor = new SyllabifierGraphEditorModel();
	
	/**
	 * Menu
	 */
	private final JMenuBar menuBar = new JMenuBar();
	private final PathAddressableMenu menu = new PathAddressableMenuImpl(menuBar);
	
	/**
	 * Docking view controls
	 */
	private final CControl dockControl;
	
	/**
	 * Dockable views
	 */
	private enum DockableView {
		CANVAS("Canvas"),
//		CONSOLE("Console"),
		DEBUG("Debug View"),
		INSPECTOR("Node Settings"),
		LIBRARY("Library"),
		SETTINGS("Syllabifier Information"),
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
		
		graphEditor.getDocument().addPropertyChangeListener(GraphDocument.PROCESSING_CONTEXT, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// update ipa
				final Processor processor = graphEditor.getDocument().getProcessingContext();
				if(processor != null) {
					final SyllabificationDisplay display = graphEditor.getSyllabificationDisplay();
					display.setTranscript(new IPATranscript());
					final Object obj = graphEditor.getDocument().getProcessingContext().getContext().get("__ipa__");
					if(obj != null && obj instanceof IPATranscript) {
						final IPATranscript ipa = (IPATranscript)obj;
						display.setTranscript(ipa);
					} else {
						display.setTranscript(new IPATranscript());
					}
				}
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
//			if(!(menuProvider instanceof ca.gedge.opgraph.app.commands.debug.DebugMenuProvider))
				menuProvider.installItems(graphEditor, menu);
		}
//		SyllabifierMenuProvider provider = new SyllabifierMenuProvider(this);
//		provider.installItems(graphEditor, menu);
		
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
//		DockController controller = new DockController();
//        controller.setRootWindow( this );
//        
//        SplitDockStation station = new SplitDockStation();
//        controller.add( station );
//        add( station );
//        
//
//        SplitDockGrid grid = new SplitDockGrid();
//        
//        grid.addDockable(0.0, 0.0, 800.0, 600.0, dockables.get(DockableView.CANVAS));
        
        final Map<DockableView, DockingViewPerspective> dockables = collectDockingViewPerspectives();
		final CControlPerspective perspectives = dockControl.getPerspectives();
		final CPerspective defaultPerspective = perspectives.createEmptyPerspective();
		
		defaultPerspective.getContentArea().getWest().add(
				dockables.get(DockableView.LIBRARY));
		defaultPerspective.getContentArea().getWest().add(
				dockables.get(DockableView.SETTINGS));
		
		final CGridPerspective center = defaultPerspective.getContentArea().getCenter();
		final CWorkingPerspective workPerspective = (CWorkingPerspective)defaultPerspective.getStation("work");
		workPerspective.gridAdd( 0, 0, 600, 600, dockables.get(DockableView.CANVAS));
		workPerspective.gridAdd( 600, 0, 200, 200, dockables.get(DockableView.INSPECTOR));
		workPerspective.gridAdd( 600, 200, 200, 200, dockables.get(DockableView.DEFAULTS));
		workPerspective.gridAdd( 600, 400, 200, 200, dockables.get(DockableView.DEBUG));
		
		center.gridAdd(0, 0, 600, 800, workPerspective);
		
		defaultPerspective.storeLocations();
		defaultPerspective.shrink();
		perspectives.setPerspective(defaultPerspective, true);
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
	
//	public IPATranscript getIPA() {
//		IPATranscript ipa = null;
//		if(display.getNumberOfGroups() == 0)
//			ipa = new IPATranscript();
//		else
//			ipa = display.getPhonesForGroup(0);
//		return ipa;
//	}
//	
//	public SyllabificationDisplay getIPADisplay() {
//		return this.display;
//	}
	
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
					
				case DEBUG:
					comp = graphEditor.getDebugInfoPanel();
					break;
					
				case INSPECTOR:
					comp = graphEditor.getNodeSettings();
					break;
					
				case LIBRARY:
					comp = graphEditor.getNodeLibrary();
					break;
					
				case SETTINGS:
					comp = graphEditor.getSettingsPanel();
					break;
					
				case DEFAULTS:
					comp = graphEditor.getNodeDefaults();
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
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
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
				final NewCommand newCommand = new NewCommand();
				newCommand.actionPerformed(null);
				editor.setDefaultCloseOperation(SyllabifierEditor.DO_NOTHING_ON_CLOSE);
				editor.setSize(new Dimension(1024, 768));
				editor.setVisible(true); 
			}
		});
	}
}
