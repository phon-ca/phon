/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.log;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.menu.MenuBuilder;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BufferWindow extends CommonModuleFrame implements BufferPanelContainer {

	private static final long serialVersionUID = 3829673739546485612L;

	private static BufferWindow _instance;
	
	private MultiBufferPanel bufferPanel;
	
	public static BufferWindow getBufferWindow() {
		// returns the currently open BufferWindow or
		// a new instance
		Optional<CommonModuleFrame> windowRef = 
				CommonModuleFrame.getOpenWindows().stream().filter( (w) -> w instanceof BufferWindow ).findAny();
		if(windowRef.isPresent()) {
			return (BufferWindow)windowRef.get();
		} else {
			return new BufferWindow();
		}
	}
	
	private BufferWindow() {
		super();
		setWindowName("Buffers");
		init();
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		
		final MenuBuilder builder = new MenuBuilder(menuBar);
		final JMenu bufferMenu = builder.addMenu(".@Query", "Buffer");
		bufferMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				bufferPanel.setupMenu(bufferMenu);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
				
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
				
			}
		});
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader("Buffers", "");
		add(header, BorderLayout.NORTH);
		
		bufferPanel = new MultiBufferPanel();
		add(bufferPanel, BorderLayout.CENTER);
	}
	
	public BufferPanel createBuffer(String name, boolean showBuffer) {
		return bufferPanel.createBuffer(name, showBuffer);
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

	@Override
	public void addListener(BufferPanelContainerListener listener) {
		bufferPanel.addListener(listener);
	}

	@Override
	public void removeListener(BufferPanelContainerListener listener) {
		bufferPanel.removeListener(listener);
	}

	@Override
	public List<BufferPanelContainerListener> getListeners() {
		return bufferPanel.getListeners();
	}

	@Override
	public void addSelectionListener(BufferPanelSelectionListener listener) {
		bufferPanel.addSelectionListener(listener);
	}

	@Override
	public void removeSelectionListener(BufferPanelSelectionListener listener) {
		bufferPanel.removeSelectionListener(listener);
	}

	@Override
	public List<BufferPanelSelectionListener> getSelectionListeners() {
		return bufferPanel.getSelectionListeners();
	}

}
