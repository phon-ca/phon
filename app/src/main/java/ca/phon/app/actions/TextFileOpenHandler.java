package ca.phon.app.actions;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;

import ca.phon.app.log.*;
import ca.phon.phonex.*;
import ca.phon.plugin.*;

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
