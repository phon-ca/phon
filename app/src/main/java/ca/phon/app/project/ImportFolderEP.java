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
package ca.phon.app.project;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionReader;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

/**
 * This module will import the contents of a directory
 * into a corpus in a phon project.  Each xml file found
 * in the folder will be imported as a session with the
 * same name as the file.  The user has a choice of importing
 * the data into a new project, or into a currently open project.
 * 
 *
 */
@PhonPlugin(name="default")
public class ImportFolderEP implements IPluginEntryPoint {
	
	private static final Logger LOGGER = Logger
			.getLogger(ImportFolderEP.class.getName());

	private final static String EP_NAME = "ImportFolder";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	/*
	 * Imports the contents of a directory into the
	 * given project and corpus.  (The corpus
	 * is created if it does not already exist.) The
	 * actual import is done in a separate thread so
	 * that this method can be safely called from UI
	 * action code.
	 */
	private void importFolder(String folder, Project project, 
			String corpus) {
		File folderFile = new File(folder);
		if(!folderFile.exists() || !folderFile.isDirectory()) {
			return;
		}
		
		// get a listing of the xml
		// files in the directory
		FileFilter xmlFilter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				boolean retVal = false;
				if(name.endsWith(".xml") ||
						name.endsWith(".tb"))
					retVal = true;
				return retVal;
			}
			
		};
		
		// get a new worker
		ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<Runnable>();
		PhonWorker worker = PhonWorker.createWorker(tasks);
		
		File[] files = folderFile.listFiles(xmlFilter);
		for(File f:files) {
			// create a new task
			tasks.add(new ImportTask(f, project, corpus));
		}
		worker.start();
	}
	
	private class ImportTask extends PhonTask {
		
		private String corpus;
		private File transcriptFile;
		private Project project;
		
		public ImportTask(File transcriptFile, Project project, String corpus) {
			super();
			super.setStatus(TaskStatus.WAITING);
			
			this.transcriptFile = transcriptFile;
			this.corpus = corpus;
			this.project = project;
		}

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			try {
				FileInputStream fIn = new FileInputStream(transcriptFile);
				
				// first create a DOM document from the data
				// make sure the root element is CHAT and look
				// at the Version attribute
				DocumentBuilderFactory domFact = 
					DocumentBuilderFactory.newInstance();
				DocumentBuilder domBuilder = null;
				Document doc = null; 
				
				domBuilder = domFact.newDocumentBuilder();
				doc = domBuilder.parse(fIn);
				
				// look at the Version attribute
				// if it does not exist - the file is most likely
				// pre 1.3 version file
				Element chatElement = doc.getDocumentElement();
				String tbVersion = chatElement.getAttribute("Version");
				if(tbVersion == null || tbVersion.length() == 0)
					tbVersion = "1.2";
				
				String sessionName = transcriptFile.getName();
				sessionName = sessionName.substring(0, sessionName.lastIndexOf("."));
				
				// get the appropriate factory
				final SessionInputFactory inputFactory = new SessionInputFactory();
				final SessionReader reader = inputFactory.createReader("phonbank", tbVersion);
				if(reader == null) 
					throw new IOException("No reader found for session");
				
				fIn.close();
				fIn = new FileInputStream(transcriptFile);
				
				final Session session = reader.readSession(fIn);
				
				// fix corpus and session names
				session.setCorpus(corpus);
				session.setName(sessionName);
				
				final UUID writeLock = project.getSessionWriteLock(session);
				if(writeLock == null)
					throw new IOException("Could not lock session: " + corpus + "." + sessionName);
				project.saveSession(session, writeLock);
				project.releaseSessionWriteLock(session, writeLock);
				
			} catch (IOException e) { 
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				super.setStatus(TaskStatus.ERROR);
				err = e;
			} catch (ParserConfigurationException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				super.setStatus(TaskStatus.ERROR);
				err = e;
			} catch (SAXException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				super.setStatus(TaskStatus.ERROR);
				err = e;
			}

			super.setStatus(TaskStatus.FINISHED);
		}
	}
	
	/*
	 * Module entry point
	 * (non-Javadoc)
	 * @see ca.phon.system.api.IModuleController#begin(java.util.HashMap)
	 */
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		final EntryPointArgs epArgs = new EntryPointArgs(initInfo);
		final Project project = epArgs.getProject();
		
		String directory = NativeDialogs.browseForDirectoryBlocking(
				CommonModuleFrame.getCurrentFrame(), "Import Corpus", "");
		if(directory != null) {
			File dirFile = new File(directory);
			try {
				project.addCorpus(dirFile.getName(), "");
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			importFolder(directory, project, dirFile.getName());
		}
	}

}
