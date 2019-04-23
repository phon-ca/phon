package ca.phon.app.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.events.StartElement;

import ca.phon.app.actions.XMLOpenHandler;
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.DesktopProject;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionEditorEP;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.session.Session;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionReader;
import ca.phon.ui.CommonModuleFrame;

public class SessionXMLFileOpenHandler implements XMLOpenHandler, IPluginExtensionPoint<XMLOpenHandler> {

	@Override
	public Class<?> getExtensionType() {
		return XMLOpenHandler.class;
	}

	@Override
	public IPluginExtensionFactory<XMLOpenHandler> getFactory() {
		return (args) -> this;
	}

	@Override
	public Set<String> supportedExtensions() {
		return Set.of("xml");
	}

	@Override
	public boolean canRead(StartElement startEle) {
		if(startEle.getName().getNamespaceURI().equals("http://phon.ling.mun.ca/ns/phonbank") 
				&& startEle.getName().getLocalPart().equals("session")) {
			return true;
		} else {
			return false;
		}
	}

	private SessionEditor findEditorForFile(File file) {
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof SessionEditor) {
				SessionEditor editor = (SessionEditor)cmf;
				
				Project project = editor.getProject();
				Session session = editor.getSession();
				String sessionPath = project.getSessionPath(session);
				File sessionFile = new File(sessionPath);
				
				if(sessionFile.equals(file)) {
					return editor;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void openXMLFile(File file) throws IOException {
		SessionEditor existingEditor = findEditorForFile(file);
		if(existingEditor != null) {
			existingEditor.toFront();
			return;
		}
		
		SessionInputFactory factory = new SessionInputFactory();
		SessionReader reader = factory.createReaderForFile(file);
		Session session = reader.readSession(new FileInputStream(file));
		session.setCorpus(file.getParentFile().getName());
		
		Project project = findProjectForFile(file);
		if(project == null) {
			project = createTempProjectForFile(file);
		}
		
		final EntryPointArgs args = new EntryPointArgs();
		args.put(EntryPointArgs.PROJECT_OBJECT, project);
		args.put(EntryPointArgs.SESSION_OBJECT, session);
		PluginEntryPointRunner.executePluginInBackground(SessionEditorEP.EP_NAME, args);
	}
	
	private Project createTempProjectForFile(File file) {
		File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
		File projectFolder = new File(tmpFolder, UUID.randomUUID().toString());
		projectFolder.mkdirs();
		
		try {
			DesktopProject project = new DesktopProject(projectFolder);
			project.setName("Temp");
			project.addCorpus(file.getParentFile().getName(), "");
			project.setCorpusPath(file.getParentFile().getName(), file.getParentFile().getAbsolutePath());
			project.setCorpusMediaFolder(file.getParentFile().getName(), file.getParentFile().getAbsolutePath());
			
			return project;
		} catch (ProjectConfigurationException | IOException e) {
			LogUtil.warning(e);
		}
		return null;
	}
	
	private Project findProjectForFile(File file) {
		File corpusFolder = file.getParentFile();
		File projectFolder = corpusFolder.getParentFile();
		
		// see if project is already open
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			Project windowProj = cmf.getExtension(Project.class);
			if(windowProj != null) {
				File windowProjFolder = new File(windowProj.getLocation());
				if(windowProjFolder.equals(projectFolder)) {
					return windowProj;
				}
			}
		}
		
		File projectFile = new File(projectFolder, "project.xml");
		if(projectFile.exists()) {
			try {
				return new DesktopProject(projectFolder);
			} catch (ProjectConfigurationException e) {
				return null;
			}
		}
		return null;
	}

}
