package ca.phon.app.actions;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferWindow;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;

@PhonexPlugin(name="Open")
public class TextFileOpenHandler implements OpenFileHandler, IPluginExtensionPoint<OpenFileHandler> {

	@Override
	public Set<String> supportedExtensions() {
		return Set.of("txt");
	}

	@Override
	public boolean canOpen(File file) throws IOException {
		return file.getName().endsWith(".txt");
	}

	@Override
	public void openFile(File file) throws IOException {
		// open file in new buffer
		BufferWindow bufferWindow = BufferWindow.getBufferWindow();
		
		BufferPanel bp = bufferWindow.createBuffer(file.getName());
		bp.getLogBuffer().setText(FileUtils.readFileToString(file, "UTF-8"));
		
		if(!bufferWindow.isVisible()) {
			bufferWindow.showWindow();
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return OpenFileHandler.class;
	}

	@Override
	public IPluginExtensionFactory<OpenFileHandler> getFactory() {
		return (args) -> this;
	}

}
