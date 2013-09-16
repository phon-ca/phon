/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

package ca.phon.query.db.xml;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.query.db.QueryManager;


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
