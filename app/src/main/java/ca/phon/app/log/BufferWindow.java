/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import ca.phon.app.log.actions.CloseAllBuffersAction;
import ca.phon.app.log.actions.CloseCurrentBufferAction;
import ca.phon.app.log.actions.SaveCurrentBufferAction;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;

public class BufferWindow extends CommonModuleFrame implements BufferPanelContainer {

	private static final long serialVersionUID = 3829673739546485612L;

	private static BufferWindow _instance;
	
	private MultiBufferPanel bufferPanel;
	
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
		
		bufferPanel = new MultiBufferPanel();
		add(bufferPanel, BorderLayout.CENTER);
	}
	
	public BufferPanel createBuffer(String name) {
		return bufferPanel.createBuffer(name);
	}
	
	public BufferPanel getBuffer(String name) {
		return bufferPanel.getBuffer(name);
	}
	
	public void removeBuffer(String name) {
		bufferPanel.removeBuffer(name);
	}
	
	public Collection<String> getBufferNames() {
		return bufferPanel.getBufferNames();
	}
	
	public void selectBuffer(String name) {
		bufferPanel.selectBuffer(name);
	}
	
	public BufferPanel getCurrentBuffer() {
		return bufferPanel.getCurrentBuffer();
	}
	
	public void closeAllBuffers() {
		bufferPanel.closeAllBuffers();
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
			} else if(size.width < 968) {
				size.width = 968;
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
