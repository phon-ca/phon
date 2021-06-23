package ca.phon.app.workspace;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.log.LogUtil;
import ca.phon.app.menu.workspace.SelectWorkspaceCommand;
import ca.phon.app.welcome.WorkspaceTextStyler;
import ca.phon.ui.DropDownIcon;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class WorkspaceButton extends MultiActionButton {

	public WorkspaceButton() {
		super();

		init();
	}

	private void init() {
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		PhonUIAction selectHistoryAct =
				new PhonUIAction(this, "onShowHistory");
		selectHistoryAct.putValue(Action.NAME, "Select workspace");
		selectHistoryAct.putValue(Action.SHORT_DESCRIPTION, "Change workspace folder...");

		ImageIcon workspaceIcnL =
				(Workspace.userWorkspaceFolder().exists()
						? (IconManager.getInstance().getSystemIconForPath(
							Workspace.userWorkspaceFolder().getAbsolutePath(), "places/folder-workspace", IconSize.MEDIUM))
						: IconManager.getInstance().getSystemStockIcon(
						(OSInfo.isMacOs() ? MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.WARNING), IconSize.MEDIUM));
		DropDownIcon icn = new DropDownIcon(workspaceIcnL, 0, SwingConstants.BOTTOM);

		setTopLabelText(WorkspaceTextStyler.toHeaderText("Workspace Folder"));
		getTopLabel().setIcon(icn);
		getTopLabel().setFont(FontPreferences.getTitleFont());
		addAction(createShowWorkspaceAction());
		setDefaultAction(selectHistoryAct);
		update();
		PrefHelper.getUserPreferences().addPreferenceChangeListener((e) -> {
			if(e.getKey().equals(Workspace.WORKSPACE_FOLDER))
				update();
		});
	}

	public void update() {
		File workspaceFolder = Workspace.userWorkspaceFolder();
		setBottomLabelText(workspaceFolder.getAbsolutePath() + " (click to change)");
	}

	public void onShowHistory(PhonActionEvent pae) {
		final WorkspaceHistory history = new WorkspaceHistory();

		final JPopupMenu menu = new JPopupMenu();
		final MenuBuilder builder = new MenuBuilder(menu);

		for(File workspaceFolder:history) {
			if(workspaceFolder.equals(Workspace.userWorkspaceFolder()) && !workspaceFolder.exists()) {
				final PhonUIAction createWorkspaceFolderAct = new PhonUIAction(this, "onCreateWorkspace");
				createWorkspaceFolderAct.putValue(PhonUIAction.NAME, "Create workspace folder");
				createWorkspaceFolderAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Create workspace folder on disk");
				builder.addItem(".", createWorkspaceFolderAct);
			} else {
				ImageIcon workspaceIcn =
						IconManager.getInstance().getSystemIconForPath(
								workspaceFolder.getAbsolutePath(), "places/folder-workspace", IconSize.SMALL);
				final PhonUIAction selectAction = new PhonUIAction(this, "onSelectFolder", workspaceFolder);
				selectAction.putValue(PhonUIAction.NAME, workspaceFolder.getAbsolutePath());
				selectAction.putValue(PhonUIAction.SMALL_ICON, workspaceIcn);
				builder.addItem(".", selectAction);
			}
		}

		ImageIcon browseIcn =
				IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		if(OSInfo.isMacOs()) {
			ImageIcon finderIcon =
					IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.OpenFolderIcon, IconSize.SMALL);
			if(finderIcon != null) browseIcn = finderIcon;
		} else if(OSInfo.isWindows()) {
			ImageIcon explorerIcon =
					IconManager.getInstance().getSystemStockIcon(WindowsStockIcon.FOLDEROPEN, IconSize.SMALL);
			if(explorerIcon != null) browseIcn = explorerIcon;
		}

		builder.addSeparator(".", "clear");

		JMenu removeItemMenu = builder.addMenu(".", "Remove item from history");
		removeItemMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				removeItemMenu.removeAll();
				Iterator<File> itr = history.iterator();
				while(itr.hasNext()) {
					File f = itr.next();
					PhonUIAction removeAct = new PhonUIAction(WorkspaceButton.this, "removeFromHistory", f);
					removeAct.putValue(PhonUIAction.NAME, f.getAbsolutePath());
					removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove " + f.getAbsolutePath() + " from workspace history");

					removeItemMenu.add(removeAct).setEnabled(!f.equals(Workspace.userWorkspaceFolder()));

				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {}

			@Override
			public void menuCanceled(MenuEvent e) {}
		});

		final PhonUIAction clearHistoryAct = new PhonUIAction(this, "onClearHistory");
		clearHistoryAct.putValue(PhonUIAction.NAME, "Clear history");
		clearHistoryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear workspace history");
		builder.addItem(".@clear", clearHistoryAct);

		builder.addSeparator(".", "browse");

		final Action showWorkspaceAct = createShowWorkspaceAction();
		builder.addItem(".@browse", showWorkspaceAct);

		final SelectWorkspaceCommand cmd = new SelectWorkspaceCommand();
		cmd.putValue(Action.NAME, "Browse for workspace folder...");
		cmd.putValue(Action.SMALL_ICON, browseIcn);
		builder.addItem(".", cmd);


		menu.show(this, 0, getHeight());
	}

	public void removeFromHistory(File f) {
		WorkspaceHistory history = new WorkspaceHistory();
		Iterator<File> itr = history.iterator();
		while(itr.hasNext()) {
			File tf = itr.next();
			if(tf.equals(f)) {
				itr.remove();
				break;
			}
		}
		history.saveHistory();
	}

	public void onCreateWorkspace() {
		File workspaceFolder = Workspace.userWorkspaceFolder();
		if(!workspaceFolder.exists()) {
			boolean created = workspaceFolder.mkdirs();
		}
	}

	public void onShowWorkspace() {
		if(Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(Workspace.userWorkspaceFolder());
			} catch (IOException e) {
				LogUtil.warning(e);
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

	private Action createShowWorkspaceAction() {

		String fsIconName = "apps/system-file-manager";
		String fsName = "file system viewer";

		ImageIcon fsIcon = IconManager.getInstance().getIcon(fsIconName, IconSize.SMALL);
		ImageIcon fsIconL = IconManager.getInstance().getIcon(fsIconName, IconSize.MEDIUM);

		if(OSInfo.isWindows()) {
			fsName = "File Explorer";

			final String explorerPath = "C:\\Windows\\explorer.exe";
			ImageIcon explorerIcon = IconManager.getInstance().getSystemIconForPath(explorerPath, IconSize.SMALL);
			ImageIcon explorerIconL = IconManager.getInstance().getSystemIconForPath(explorerPath, IconSize.MEDIUM);

			if(explorerIcon != null)
				fsIcon = explorerIcon;
			if(explorerIconL != null)
				fsIconL = explorerIconL;
		} else if(OSInfo.isMacOs()) {
			fsName = "Finder";

			ImageIcon finderIcon = IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.FinderIcon, IconSize.SMALL);
			ImageIcon finderIconL = IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.FinderIcon, IconSize.MEDIUM);

			if(finderIcon != null)
				fsIcon = finderIcon;
			if(finderIconL != null)
				fsIconL = finderIconL;
		}

		final PhonUIAction act = new PhonUIAction(this, "onShowWorkspace");
		act.putValue(PhonUIAction.NAME, "Show workspace");
		act.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show workspace folder");
		act.putValue(PhonUIAction.SMALL_ICON, fsIcon);
		act.putValue(PhonUIAction.LARGE_ICON_KEY, fsIconL);

		return act;
	}

	public void onClearHistory() {
		final WorkspaceHistory history = new WorkspaceHistory();
		history.clearHistory();
		history.addToHistory(Workspace.userWorkspaceFolder());
	}

	public void onSelectFolder(File workspaceFolder) {
		Workspace.setUserWorkspaceFolder(workspaceFolder);
	}

	public void onResetWorkspace(PhonActionEvent pae) {
		final File defaultWorkspace = Workspace.defaultWorkspaceFolder();
		Workspace.setUserWorkspaceFolder(defaultWorkspace);
	}

}
