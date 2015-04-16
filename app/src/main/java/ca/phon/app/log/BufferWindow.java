/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.log;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import ca.phon.app.log.actions.CloseAllBuffersAction;
import ca.phon.app.log.actions.CloseCurrentBufferAction;
import ca.phon.app.log.actions.SaveCurrentBufferAction;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class BufferWindow extends CommonModuleFrame {

	private static final long serialVersionUID = 3829673739546485612L;

	private static BufferWindow _instance;

	private final CardLayout buffersLayout = new CardLayout();
	
	private JPanel buffersPanel;
	
	private JComboBox buffersBox;
	
	private JButton closeButton;
	
	private JPanel topPanel;
	
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
		setDefaultCloseOperation(HIDE_ON_CLOSE);
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
		
		final JPanel centerPanel = new JPanel(new BorderLayout());
		final JPanel selectionPanel = new JPanel(new FormLayout("pref, 3dlu, fill:pref:grow, 3dlu, pref", "pref"));
		final CellConstraints cc = new CellConstraints();
		selectionPanel.add(new JLabel("Buffer: "), cc.xy(1,1));
		buffersBox = new JComboBox();
		buffersBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				final String bufferName = e.getItem().toString();
				selectBuffer(bufferName);
			}
			
		});
		selectionPanel.add(buffersBox, cc.xy(3, 1));
		
		closeButton = new JButton(new CloseCurrentBufferAction());
		selectionPanel.add(closeButton, cc.xy(5, 1));
		
		centerPanel.add(selectionPanel, BorderLayout.NORTH);
		
		buffersPanel = new JPanel(buffersLayout);
		centerPanel.add(buffersPanel, BorderLayout.CENTER);
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public BufferPanel createBuffer(String name) {
		int idx = 0;
		final String prefix = name;
		while(panels.keySet().contains(name)) {
			name = prefix + "(" + (++idx) + ")";
		}
		final BufferPanel retVal = new BufferPanel(name);
		
		buffersPanel.add(retVal, name);
		
		buffersBox.addItem(name);
		buffersBox.setSelectedItem(name);
		
		panels.put(name, retVal);
		
		selectBuffer(name);
		return retVal;
	}
	
	public BufferPanel getBuffer(String name) {
		return panels.get(name);
	}
	
	public void removeBuffer(String name) {
		final BufferPanel panel = panels.remove(name);
		if(panel != null) {
			buffersBox.removeItem(name);
			buffersPanel.remove(panel);
		}
	}
	
	public Collection<String> getBufferNames() {
		return panels.keySet();
	}
	
	public void selectBuffer(String name) {
		buffersLayout.show(buffersPanel, name);
	}
	
	public BufferPanel getCurrentBuffer() {
		return (buffersBox.getSelectedItem() != null 
				? panels.get(buffersBox.getSelectedItem().toString()) : null);
	}
	
	public void closeAllBuffers() {
		final String[] bufferNames = panels.keySet().toArray(new String[0]);
		for(String buffer:bufferNames) {
			removeBuffer(buffer);
		}
	}

	public void closeCurrentBuffer() {
		final BufferPanel panel = getCurrentBuffer();
		removeBuffer(panel.getBufferName());
	}
	
	public void showWindow() {
		if(!isVisible()) {
			pack();
			final Dimension size = getSize();
			
			if(size.width > Toolkit.getDefaultToolkit().getScreenSize().width) {
				size.width = Toolkit.getDefaultToolkit().getScreenSize().width;
			} else if(size.width < 550) {
				size.width = 550;
			}
			size.height = 600;
			setSize(size);
			centerWindow();
			setVisible(true);
		} else {
			requestFocus();
		}
	}
}
