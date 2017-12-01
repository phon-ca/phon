package ca.phon.app.log;

import java.util.EventListener;

public interface BufferPanelContainerListener extends EventListener {

	public void bufferAdded(String bufferName);
	
	public void bufferRemoved(String bufferName);
	
}
