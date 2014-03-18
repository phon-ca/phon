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

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
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
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Phon module which will import an XML file as a new transcript.
 * The XML file must be a format understood by phon.
 *
 */
@PhonPlugin(name="default")
public class ImportSessionEP implements IPluginEntryPoint {
	
	private static final Logger LOGGER = Logger
			.getLogger(ImportSessionEP.class.getName());
	
	/** The project */
	private Project _project;
	
	/** The corpus */
	private String corpus;
	
	/** The new session name */
	private String session;
	
	/** The file to import */
	private String filename;
	
//	/** Raw data to import */
//	private StringBuffer rawData;
	
	/** The transcript */
	private Session transcript;

	public ImportSessionEP() {
		super();
	}
	
	private final static String EP_NAME = "ImportSession";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
//	private void loadArguments(Map<String, Object> initInfo) {
//		// get the project
//		if(initInfo.get("project") != null)
//			_project = (IPhonProject)initInfo.get("project");
//		else
//			throw new IllegalArgumentException("'project' argument must be set.");
//		
//		if(initInfo.get("corpus") != null)
//			corpus = initInfo.get("corpus").toString();
//		
//		if(initInfo.get("session") != null)
//			session = initInfo.get("session").toString();
//		
//		if(initInfo.get("filename") != null)
//			filename = initInfo.get("filename").toString();
//		
////		if(initInfo.get("rawdata") != null) {
////			rawData = new StringBuffer();
////			rawData.append(initInfo.get("rawdata").toString());
////		}
//	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		final EntryPointArgs epArgs = new EntryPointArgs(initInfo);
		_project = epArgs.getProject();
		corpus = epArgs.getCorpus();
		session = (epArgs.get(EntryPointArgs.SESSION_NAME) != null ?
				epArgs.get(EntryPointArgs.SESSION_NAME).toString() : null);
		filename = (epArgs.get("filename") != null ? 
				epArgs.get("filename").toString() : null);
		
		if(_project == null) return;
		
		// if we have a filename, corpus and session load the file
		if(filename != null
				&& corpus != null 
				&& session != null) {
			importFileAsSession(filename, corpus, session);
		} else {
			ImportSessionFrame f = new ImportSessionFrame();
			
			f.putExtension(Project.class, _project);
			
			f.pack();
			f.setVisible(true);
		}
	}
	
	/**
	 * This method will try to import <CODE>filename</CODE> into the
	 * given <CODE>corpus</CODE> and <CODE>session</CODE>.  The file
	 * is first check for compatibility.
	 * 
	 * @param filename
	 * @param corpus
	 * @param session
	 * 
	 * @throws ModuleException if the file failed to import.
	 */
	private void importFileAsSession(String filename, String corpus, String sessionName) {
		if(_project == null) 
			throw new IllegalArgumentException("Project cannot be null");
		
		File sessionFile = new File(filename);
		if(!sessionFile.exists())
			throw new IllegalArgumentException("Cannot find file: " + filename);
		
		// first create a DOM document from the data
		// make sure the root element is CHAT and look
		// at the Version attribute
		DocumentBuilderFactory domFact = 
			DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = null;
		Document doc = null; 
		try {
			domBuilder = domFact.newDocumentBuilder();
			doc = domBuilder.parse(new FileInputStream(sessionFile));
			
			// look at the Version attribute
			// if it does not exist - the file is most likely
			// pre 1.3 version file
//			String tbVersion = "1.2";
			Element chatElement = doc.getDocumentElement();
			String tbVersion = chatElement.getAttribute("Version");
			if(tbVersion == null || tbVersion.length() == 0)
				tbVersion = "1.2";
			
			// get the appropriate factory
			
			final SessionInputFactory inputFactory = new SessionInputFactory();
			final SessionReader reader = inputFactory.createReader("phonbank", tbVersion);
			if(reader != null) {
				transcript = reader.readSession(new FileInputStream(sessionFile));
			}
			
			// setup corpus and session
			if(!_project.getCorpora().contains(corpus))
				_project.addCorpus(corpus, "");
			this.corpus = corpus;
			
			if(_project.getCorpusSessions(corpus).contains(sessionName))
				throw new IllegalArgumentException("A session with name '" + sessionName + "' already exists.");
			this.session = sessionName;
			
			//			 ensure session variables are setup
			transcript.setCorpus(corpus);
			transcript.setName(session);
			
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		importSession();
	}
	
	/**
	 * Assumes that all of the instance variables have been setup.
	 * All that's left to do is tell the project to create the new
	 * session and validate the written data.
	 * 
	 * @throws ModuleException
	 */
	private void importSession() {

		try {
			final UUID writeLock = _project.getSessionWriteLock(transcript);
			_project.saveSession(transcript, writeLock);
			_project.releaseSessionWriteLock(corpus, session, writeLock);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	/** The module window class */
	private class ImportSessionFrame extends CommonModuleFrame {
		
		private static final long serialVersionUID = -4725921953928779747L;
		
		/** GUI components */
		private JTextField filenameField;
		private JButton browseButton;
		
		private JComboBox corpusBox;
		
		private JTextField sessionNameField;
		
		private JButton importButton;
		private JButton cancelButton;
		
		public ImportSessionFrame() {
			super("Import Session from XML");
			
			init();
		}
		
		private void init() {
			// setup layout
			FormLayout layout = new FormLayout(
					"5dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref, 5dlu",
					"5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 5dlu");
			Container pane = getContentPane();
			
			pane.setLayout(layout);
			
			// components
			filenameField = new JTextField();
			
			browseButton = new JButton();
			ImageIcon browseIcon = 
					IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
			browseButton.setIcon(browseIcon);
			browseButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					
					Runnable run = new Runnable() {
						@Override
						public void run() {
							FileFilter filters[] = new FileFilter[1];
							filters[0] = FileFilter.xmlFilter;
							
							String filename =
								NativeDialogs.browseForFileBlocking(CommonModuleFrame.getCurrentFrame(),
										"", "*.xml", filters, "Choose XML File");
							if(filename != null) {
								filenameField.setText(filename);
								
								File f = new File(filename);
								String sessionName = f.getName();
								sessionName = sessionName.split("\\.")[0];
								
								sessionNameField.setText(sessionName);
							}
						}
					};
					PhonWorker.getInstance().invokeLater(run);
				}
				
			});
			
			String[] corpora = null;
				corpora = 
					_project.getCorpora().toArray(new String[0]);
			
			corpusBox = new JComboBox(corpora);
//			corpusBox.addActionListener(new ActionListener() {
//
//				public void actionPerformed(ActionEvent e) {
//					checkSessionName();
//				}
//				
//			});
			
			sessionNameField = new JTextField();
//			sessionNameField.getDocument().addDocumentListener(new DocumentListener() {
//
//				public void changedUpdate(DocumentEvent e) {
//					checkSessionName();
//				}
//
//				public void insertUpdate(DocumentEvent e) {
//					checkSessionName();
//				}
//
//				public void removeUpdate(DocumentEvent e) {
//					checkSessionName();
//				}
//				
//			});
			
			importButton = new JButton("Import");
			importButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Runnable run = new Runnable() {
						@Override
						public void run() {
						try {
							importFileAsSession(
									filenameField.getText(),
									corpusBox.getSelectedItem().toString(),
									sessionNameField.getText());
						} catch (Exception e) {
							NativeDialogs.showMessageDialogBlocking(ImportSessionFrame.this, 
									null, "Session Import Failed", e.getMessage());
							LOGGER.log(Level.SEVERE,
									e.getLocalizedMessage(), e);
							return;
						}
						
						ImportSessionFrame.this.dispose();
						}
					};
					PhonWorker.getInstance().invokeLater(run);
				}
				
			});
			
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					CommonModuleFrame.getCurrentFrame().dispose();
				}
				
			});
			
			// add components
			CellConstraints cc = new CellConstraints();
			
			DialogHeader header = new DialogHeader("Import Session", "Import an XML file as a new session.");
			pane.add(header, cc.xyw(2, 2, 5));
			
			pane.add(new JLabel("XML File: "), cc.xy(2, 4));
			pane.add(filenameField, cc.xy(4, 4));
			pane.add(browseButton, cc.xy(6, 4));
			
			pane.add(new JLabel("Corpus: "), cc.xy(2, 6));
			pane.add(corpusBox, cc.xyw(4, 6, 3));
			
			pane.add(new JLabel("Session: "), cc.xy(2, 8));
			pane.add(sessionNameField, cc.xyw(4, 8, 3));
			
			final ButtonBarBuilder barBuilder = new ButtonBarBuilder();
			JPanel buttonBar = 
					barBuilder.addButton(importButton).addButton(cancelButton).build();
			pane.add(buttonBar, cc.xyw(4, 10, 3));
			
			this.getRootPane().setDefaultButton(importButton);
		}
		
		private Border cachedPathBorder = null;
		private boolean checkFilePath() {
			String filepath = filenameField.getText();
			if(!(new File(filepath)).exists()) {
				cachedPathBorder = filenameField.getBorder();
				filenameField.setBorder(BorderFactory.createLineBorder(Color.red));
				return false;
			} else {
				filenameField.setBorder(cachedPathBorder);
				return true;
			}
		}
		
		private Border cachedNameBorder = null;
		private boolean checkSessionName() {
			String corpus = corpusBox.getSelectedItem().toString();
			
			List<String> currentSessions = 
					_project.getCorpusSessions(corpus);
			
			String sessionName = sessionNameField.getName();
			if(sessionName.length() == 0 || 
					currentSessions.contains(sessionName)) {
				cachedNameBorder = sessionNameField.getBorder();
				sessionNameField.setBorder(BorderFactory.createLineBorder(Color.red));
				return false;
			} else {
//				if(cachedNameBorder != null)
				sessionNameField.setBorder(cachedNameBorder);
				return true;
			}
		}
	}
	
}
