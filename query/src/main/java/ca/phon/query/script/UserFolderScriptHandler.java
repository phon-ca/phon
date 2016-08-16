/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.query.script;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import ca.phon.query.db.ScriptLibrary;
import ca.phon.util.resources.FolderHandler;

public class UserFolderScriptHandler extends FolderHandler<QueryScript> {

	public UserFolderScriptHandler(File folder) {
		super(folder);
		setFileFilter(scriptFilter);
		setRecursive(true);
	}
	
	@Override
	public QueryScript loadFromFile(File f) throws IOException {
		QueryScript retVal = new QueryScript(f.toURI().toURL());
		
		final QueryName qn = retVal.getExtension(QueryName.class);
		qn.setScriptLibrary(ScriptLibrary.USER);
		
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
