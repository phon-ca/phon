package ca.phon.app.log;

import java.util.EventListener;

public interface BufferPanelSelectionListener extends EventListener {
	
	public void bufferSelected(String bufferName);
	
}
