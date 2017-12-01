/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.util.*;

public interface BufferPanelContainer {

	public BufferPanel createBuffer(String name);
	
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
