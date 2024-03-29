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
import ca.phon.util.resources.ClassLoaderHandler;

import java.io.IOException;
import java.net.URL;

public class SystemQueryScriptHandler extends ClassLoaderHandler<QueryScript>{
	
	private final static String LIST = "ca/phon/query/script/query.list";
	
	public SystemQueryScriptHandler() {
		super();
		super.loadResourceFile(LIST);
	}
	
	@Override
	public QueryScript loadFromURL(URL url) throws IOException {
		QueryScript retVal = new QueryScript(url);
		
		final QueryName qn = retVal.getExtension(QueryName.class);
		qn.setScriptLibrary(ScriptLibrary.STOCK);
		
		return retVal;
	}

}
