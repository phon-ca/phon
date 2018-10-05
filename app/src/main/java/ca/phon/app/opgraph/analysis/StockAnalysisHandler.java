/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.analysis;

import java.io.IOException;
import java.net.URL;

import ca.phon.util.resources.ClassLoaderHandler;

public class StockAnalysisHandler extends ClassLoaderHandler<URL> {
	
	private final static String STOCK_ANALYSIS_LIST = "analysis/analysis.list";
	
	public StockAnalysisHandler() {
		super();
		loadResourceFile(STOCK_ANALYSIS_LIST);
	}

	@Override
	public URL loadFromURL(URL url) throws IOException {
		return url;
	}
	
}
