package ca.phon.app.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.app.menu.file.OpenFileHistory;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

public class OpenFileEP extends HookableAction implements IPluginEntryPoint {

	public static String EP_NAME = "OpenFile";
	
	public static String DESC = "Open file on disk...";
	
	public static String INPUT_FILE = OpenFileEP.class.getName() + ".inputFile";
	
	private File inputFile;
	
	public OpenFileEP() {
		super();
		
		putValue(NAME, "Open file...");
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
	}
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		var inputFileObj = args.get(INPUT_FILE);
		if(inputFileObj != null) {
			if(inputFileObj instanceof File) {
				inputFile = (File)inputFileObj;
			} else {
				inputFile = new File(inputFileObj.toString());
			}
		}
		SwingUtilities.invokeLater( () -> {
			ActionEvent ae = new ActionEvent(OpenFileEP.this, 0, EP_NAME);
			hookableActionPerformed(ae);
		});
	}
	
	private FileFilter createFileFilter() {
		Set<String> supportedExtensions = new LinkedHashSet<>();
		
		List<IPluginExtensionPoint<OpenFileHandler>> fileHandlers = PluginManager.getInstance().getExtensionPoints(OpenFileHandler.class);
		for(var extPt:fileHandlers) {
			OpenFileHandler handler = extPt.getFactory().createObject();
			supportedExtensions.addAll(handler.supportedExtensions());
		}
		
		String extensions = supportedExtensions.stream().collect(Collectors.joining(";"));
		FileFilter retVal = new FileFilter("Supported files", extensions);
		return retVal;
	}
	
	public void dialogFinished(NativeDialogEvent evt) {
		if(evt.getDialogResult() != NativeDialogEvent.OK_OPTION) return;
		String selectedFile = evt.getDialogData().toString();
		openFile(new File(selectedFile));
	}
	
	public void openFile(File file) {
		List<IPluginExtensionPoint<OpenFileHandler>> fileHandlers = 
				PluginManager.getInstance().getExtensionPoints(OpenFileHandler.class);
		for(var extPt:fileHandlers) {
			OpenFileHandler handler = extPt.getFactory().createObject();
			
			String fileExt = FilenameUtils.getExtension(file.getName());
			boolean canOpen = handler.supportedExtensions().contains(fileExt);
			if(canOpen) {
				try {
					canOpen = handler.canOpen(file);
				} catch (IOException e) {
					LogUtil.warning(e);
				}
			}
			if(canOpen) {
				try {
					handler.openFile(file);
					
					OpenFileHistory history = new OpenFileHistory();
					history.addToHistory(file);
					history.saveHistory();
					
					break;
				} catch (IOException e) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(e);
				}
			}
		}
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(inputFile == null) {
			OpenDialogProperties props = new OpenDialogProperties();
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			props.setAllowMultipleSelection(false);
			props.setCanChooseDirectories(false);
			props.setCanCreateDirectories(false);
			props.setCanChooseFiles(true);
			props.setFileFilter(createFileFilter());
			props.setRunAsync(true);
			props.setListener(this::dialogFinished);
			
			NativeDialogs.showOpenDialog(props);
		} else {
			openFile(inputFile);
		}
	}

}
