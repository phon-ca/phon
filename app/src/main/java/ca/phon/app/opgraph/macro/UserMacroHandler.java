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
package ca.phon.app.opgraph.macro;

import ca.phon.util.PrefHelper;
import ca.phon.util.resources.FolderHandler;

import java.io.*;
import java.net.URL;

public class UserMacroHandler extends FolderHandler<URL> {

	public final static String DEFAULT_USER_MACRO_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "macro";
	
	public UserMacroHandler() {
		this(new File(DEFAULT_USER_MACRO_FOLDER));
	}
	
	public UserMacroHandler(File file) {
		super(file);
		setRecursive(true);
		super.setFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return (pathname.getName().endsWith(".xml") ||
						pathname.getName().endsWith(".opgraph"));
			}
		});
	}

	@Override
	public URL loadFromFile(File f) throws IOException {
		return (f != null ? f.toURI().toURL() : null);
	}
	
}
