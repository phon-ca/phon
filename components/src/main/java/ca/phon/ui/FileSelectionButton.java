package ca.phon.ui;

import ca.hedlund.desktopicons.*;
import ca.phon.ui.action.*;
import ca.phon.ui.dnd.FileTransferHandler;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.util.List;
import java.util.*;

/**
 * A file selection, multi-action button with history.
 *
 */
public class FileSelectionButton extends MultiActionButton {

	private File selection;

	// file history
	private Iterable<File> files;

	private FileFilter fileFilter;

	private boolean selectFile = true;

	private boolean selectFolder = false;

	public FileSelectionButton() {
		super();
		init();
	}

	public void setFiles(Iterable<File> files) {
		this.files = files;

		if(this.files != null) {
			PhonUIAction showFilesAct = PhonUIAction.eventConsumer(this::onShowFiles);
			showFilesAct.putValue(Action.NAME, "Select file");
			showFilesAct.putValue(Action.SHORT_DESCRIPTION, "Select file/folder");
			setDefaultAction(showFilesAct);
		} else {
			this.files = null;
			setDefaultAction(createBrowseAction());
		}
	}

	public Iterable<File> getFiles() {
		return this.files;
	}

	public FileFilter getFileFilter() {
		return fileFilter;
	}

	public void setFileFilter(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}

	public boolean isSelectFile() {
		return selectFile;
	}

	public void setSelectFile(boolean selectFile) {
		this.selectFile = selectFile;
	}

	public boolean isSelectFolder() {
		return selectFolder;
	}

	public void setSelectFolder(boolean selectFolder) {
		this.selectFolder = selectFolder;
	}

	public File getSelection() {
		return this.selection;
	}

	public void setSelection(String selection) {
		setSelection(new File(selection));
	}

	public void setSelection(File selection) {
		var oldVal = this.selection;
		this.selection = selection;
		firePropertyChange("selection", oldVal, this.selection);
	}

	private void init() {
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		StockIcon stockIcon =
				(OSInfo.isMacOs() ? MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.FOLDER);
		ImageIcon icn = IconManager.getInstance().getSystemStockIcon(stockIcon, IconSize.SMALL);
		DropDownIcon ddicn = new DropDownIcon(icn, 0, SwingConstants.BOTTOM);

		setTopLabelText("Click to select file/folder");
		getTopLabel().setIcon(ddicn);
		getTopLabel().setFont(FontPreferences.getTitleFont());
		update();

		addPropertyChangeListener("selection", (e) -> this.update() );

		// setup drop target
		TransferHandler handler = new TransferHandler();
		setTransferHandler(handler);
	}

	private void update() {
		if(getSelection() == null) {
			getBottomLabel().setText("<html><i>No selection</i></html>");
			getBottomLabel().setForeground(Color.lightGray);
		} else {
			getBottomLabel().setText(getSelection().getAbsolutePath());
			getBottomLabel().setForeground(Color.darkGray);
		}
	}

	public void onShowFiles(PhonActionEvent<Void> pae) {
		if(this.files == null)  return;

		JPopupMenu menu = new JPopupMenu();
		MenuBuilder builder = new MenuBuilder(menu);

		List<File> history = new ArrayList<>();
		for(File f:this.files) history.add(f);
		if(history.size() > 0) {
			for(File f:history) {
				PhonUIAction<File> selectFileAct = PhonUIAction.consumer(this::setSelection, f);
				selectFileAct.putValue(PhonUIAction.NAME, f.getAbsolutePath());
				selectFileAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select " + f.getAbsolutePath());
				builder.addItem(".", selectFileAct);
			}
			builder.addSeparator(".", "history");
			builder.addItem(".", createBrowseAction());

			menu.show(this, 0, getHeight());
		} else {
			createBrowseAction().actionPerformed(pae.getActionEvent());
		}
	}

	public void onBrowse() {
		OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setAllowMultipleSelection(false);
		props.setCanChooseFiles(this.selectFile);
		props.setCanChooseDirectories(this.selectFolder);
		props.setCanCreateDirectories(true);
		props.setFileFilter(fileFilter);
		props.setRunAsync(false);

		List<String> selection = NativeDialogs.showOpenDialog(props);
		if(selection != null && selection.size() > 0) {
			setSelection(selection.get(0));
		}
	}

	public Action createBrowseAction() {
		PhonUIAction browseAct = PhonUIAction.runnable(this::onBrowse);
		browseAct.putValue(PhonUIAction.NAME, "Browse...");
		browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for file/folder...");
		browseAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL));
		browseAct.putValue(PhonUIAction.LARGE_ICON_KEY, IconManager.getInstance().getIcon("actions/document-open", IconSize.MEDIUM));
		return browseAct;
	}

	private class TransferHandler extends FileTransferHandler {

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			try {
				File f = getFile(t);

				if(!f.exists()) return false;

				if(!isSelectFile() && f.isFile()) return false;
				if(!isSelectFolder() && f.isDirectory()) return false;

				setSelection(f);
			} catch (IOException e) {
				return false;
			}
			return true;
		}

	}

}
