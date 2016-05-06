package ca.phon.app.log.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanelContainer;
import ca.phon.app.log.LogBuffer;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SaveAllBuffersAction extends HookableAction {
	
	private final static Logger LOGGER = Logger.getLogger(SaveAllBuffersAction.class.getName());
	
	private final static String TXT = "Save all";
	
	private final static String DESC = "Save all buffers to folder...";
	
	private final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/save_all", IconSize.SMALL);
	
	private BufferPanelContainer buffers;
	
	public SaveAllBuffersAction(BufferPanelContainer bufferPanelContainer) {
		super();
		
		this.buffers = bufferPanelContainer;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		props.setCanCreateDirectories(true);
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setTitle("Select Assessment Folder");
		props.setInitialFolder(PrefHelper.getUserDataFolder());
		props.setRunAsync(false);
		
		List<String> selectedFolders = NativeDialogs.showOpenDialog(props);
		if(selectedFolders != null && selectedFolders.size() > 0) {
			for(String bufferName:buffers.getBufferNames()) {
				final File bufferFile = new File(selectedFolders.get(0), bufferName + ".csv");
				final LogBuffer logBuffer = buffers.getBuffer(bufferName).getLogBuffer();
				try {
					final FileOutputStream out = new FileOutputStream(bufferFile);
					final OutputStreamWriter writer = new OutputStreamWriter(out, logBuffer.getEncoding());
					writer.write(logBuffer.getText());
					writer.flush();
					writer.close();
				} catch (IOException e) {
					LOGGER
							.log(Level.SEVERE, e.getLocalizedMessage(), e);
					ToastFactory.makeToast(e.getLocalizedMessage()).start(logBuffer);
				}
			}
		}
	}

}
