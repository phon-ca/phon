package ca.phon.app.menu.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.project.DesktopProjectFactory;
import ca.phon.app.project.OpenProjectEP;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.nativedialogs.SaveDialogProperties;

public class NewProjectCommand extends HookableAction {
	
private final static String TXT = "New project...";
	
	private final static String DESC = "Create project...";
	
	private final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_N,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);
	
	private static final long serialVersionUID = -1288974377105467180L;

	private final static Logger LOGGER = Logger.getLogger(NewProjectCommand.class.getName());

	public NewProjectCommand() {
		super();
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanCreateDirectories(true);
		props.setTitle("New Project");
		props.setMessage("Create a new project at given path");
		props.setNameFieldLabel("Enter project name:");
		props.setPrompt("Create Project");
		props.setRunAsync(true);
		props.setListener( (e) -> {
			if(e.getDialogData() != null) {
				final String folderName = e.getDialogData().toString();
				final File folder = new File(folderName);
				
				if(folder.exists()) {
					final MessageDialogProperties p2 = new MessageDialogProperties();
					p2.setParentWindow(CommonModuleFrame.getCurrentFrame());
					p2.setHeader("Create Project Failed");
					p2.setMessage("Cannot create project, an item already exists at path " + folderName);
					p2.setOptions(MessageDialogProperties.okOptions);
					p2.setRunAsync(false);
					NativeDialogs.showMessageDialog(p2);
					
					return;
				}
				
				final File parentFolder = folder.getParentFile();
				final File testFile = new File(parentFolder, "project.xml");
				if(testFile.exists()) {
					final MessageDialogProperties p2 = new MessageDialogProperties();
					p2.setParentWindow(CommonModuleFrame.getCurrentFrame());
					p2.setHeader("Create Project Failed");
					p2.setMessage("Cannot create project inside of another");
					p2.setOptions(MessageDialogProperties.okOptions);
					p2.setRunAsync(false);
					NativeDialogs.showMessageDialog(p2);
					
					return;
				}
				
				try {
					final ProjectFactory factory = new DesktopProjectFactory();
					factory.createProject(folder);
					
					// open project
					final HashMap<String, Object> initInfo = new HashMap<String, Object>();
					initInfo.put(OpenProjectEP.PROJECTPATH_PROPERTY, folderName);
					
					PluginEntryPointRunner.executePluginInBackground(OpenProjectEP.EP_NAME, initInfo);
				} catch (IOException ex) {
					LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), e);
					final MessageDialogProperties p2 = new MessageDialogProperties();
					p2.setParentWindow(CommonModuleFrame.getCurrentFrame());
					p2.setHeader("Create Project Failed");
					p2.setMessage("Cannot create project: " + ex.getLocalizedMessage());
					p2.setOptions(MessageDialogProperties.okOptions);
					p2.setRunAsync(false);
					NativeDialogs.showMessageDialog(p2);
				}
			}
		});
		NativeDialogs.showDialog(props);
	}

}
