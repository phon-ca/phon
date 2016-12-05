/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.query.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.query.report.csv.CSVReportBuilder;
import ca.phon.util.CollatorFactory;

/**
 * Factory for creating report builders.
 *
 * @deprecated
 */
@Deprecated
public class ReportBuilderFactory {
	
	/**
	 * Hidden constructor
	 */
	protected ReportBuilderFactory() {
		super();
		fillBuilderMap();
	}
	
	public static ReportBuilderFactory getInstance() {
		return new ReportBuilderFactory();
	}
	
	/**
	 * Map of plugin builder factories
	 */
	public Map<String, IPluginExtensionFactory<ReportBuilder>> pluginBuilders =
		new HashMap<String, IPluginExtensionFactory<ReportBuilder>>();
	
	/**
	 * Populate the map of registered builders.
	 */
	private void fillBuilderMap() {
		pluginBuilders.clear();
		
		// add registered builders from plugins
		List<IPluginExtensionPoint<ReportBuilder>> reportBuilders = 
			PluginManager.getInstance().getExtensionPoints(ReportBuilder.class);
		for(IPluginExtensionPoint<ReportBuilder> builderExtPt:reportBuilders) {
			IPluginExtensionFactory<ReportBuilder> factory = 
				builderExtPt.getFactory();
			pluginBuilders.put(factory.createObject().getDisplayName(), factory);
		}
		
	}
	
	/**
	 * Get names of all available builders
	 */
	public String[] getReportBuilderNames() {
		Set<String> retVal = new HashSet<String>();
		retVal.addAll(pluginBuilders.keySet());
		// add default csv builder
		retVal.add("CSV");
		
		List<String> sortedVals = new ArrayList<String>(retVal);
		Collections.sort(sortedVals, CollatorFactory.defaultCollator());
		return sortedVals.toArray(new String[0]);
	}
	
	/**
	 * Get the builder for the given name
	 * 
	 * @param builder name
	 * @return the builder or <CODE>null</CODE> if not found
	 */
	public ReportBuilder getBuilder(String builder) {
		IPluginExtensionFactory<ReportBuilder> builderFactory = 
			pluginBuilders.get(builder);
		
		ReportBuilder retVal = null;
		
		if(builderFactory == null) {
			if(builder.equals("CSV")) {
				retVal = new CSVReportBuilder();
			}
		} else {
			retVal = builderFactory.createObject();
		}
		
		return retVal;
	}
}
