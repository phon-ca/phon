package ca.phon.ui;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.StockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.OSInfo;
import ca.phon.util.RecentFiles;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.tools.Tool;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A file selection, multi-action button with history.
 *
 */
public class FileSelectionButton extends MultiActionButton {

	private File selection;

	// file history
	private RecentFiles files;

	private boolean selectFile = true;

	private boolean selectFolder = false;

	public FileSelectionButton() {
		this(null);
	}

	public FileSelectionButton(String historyProp) {
		super();

		setHistoryPropertyKey(historyProp);
		init();
	}

	public void setHistoryPropertyKey(String property) {
		if(property != null) {
			this.files = new RecentFiles(property);

			PhonUIAction selectHistoryAct =
					new PhonUIAction(this, "onShowHistory");
			selectHistoryAct.putValue(Action.NAME, "Select file");
			selectHistoryAct.putValue(Action.SHORT_DESCRIPTION, "Select file/folder");
			setDefaultAction(selectHistoryAct);
		} else {
			this.files = null;
			setDefaultAction(createBrowseAction());
		}
	}

	public String getHistoryPropertyKey() {
		return (this.files != null ? this.files.getPropertyKey() : null);
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
		this.selection = selection;
		update();
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

	public void onShowHistory(PhonActionEvent pae) {
		if(this.files == null)  return;

		JPopupMenu menu = new JPopupMenu();
		MenuBuilder builder = new MenuBuilder(menu);

		List<File> history = new ArrayList<>();
		for(File f:this.files) history.add(f);
		if(history.size() > 0) {
			for(File f:history) {
				PhonUIAction selectFileAct = new PhonUIAction(this, "setSelection", f);
				selectFileAct.putValue(PhonUIAction.NAME, f.getAbsolutePath());
				selectFileAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select " + f.getAbsolutePath());
				builder.addItem(".", selectFileAct);
			}
			builder.addSeparator(".", "history");
			builder.addItem(".", createBrowseAction());
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

		List<String> selection = NativeDialogs.showOpenDialog(props);
		if(selection.size() > 0) {
			setSelection(selection.get(0));
		}
	}

	private Action createBrowseAction() {
		PhonUIAction browseAct = new PhonUIAction(this, "onBrowse");
		browseAct.putValue(PhonUIAction.NAME, "Browse...");
		browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for file/folder...");
		browseAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL));
		browseAct.putValue(PhonUIAction.LARGE_ICON_KEY, IconManager.getInstance().getIcon("actions/document-open", IconSize.MEDIUM));
		return browseAct;
	}

}
