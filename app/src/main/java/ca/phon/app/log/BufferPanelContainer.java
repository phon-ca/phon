package ca.phon.app.log;

import java.util.Collection;

public interface BufferPanelContainer {

	public BufferPanel createBuffer(String name);
	
	public BufferPanel getBuffer(String name);
	
	public void removeBuffer(String name);
	
	public Collection<String> getBufferNames();
	
	public void selectBuffer(String name);
	
	public BufferPanel getCurrentBuffer();
	
}
