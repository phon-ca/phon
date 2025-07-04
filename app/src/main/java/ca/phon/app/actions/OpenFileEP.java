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
package ca.phon.app.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.app.menu.file.OpenFileHistory;
import ca.phon.plugin.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.*;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class OpenFileEP extends HookableAction implements IPluginEntryPoint {

	public static String EP_NAME = "OpenFile";
	
	public static String DESC = "Open file on disk...";
	
	public static String INPUT_FILE = OpenFileEP.class.getName() + ".inputFile";
	
	private File inputFile;

	private Map<String, Object> args = new HashMap<>();
	
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
		this.args = args;
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
		final String selectedFile = evt.getDialogData().toString();
		SwingUtilities.invokeLater(() -> {
			openFile(new File(selectedFile), args);
		});
	}
	
	public void openFile(File file, Map<String, Object> args) {
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
					handler.openFile(file, args);
					
					OpenFileHistory history = new OpenFileHistory();
					history.addToHistory(file);
					history.saveHistory();
					
					break;
				} catch (IOException e) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(e);
					
					if(CommonModuleFrame.getCurrentFrame() != null) {
						CommonModuleFrame.getCurrentFrame()
							.showMessageDialog("Open file failed", e.getLocalizedMessage(), MessageDialogProperties.okOptions);
					}
				}
			}
		}
	}

	private void doOpen(Map<String, String> args) {

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
			openFile(inputFile, args);
		}
	}

}
