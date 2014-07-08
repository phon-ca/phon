package ca.phon.app.log.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogBuffer;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SaveLogBufferAction extends HookableAction {
	
	private static final Logger LOGGER = Logger
			.getLogger(SaveLogBufferAction.class.getName());
	
	private static final long serialVersionUID = -2827879669257916438L;
	
	private final LogBuffer logBuffer;
	
	private final static String CMD_NAME = "Save";
	
	private final static String SHORT_DESC = "Save buffer to file...";
	
	private final static String ICON = "actions/document-save-as";

	public SaveLogBufferAction(LogBuffer buffer) {
		this.logBuffer = buffer;
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(logBuffer != null) {
			final SaveDialogProperties props = new SaveDialogProperties();
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			props.setRunAsync(false);
			props.setCanCreateDirectories(true);
			props.setTitle("Save buffer: "  + logBuffer.getBufferName());
			props.setInitialFile(logBuffer.getBufferName() + ".txt");
			
			final String bufferFile = NativeDialogs.showSaveDialog(props);
			if(bufferFile != null) {
				try {
					final FileOutputStream out = new FileOutputStream(new File(bufferFile));
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
