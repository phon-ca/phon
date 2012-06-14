package ca.phon.syllabifier.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.PropertiesUtil;
import net.infonode.docking.util.ViewMap;

import ca.gedge.opgraph.ProcessingContext;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditor;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.commands.core.SaveCommand;
import ca.gedge.opgraph.app.components.ConsolePanel;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.components.PathAddressableMenuImpl;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllabifier.editor.commands.syllabifier.SyllabifierMenuProvider;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.ipa.SyllabificationDisplay;

/**
 * 
 */
public class SyllabifierEditor extends JFrame {
	
	private final Logger LOGGER = 
			Logger.getLogger(SyllabifierEditor.class.getName());
	
	/**
	 * Graph editor
	 */
	private final GraphEditor graphEditor = new GraphEditor();
	
	/**
	 * Menu
	 */
	private final JMenuBar menuBar = new JMenuBar();
	private final PathAddressableMenu menu = new PathAddressableMenuImpl(menuBar);
	
	private final SyllabificationDisplay display = new SyllabificationDisplay();
	
	private final JTextField ipaField = new JTextField();
	
	/**
	 * Root docking window
	 */
	private RootWindow rootWindow;
	
	// internal view ids
	private final int ID_START = 100;
	private final int CANVAS_VIEW_ID = ID_START + 1;
	private final int LIBRARY_VIEW_ID = ID_START + 2;
	private final int INSPECTOR_VIEW_ID = ID_START + 3;
	private final int CONSOLE_VIEW_ID = ID_START + 4;
	
	public SyllabifierEditor() {
		super("Syllabifier Editor");
		setupDockingWindow();

		graphEditor.getModel().getConsolePanel().addMouseListener(new ContextPanelContextListener());
		
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
					dispose();
					System.exit(0);
				}
			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {
				
			}
		});
	}
	
	/**
	 * Setup views
	 */
	private void setupDockingWindow() {
		final ViewMap viewMap = new ViewMap();
		
		final JPanel canvasPanel = 
				new JPanel(new BorderLayout());
		canvasPanel.add(graphEditor.getModel().getBreadcrumb(), BorderLayout.NORTH);
		canvasPanel.add(new JScrollPane(graphEditor.getModel().getCanvas()), BorderLayout.CENTER);
		final View canvasView = 
				new View("Syllabifier", null, canvasPanel);
		viewMap.addView(CANVAS_VIEW_ID, canvasView);
		
		final View libraryView = 
				new View("Library", null, graphEditor.getModel().getNodeLibrary());
		viewMap.addView(LIBRARY_VIEW_ID, libraryView);
		
		final View inspectorView = 
				new View("Inspector", null, graphEditor.getModel().getNodeSettings());
		viewMap.addView(INSPECTOR_VIEW_ID, inspectorView);
		
		final ConsolePanel consolePanel = graphEditor.getModel().getConsolePanel();
		consolePanel.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				consolePanel.setCaretPosition(consolePanel.getDocument().getLength());
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		consolePanel.addLogger(Logger.getLogger(""));
		consolePanel.setAutoscrolls(true);
		
		final Font ipaFont = new Font("Charis SIL Compact", Font.PLAIN, 12);
		ipaField.setFont(ipaFont);
		ipaField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				// parse and set ipa
				if(ipaField.getText().length() > 0 ) {
					final IPATranscript t = IPATranscript.parseTranscript(ipaField.getText());
					display.setPhonesForGroup(0, t);
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		JPanel topPanel = new JPanel(new VerticalLayout());
		topPanel.add(new JLabel("Enter IPA:"));
		topPanel.add(ipaField);
		topPanel.add(display);
		JPanel cp = new JPanel(new BorderLayout());
		cp.add(topPanel, BorderLayout.NORTH);
		cp.add(new JScrollPane(graphEditor.getModel().getConsolePanel()), BorderLayout.CENTER);
		
		final View consoleView = 
				new View("Console", null, cp);
		viewMap.addView(CONSOLE_VIEW_ID, consoleView);
		
		rootWindow = DockingUtil.createRootWindow(viewMap, true);
		
		final TabWindow inspectorTab = 
				new TabWindow(new DockingWindow[]{inspectorView, consoleView});
		
		final SplitWindow leftSide = 
				new SplitWindow(false, 0.6f, libraryView, inspectorTab);
		final SplitWindow mainSplit =
				new SplitWindow(true, 0.35f, leftSide, canvasView);
	
		rootWindow.setWindow(mainSplit);
		
		final DockingWindowsTheme theme = 
				new net.infonode.docking.theme.ClassicDockingTheme();
		rootWindow.getRootWindowProperties().addSuperObject(theme.getRootWindowProperties());
		
		final RootWindowProperties titleBarStyleProperties =
				PropertiesUtil.createTitleBarStyleRootWindowProperties();
		// Enable title bar style
		rootWindow.getRootWindowProperties().addSuperObject(
			titleBarStyleProperties);
		
		GraphEditorModel.setActiveEditorModel(graphEditor.getModel());
		// setup menu
		for(MenuProvider menuProvider:graphEditor.getModel().getMenuProviders()) {
			if(!(menuProvider instanceof ca.gedge.opgraph.app.commands.debug.DebugMenuProvider))
				menuProvider.installItems(graphEditor.getModel(), menu);
		}
		SyllabifierMenuProvider provider = new SyllabifierMenuProvider(this);
		provider.installItems(graphEditor.getModel(), menu);
		
		setJMenuBar(menuBar);
		
		add(rootWindow);
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
		graphEditor.getModel().getConsolePanel().setText("");
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
