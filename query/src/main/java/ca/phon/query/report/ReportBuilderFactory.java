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
package ca.phon.query.report;

import ca.phon.plugin.*;
import ca.phon.query.report.csv.CSVReportBuilder;
import ca.phon.util.CollatorFactory;

import java.util.*;

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
