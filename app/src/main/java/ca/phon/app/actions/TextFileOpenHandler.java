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
package ca.phon.app.actions;

import ca.phon.app.log.*;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.plugin.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

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
	public void openFile(File file, Map<String, Object> args) throws IOException {
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
