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
package ca.phon.app.opgraph.nodes.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.model.api.ReportDesignHandle;

public class BirtReportEngine {
	
	private final static Logger LOGGER = Logger.getLogger(BirtReportEngine.class.getName());
	
	private IReportEngine reportEngine;
	
	public BirtReportEngine() {
		super();
		
		startEngine();
	}
	
	public void startEngine() {
		try {
			final EngineConfig config = new EngineConfig();
			Platform.startup(config);
			
			final IReportEngineFactory engineFactory = 
					(IReportEngineFactory)Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			reportEngine = engineFactory.createReportEngine( config );
		} catch (BirtException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	public IReportEngine getReportEngine() {
		return this.reportEngine;
	}
	
	public IReportRunnable openReportDesign(ReportDesignHandle designHandle) throws EngineException {
		return reportEngine.openReportDesign(designHandle);
	}
	
	@SuppressWarnings("unchecked")
	public void renderHTMLDocument(String name, String file, IReportRunnable design) throws EngineException {
		IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);
		
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, 
				BirtReportEngine.class.getClassLoader());
		task.setParameterValue("reportName", name);
		
		HTMLRenderOption options = new HTMLRenderOption();
		options.setOutputFileName(file);
		options.setOutputFormat("html");
		
		options.setEmbeddable(false);
		task.setRenderOption(options);
		
		task.run();
		task.close();
	}

//	private BirtClassloader getBirtClassloader() {
//		final String classpath = System.getProperty("java.class.path");
//		String[] jars = classpath.split(":");
//		
//		List<URL> urls = new ArrayList<>();
//		for(String jar:jars) {
//			if(!jar.contains("rhino")) {
//				try {
//					urls.add(new URL("file:/" + jar));
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				}
//				
//			} else {
//				System.out.println("Excluding jar " + jar);
//			}
//		} 
//		
//		return new BirtClassloader(urls.toArray(new URL[0]));
//	}
	
}
