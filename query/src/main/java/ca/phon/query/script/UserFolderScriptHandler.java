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
package ca.phon.query.script;

import ca.phon.query.db.ScriptLibrary;
import ca.phon.util.resources.FolderHandler;

import java.io.*;

public class UserFolderScriptHandler extends FolderHandler<QueryScript> {

	private ScriptLibrary scriptLibrary = null;
	
	public UserFolderScriptHandler(File folder, ScriptLibrary library) {
		super(folder);
		this.scriptLibrary = library;
		setFileFilter(scriptFilter);
		setRecursive(true);
	}
	
	@Override
	public QueryScript loadFromFile(File f) throws IOException {
		QueryScript retVal = new QueryScript(f.toURI().toURL());
		
		final QueryName qn = retVal.getExtension(QueryName.class);
		qn.setScriptLibrary(this.scriptLibrary);
		
		return retVal;
	}

	private final FileFilter scriptFilter = new FileFilter() {
		@Override
		public boolean accept(File f) {
			final String name = f.getAbsolutePath();
			boolean prefixOk = 
					!(name.startsWith(".") || name.startsWith("~") || name.startsWith("__"));
			boolean suffixOk = 
					(name.endsWith(".js") || name.endsWith(".xml"));
			return prefixOk && suffixOk;
		}
	};
	
}
