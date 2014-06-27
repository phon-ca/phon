package ca.phon.app.log;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import ca.phon.app.log.actions.CloseAllBuffersAction;
import ca.phon.app.log.actions.CloseCurrentBufferAction;
import ca.phon.app.log.actions.SaveCurrentBufferAction;
import ca.phon.app.log.actions.SaveLogBufferAction;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;

public class BufferWindow extends CommonModuleFrame {

	private static final long serialVersionUID = 3829673739546485612L;

	private JTabbedPane tabPane;
	
	private static BufferWindow _instance;
	
	private final Map<String, BufferPanel> panels = 
			Collections.synchronizedMap(new HashMap<String, BufferPanel>());
	
	public static BufferWindow getInstance() {
		if(_instance == null) {
			_instance = new BufferWindow();
		}
		return _instance;
	}
	
	private BufferWindow() {
		super();
		setWindowName("Buffers");
		
		init();
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		
		final JMenu fileMenu = menuBar.getMenu(0);
		if(!fileMenu.getText().equals("File")) return;
		
		final SaveCurrentBufferAction saveBufferAct = new SaveCurrentBufferAction();
		fileMenu.add(new JMenuItem(saveBufferAct), 0);
		fileMenu.add(new JSeparator(), 1);
		
		final CloseCurrentBufferAction closeBufferAct = new CloseCurrentBufferAction();
		fileMenu.add(new JMenuItem(closeBufferAct), 2);
		
		final CloseAllBuffersAction closeAllBuffersAct = new CloseAllBuffersAction();
		fileMenu.add(new JMenuItem(closeAllBuffersAct), 3);
		fileMenu.add(new JSeparator(), 4);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader("Buffers", "");
		add(header, BorderLayout.NORTH);
		
		tabPane = new JTabbedPane();
		add(tabPane, BorderLayout.CENTER);
	}
	
	public BufferPanel createBuffer(String name) {
		int idx = 0;
		final String prefix = name;
		while(panels.keySet().contains(name)) {
			name = prefix + "(" + (++idx) + ")";
		}
		final BufferPanel retVal = new BufferPanel(name);
		
		tabPane.addTab(name, retVal);
		tabPane.setSelectedComponent(retVal);
		
		panels.put(name, retVal);
		
		return retVal;
	}
	
	public BufferPanel getBuffer(String name) {
		return panels.get(name);
	}
	
	public void removeBuffer(String name) {
		final BufferPanel panel = panels.remove(name);
		if(panel != null) {
			tabPane.remove(panel);
		}
	}
	
	public Collection<String> getBufferNames() {
		return panels.keySet();
	}
	
	public BufferPanel getCurrentBuffer() {
		return (tabPane.getSelectedComponent() != null ? (BufferPanel)tabPane.getSelectedComponent() : null);
	}
	
	public void closeAllBuffers() {
		final String[] bufferNames = panels.keySet().toArray(new String[0]);
		for(String buffer:bufferNames) {
			removeBuffer(buffer);
		}
	}

	public void closeCurrentBuffer() {
		if(tabPane.getSelectedComponent() != null) {
			final BufferPanel panel = (BufferPanel)tabPane.getSelectedComponent();
			removeBuffer(panel.getBufferName());
		}
	}
	
}
