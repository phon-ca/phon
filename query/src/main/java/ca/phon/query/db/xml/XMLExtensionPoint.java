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

package ca.phon.query.db.xml;

import ca.phon.plugin.*;
import ca.phon.query.db.*;


/**
 * Extension point for the XML-based implementation of the query interfaces.
 */
@PhonPlugin(author="Jason Gedge", name="XML query manager")
public class XMLExtensionPoint implements IPluginExtensionPoint<QueryManager> {
	@Override
	public Class<?> getExtensionType() {
		return QueryManager.class;
	}

	@Override
	public IPluginExtensionFactory<QueryManager> getFactory() {
		return new IPluginExtensionFactory<QueryManager>() {
			@Override
			public QueryManager createObject(Object... args) {
				return new XMLQueryManager();
			}
		};
	}
}
