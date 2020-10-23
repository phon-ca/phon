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

import java.util.*;

public interface BufferPanelContainer {

	public BufferPanel createBuffer(String name);
	
	public BufferPanel createBuffer(String name, boolean showBuffer);
	
	public BufferPanel getBuffer(String name);
	
	public void removeBuffer(String name);
	
	public Collection<String> getBufferNames();
	
	public void selectBuffer(String name);
	
	public BufferPanel getCurrentBuffer();
	
	public void closeAllBuffers();
	
	/* Listeners */
	public void addListener(BufferPanelContainerListener listener);
	
	public void removeListener(BufferPanelContainerListener listener);
	
	public List<BufferPanelContainerListener> getListeners();
	
	public void addSelectionListener(BufferPanelSelectionListener listener);
	
	public void removeSelectionListener(BufferPanelSelectionListener listener);
	
	public List<BufferPanelSelectionListener> getSelectionListeners();
	
}
