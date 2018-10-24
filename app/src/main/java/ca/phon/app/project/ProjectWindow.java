/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXStatusBar.Constraint.ResizeBehavior;
import org.jdesktop.swingx.MultiSplitLayout;

import ca.hedlund.desktopicons.GtkStockIcon;
import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.NativeUtilities;
import ca.hedlund.desktopicons.StockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.log.LogUtil;
import ca.phon.app.project.actions.AnonymizeAction;
import ca.phon.app.project.actions.CheckTranscriptionsAction;
import ca.phon.app.project.actions.DeleteCorpusAction;
import ca.phon.app.project.actions.DeleteSessionAction;
import ca.phon.app.project.actions.DeriveSessionAction;
import ca.phon.app.project.actions.DuplicateCorpusAction;
import ca.phon.app.project.actions.DuplicateSessionAction;
import ca.phon.app.project.actions.NewCorpusAction;
import ca.phon.app.project.actions.NewSessionAction;
import ca.phon.app.project.actions.OpenCorpusTemplateAction;
import ca.phon.app.project.actions.OpenSessionAction;
import ca.phon.app.project.actions.RefreshAction;
import ca.phon.app.project.actions.RenameCorpusAction;
import ca.phon.app.project.actions.RenameSessionAction;
import ca.phon.app.project.actions.SelectCorpusMediaFolder;
import ca.phon.app.project.actions.SelectProjectMediaFolder;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.app.project.git.actions.CommitAction;
import ca.phon.app.project.git.actions.InitAction;
import ca.phon.app.project.git.actions.PullAction;
import ca.phon.app.project.git.actions.PushAction;
import ca.phon.app.welcome.WelcomeWindow;
import ca.phon.app.welcome.WorkspaceTextStyler;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.project.ProjectListener;
import ca.phon.project.ProjectPath;
import ca.phon.project.ProjectRefresh;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuManager;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;


/**
 * The project window.
 *
 */
public class ProjectWindow extends CommonModuleFrame {

	private static final long serialVersionUID = -4771564010497815447L;

	/** The corpus list */
	private TitledPanel corpusPanel;
	private MultiActionButton createCorpusButton;
	private JTextField corpusNameField;
	private JList<String> corpusList;
	private CorpusListModel corpusModel;
	private CorpusDetails corpusDetails;

	/** The session list */
	private TitledPanel sessionPanel;
	private MultiActionButton createSessionButton;
	private JTextField sessionNameField;
	private JList<String> sessionList;
	private SessionListModel sessionModel;
	private SessionDetails sessionDetails;

	public static final String BLIND_MODE_PROPERTY = ProjectWindow.class.getName() + ".blindMode";
	public static final boolean DEFAULT_BLIND_MODE = false;
	public boolean blindMode =
			PrefHelper.getBoolean(BLIND_MODE_PROPERTY, DEFAULT_BLIND_MODE);
	private JCheckBox blindModeBox;

//	/** Label for messages */
//	private StatusPanel msgPanel;

	private JXStatusBar statusBar;
	private JXBusyLabel busyLabel;
	private JLabel statusLabel;

	/** Project path (used to load the project) */
	private String projectLoadPath = new String();

	private final ProjectListener myProjectListener;

	private ProjectGitController gitController;

	/** Constructor */
	public ProjectWindow(Project project, String projectPath) {
		super("");

		setWindowName("Project Manager");

		putExtension(Project.class, project);
		gitController = new ProjectGitController(project);
		if(gitController.hasGitFolder()) {
			try {
				gitController.open();
			} catch (IOException e) {
				LogUtil.warning(e);
			}
		}

		myProjectListener = new ProjectWindowProjectListener(this);
		project.addProjectListener(myProjectListener);

		this.projectLoadPath = projectPath;

		this.setTitle("Phon : " + project.getName() + " : Project Manager");

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		init();

		final JMenuBar menuBar = MenuManager.createWindowMenuBar(this);
		setJMenuBar(menuBar);
	}

	public MultiActionButton getCorpusButton() {
		return this.createCorpusButton;
	}

	public JList<String> getCorpusList() {
		return this.corpusList;
	}

	public MultiActionButton getSessionButton() {
		return this.createSessionButton;
	}

	public JList<String> getSessionList() {
		return this.sessionList;
	}

	public String getSelectedCorpus() {
		return corpusList.getSelectedValue();
	}

	public List<String> getSelectedCorpora() {
		return corpusList.getSelectedValuesList();
	}

	public String getSelectedSessionName() {
		return sessionList.getSelectedValue();
	}

	public List<String> getSelectedSessionNames() {
		return sessionList.getSelectedValuesList();
	}

	public SessionPath getSelectedSessionPath() {
		return new SessionPath(getSelectedCorpus(), getSelectedSessionName());
	}

	public boolean isBlindMode() {
		return this.blindMode;
	}

	public void setBlindMode(boolean blindMode) {
		this.blindMode = blindMode;
		PrefHelper.getUserPreferences().putBoolean(BLIND_MODE_PROPERTY, blindMode);
	}

	/**
	 * Get the project
	 * @return project
	 */
	public Project getProject() {
		return getExtension(Project.class);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(700, 500);
	}


	@Override
	public void setJMenuBar(JMenuBar menu) {
		super.setJMenuBar(menu);

		JMenu projectMenu = new JMenu("Project");

		int projectMenuIndex = -1;
		// get the edit menu and add view commands
		for(int i = 0; i < menu.getMenuCount(); i++) {
			JMenu currentBar = menu.getMenu(i);

			if(currentBar != null && currentBar.getText() != null && currentBar.getText().equals("Workspace")) {
				projectMenuIndex = i+1;
			}
		}

		if(projectMenuIndex > 0) {
			menu.add(projectMenu, projectMenuIndex);
		}

		// refresh lists
		final RefreshAction refreshItem = new RefreshAction(this);
		projectMenu.add(refreshItem);
		projectMenu.addSeparator();

		// create corpus item
		final NewCorpusAction newCorpusItem = new NewCorpusAction(this);
		projectMenu.add(newCorpusItem);

		//		 create corpus item
		final NewSessionAction newSessionItem = new NewSessionAction(this);
		projectMenu.add(newSessionItem);

		projectMenu.addSeparator();

		projectMenu.add(new SelectProjectMediaFolder(this));
		projectMenu.add(new SelectCorpusMediaFolder(this));

		projectMenu.addSeparator();

		final AnonymizeAction anonymizeParticipantInfoItem = new AnonymizeAction(this);
		projectMenu.add(anonymizeParticipantInfoItem);

		final CheckTranscriptionsAction repairItem = new CheckTranscriptionsAction(this);
		projectMenu.add(repairItem);

		// merge/split sessions
		final DeriveSessionAction deriveItem = new DeriveSessionAction(this);
		projectMenu.add(deriveItem);

		final JMenu teamMenu = new JMenu("Team");
		teamMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				teamMenu.removeAll();
				if(getProject() != null) {
					final ProjectGitController gitController = new ProjectGitController(getProject());
					if(gitController.hasGitFolder()) {
						teamMenu.add(new CommitAction(ProjectWindow.this));

						teamMenu.addSeparator();

						teamMenu.add(new PullAction(ProjectWindow.this));
						teamMenu.add(new PushAction(ProjectWindow.this));

					} else {
						final InitAction initRepoAct =
								new InitAction(ProjectWindow.this);
						teamMenu.add(initRepoAct);
					}
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {

			}

			@Override
			public void menuCanceled(MenuEvent e) {

			}
		});
		projectMenu.addSeparator();
		projectMenu.add(teamMenu);
	}

	private void init() {
		/* Layout */
		setLayout(new BorderLayout());

		final ProjectDataTransferHandler transferHandler = new ProjectDataTransferHandler(this);

		/* Create components */
		createCorpusButton = createCorpusButton();
		createCorpusButton.setVisible(false);
		corpusList = new JList<String>();
		corpusModel = new CorpusListModel(getProject());
		corpusList.setModel(corpusModel);
		corpusList.setCellRenderer(new CorpusListCellRenderer());
		corpusList.setVisibleRowCount(20);
		corpusList.addListSelectionListener( e -> {
			if(getSelectedCorpus() != null) {
				String corpus = getSelectedCorpus();
				sessionModel.setCorpus(corpus);
				sessionList.clearSelection();
				corpusDetails.setCorpus(corpus);
				sessionDetails.setCorpus(corpus);
				sessionDetails.setSession(null);

				if(getProject().getCorpusSessions(corpus).size() == 0) {
					onShowCreateSessionButton();
				} else {
					onHideCreateSessionButton();
				}
			}
		});
		corpusList.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				doPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				doPopup(e);
			}

			public void doPopup(MouseEvent e) {
				if(e.isPopupTrigger()) {
					int clickedIdx = corpusList.locationToIndex(e.getPoint());
					if(clickedIdx >= 0 &&
							Arrays.binarySearch(corpusList.getSelectedIndices(), clickedIdx) < 0) {
						corpusList.setSelectedIndex(clickedIdx);
					}
					showCorpusListContextMenu(e.getPoint());
				}
			}
		});

		final DragSource corpusDragSource = new DragSource();
		corpusDragSource.createDefaultDragGestureRecognizer(corpusList, DnDConstants.ACTION_COPY, (event) -> {
			final List<ProjectPath> paths = new ArrayList<>();
			for(String corpus:getSelectedCorpora()) {
				final ProjectPath corpusPath = new ProjectPath(getProject(), corpus, null);
				paths.add(corpusPath);
			}
			final ProjectPathTransferable transferable = new ProjectPathTransferable(paths);
			event.startDrag(DragSource.DefaultCopyDrop, transferable);
		});

		corpusList.setDragEnabled(true);
		corpusList.setTransferHandler(transferHandler);

		corpusDetails = new CorpusDetails(getProject(), null);
		corpusDetails.setBackground(Color.white);
		corpusDetails.setOpaque(true);
		corpusDetails.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		sessionList = new JList<String>();
		createSessionButton = createSessionButton();
		createSessionButton.setVisible(false);
		sessionModel = new SessionListModel(getProject());
		sessionList.setModel(sessionModel);
		sessionList.setCellRenderer(new SessionListCellRenderer());
		sessionList.setVisibleRowCount(20);
		sessionList.addListSelectionListener( e -> {
			if(sessionList.getSelectedValue() != null && !e.getValueIsAdjusting()) {
				String corpus = getSelectedCorpus();
				String session = getSelectedSessionName();

				// clear details if more than one session is selected
				if(sessionList.getSelectedIndices().length > 1)
					session = null;

				sessionDetails.setSession(corpus, session);
			}
		});
		sessionList.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 &&
						e.getButton() == 1) {
					// get the clicked item
					int clickedItem = sessionList.locationToIndex(e.getPoint());
					if(sessionList.getModel().getElementAt(clickedItem) == null)
						return;

					final String session =
						sessionList.getModel().getElementAt(clickedItem).toString();
					final String corpus =
						((SessionListModel)sessionList.getModel()).getCorpus();

					busyLabel.setBusy(true);
					statusLabel.setText("Opening " + corpus + "." + session + "...");

					PhonWorker.getInstance().invokeLater(() -> {
						final ActionEvent ae = new ActionEvent(sessionList, -1, "openSession");
						(new OpenSessionAction(ProjectWindow.this, corpus, session)).actionPerformed(ae);

						SwingUtilities.invokeLater( () -> {
							statusLabel.setText("");
							busyLabel.setBusy(false);
						});
					});
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				doPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				doPopup(e);
			}

			public void doPopup(MouseEvent e) {
				if(e.isPopupTrigger()) {
					int clickedIdx = sessionList.locationToIndex(e.getPoint());
					if(clickedIdx >= 0 &&
							Arrays.binarySearch(sessionList.getSelectedIndices(), clickedIdx) < 0) {
						sessionList.setSelectedIndex(clickedIdx);
					}
					showSessionListContextMenu(e.getPoint());
				}
			}
		});

		sessionList.setDragEnabled(true);
		sessionList.setTransferHandler(transferHandler);

		final ActionMap sessionListAM = sessionList.getActionMap();
		final InputMap sessionListIM = sessionList.getInputMap(JComponent.WHEN_FOCUSED);

		final PhonUIAction openSessionAct = new PhonUIAction(this, "onOpenSelectedSession");
		sessionListAM.put("openSelectedSession", openSessionAct);
		sessionListIM.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openSelectedSession");

		sessionList.setActionMap(sessionListAM);
		sessionList.setInputMap(JComponent.WHEN_FOCUSED, sessionListIM);

		final DragSource sessionDragSource = new DragSource();
		sessionDragSource.createDefaultDragGestureRecognizer(sessionList, DnDConstants.ACTION_COPY, (event) -> {
			final List<ProjectPath> paths = new ArrayList<>();
			final String corpus = getSelectedCorpus();
			if(corpus == null) return;
			for(String session:getSelectedSessionNames()) {
				final ProjectPath sessionPath = new ProjectPath(getProject(), corpus, session);
				paths.add(sessionPath);
			}
			final ProjectPathTransferable transferable = new ProjectPathTransferable(paths);
			event.startDrag(DragSource.DefaultCopyDrop, transferable);
		});

		sessionDetails = new SessionDetails(getProject());
		sessionDetails.setBackground(Color.white);
		sessionDetails.setOpaque(true);
		sessionDetails.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		JScrollPane corpusScroller = new JScrollPane(corpusList);
		JScrollPane sessionScroller = new JScrollPane(sessionList);

		final PhonUIAction showCreateCorpusAct = new PhonUIAction(this, "onShowCreateCorpusButton");
		showCreateCorpusAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "New corpus...");
		showCreateCorpusAct.putValue(PhonUIAction.SMALL_ICON,
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL));
		final JButton showCreateCorpusBtn = new JButton(showCreateCorpusAct);
		showCreateCorpusBtn.setMargin(new Insets(0, 0, 0, 0));
		showCreateCorpusBtn.setOpaque(false);
		showCreateCorpusBtn.setBorderPainted(false);

		corpusPanel = new TitledPanel("Corpus");
		final ImageIcon defCorpusIcn = IconManager.getInstance().getSystemStockIcon(
				(OSInfo.isMacOs() ? MacOSStockIcon.GenericFolderIcon :
					OSInfo.isWindows() ? WindowsStockIcon.FOLDER : GtkStockIcon.FOLDER), IconSize.SMALL);
		corpusPanel.setIcon(defCorpusIcn);

		corpusPanel.getContentContainer().add(createCorpusButton, BorderLayout.NORTH);
		corpusPanel.getContentContainer().add(corpusScroller, BorderLayout.CENTER);
		corpusPanel.setRightDecoration(showCreateCorpusBtn);

		final PhonUIAction showCreateSessionAct = new PhonUIAction(this, "onShowCreateSessionButton");
		showCreateSessionAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "New session...");
		showCreateSessionAct.putValue(PhonUIAction.SMALL_ICON,
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL));
		final JButton showCreateSessionBtn = new JButton(showCreateSessionAct);
		showCreateSessionBtn.setMargin(new Insets(0, 0, 0, 0));
		showCreateSessionBtn.setOpaque(false);
		showCreateSessionBtn.setBorderPainted(false);

		blindModeBox = new JCheckBox("Blind mode");
		blindModeBox.setOpaque(false);
		blindModeBox.setMargin(new Insets(0, 0, 0, 0));
		blindModeBox.setSelected(this.blindMode);
		blindModeBox.addActionListener( (e) -> setBlindMode(blindModeBox.isSelected()) );

		final JPanel sessionDecoration = new JPanel(new HorizontalLayout());
		sessionDecoration.setOpaque(false);
		sessionDecoration.add(showCreateSessionBtn);

		sessionPanel = new TitledPanel("Session");
		sessionPanel.setIcon(IconManager.getInstance().getSystemIconForFileType(
				(OSInfo.isNix() ? "text-xml" : "xml"), IconSize.SMALL));
		sessionPanel.setRightDecoration(sessionDecoration);
		sessionPanel.getContentContainer().add(createSessionButton, BorderLayout.NORTH);
		sessionPanel.getContentContainer().add(sessionScroller, BorderLayout.CENTER);

		final JXCollapsiblePane bottomPanel = new JXCollapsiblePane(Direction.UP);
		bottomPanel.setLayout(new GridLayout(1, 2));
		bottomPanel.add(corpusDetails);
		bottomPanel.add(sessionDetails);

		final TitledPanel detailsPanel = new TitledPanel("Details", bottomPanel);
		detailsPanel.setIcon(IconManager.getInstance().getIcon("categories/info-white", IconSize.SMALL));

		final JXMultiSplitPane multiSplitPane = new JXMultiSplitPane();
		final String multiSplitLayout = "(COLUMN "
				+ "(ROW weight=1.0 (LEAF weight=0.5 name=corpus) (LEAF weight=0.5 name=session) ) "
				+ "(LEAF weight=0.0 name=details))";
		final MultiSplitLayout.Node rootLayoutNode = MultiSplitLayout.parseModel(multiSplitLayout);

		corpusPanel.setPreferredSize(new Dimension(500, 600));
		sessionPanel.setPreferredSize(new Dimension(500, 600));

		multiSplitPane.setModel(rootLayoutNode);
		multiSplitPane.setDividerSize(2);
		multiSplitPane.add(corpusPanel, "corpus");
		multiSplitPane.add(sessionPanel, "session");
		multiSplitPane.add(detailsPanel, "details");

		statusBar = new JXStatusBar();
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		statusBar.add(busyLabel, new JXStatusBar.Constraint(16));

		statusLabel = new JLabel();
		statusBar.add(statusLabel, new JXStatusBar.Constraint(ResizeBehavior.FILL));
		statusBar.add(blindModeBox, new JXStatusBar.Constraint(ResizeBehavior.FIXED));

		String projectName = null;
		projectName = getProject().getName();

		DialogHeader header = new DialogHeader(projectName,
				"<html><u style='color: rgb(0, 90, 140);'>" + StringUtils.abbreviate(projectLoadPath, 80) + "</u></html>");
		header.getBottomLabel().setToolTipText(projectLoadPath);
		header.getBottomLabel().setForeground(Color.blue);
		header.getBottomLabel().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent me) {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(new File(projectLoadPath));
					} catch (IOException e) {
						LogUtil.warning(e);
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}

		});
		header.getBottomLabel().setIcon(IconManager.getInstance().getSystemIconForPath(projectLoadPath, IconSize.MEDIUM));
		header.getBottomLabel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		add(header, BorderLayout.NORTH);
		add(multiSplitPane, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);

		// if no corpora are currently available, 'prompt' the user to create a new one
		if(getProject().getCorpora().size() == 0) {
			SwingUtilities.invokeLater( () -> {
				onShowCreateCorpusButton();
			});
		} else {
			SwingUtilities.invokeLater( () -> {
				corpusList.setSelectedIndex(0);
				corpusList.requestFocusInWindow();
			});
		}
	}

	public void onOpenSelectedSession(PhonActionEvent pae) {
		final PhonWorker worker = PhonWorker.createWorker();
		busyLabel.setBusy(true);

		for(int selectedIdx:sessionList.getSelectedIndices()) {
			final String session = ((SessionListModel)sessionList.getModel()).getSessions().get(selectedIdx);
			final String corpus =
				((SessionListModel)sessionList.getModel()).getCorpus();

			worker.invokeLater( () -> {
				try {
					SwingUtilities.invokeAndWait( () -> statusLabel.setText("Opening " + corpus + "." + session + "...") );
				} catch (InvocationTargetException | InterruptedException e) {
				}
			});

			worker.invokeLater(() -> {
				final ActionEvent ae = new ActionEvent(sessionList, -1, "openSession");
				(new OpenSessionAction(ProjectWindow.this, corpus, session)).actionPerformed(ae);
			});
		}

		worker.invokeLater( () -> {;
			SwingUtilities.invokeLater( () -> {
				statusLabel.setText("");
				busyLabel.setBusy(false);
			});
		});
		worker.start();
	}

	public void onRenameCorpus(PhonActionEvent pae) {
		if(getSelectedCorpus() == null) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Please select a corpus").start(corpusList);
			return;
		}
		(new RenameCorpusAction(this)).actionPerformed(pae.getActionEvent());
	}

	private ImageIcon createNewCorpusIcon() {
		final String folderIconName = "actions/folder_new";
		final StockIcon stockIcon =
				(NativeUtilities.isMacOs() ? MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.FOLDER);
		final ImageIcon folderIcon =
				IconManager.getInstance().getSystemStockIcon(stockIcon, folderIconName, IconSize.MEDIUM);
		final ImageIcon addIcon =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);

		final BufferedImage newIcnImg =
				new BufferedImage(IconSize.MEDIUM.getHeight(), IconSize.MEDIUM.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
		final Graphics g = newIcnImg.createGraphics();
		folderIcon.paintIcon(null, g, 0, 0);
		g.drawImage(addIcon.getImage(), IconSize.MEDIUM.getWidth() - IconSize.XSMALL.getWidth(),
				IconSize.MEDIUM.getHeight() - IconSize.XSMALL.getHeight(), this);
		return new ImageIcon(newIcnImg);
	}

	private MultiActionButton createCorpusButton() {
		MultiActionButton retVal = new MultiActionButton();

		final ImageIcon folderNewIcn = createNewCorpusIcon();
		String s1 = "New Corpus";
		String s2 = "Enter corpus name and press enter.  Press escape to cancel.";

		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(folderNewIcn);
		retVal.setAlwaysDisplayActions(true);

		retVal.setOpaque(false);

		ImageIcon cancelIcn = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		ImageIcon cancelIcnL = cancelIcn;

		PhonUIAction btnSwapAct = new PhonUIAction(this, "onHideCreateCorpusButton");
		btnSwapAct.putValue(Action.ACTION_COMMAND_KEY, "CANCEL_CREATE_ITEM");
		btnSwapAct.putValue(Action.NAME, "Cancel create");
		btnSwapAct.putValue(Action.SHORT_DESCRIPTION, "Cancel create");
		btnSwapAct.putValue(Action.SMALL_ICON, cancelIcn);
		btnSwapAct.putValue(Action.LARGE_ICON_KEY, cancelIcnL);
		retVal.addAction(btnSwapAct);

		JPanel corpusNamePanel = new JPanel(new BorderLayout());
		corpusNamePanel.setOpaque(false);

		corpusNameField = new JTextField();
		corpusNameField.setDocument(new NameDocument());
		corpusNameField.setText("Corpus Name");
		corpusNamePanel.add(corpusNameField, BorderLayout.CENTER);

		ActionMap actionMap = retVal.getActionMap();
		actionMap.put(btnSwapAct.getValue(Action.ACTION_COMMAND_KEY), btnSwapAct);
		InputMap inputMap = retVal.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);

		inputMap.put(ks, btnSwapAct.getValue(Action.ACTION_COMMAND_KEY));

		retVal.setActionMap(actionMap);
		retVal.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);

		PhonUIAction createNewCorpusAct =
			new PhonUIAction(this, "onCreateCorpus", corpusNameField);
		createNewCorpusAct.putValue(Action.SHORT_DESCRIPTION, "Create new corpus folder");
		createNewCorpusAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL));

		JButton createBtn = new JButton(createNewCorpusAct);
		corpusNamePanel.add(createBtn, BorderLayout.EAST);

		corpusNameField.setAction(createNewCorpusAct);

		// swap bottom component in new project button
		retVal.setBottomLabelText(WorkspaceTextStyler.toDescText(s2));
		retVal.add(corpusNamePanel, BorderLayout.CENTER);

		retVal.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
				corpusNameField.requestFocus();
			}
		});

		return retVal;
	}

	public void onShowCreateCorpusButton() {
		createCorpusButton.setVisible(true);
		corpusPanel.revalidate();

		corpusNameField.requestFocusInWindow();
		corpusNameField.selectAll();
	}

	public void onHideCreateCorpusButton() {
		createCorpusButton.setVisible(false);
		corpusPanel.revalidate();
	}

	public void onCreateCorpus(PhonActionEvent pae) {
		final JTextField textField = (JTextField)pae.getData();
		final String corpusName = textField.getText().trim();
		if(corpusName.length() == 0) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Corpus name cannot be empty").start(textField);
			return;
		}

		final NewCorpusAction newCorpusAct = new NewCorpusAction(this, corpusName);
		newCorpusAct.actionPerformed(pae.getActionEvent());
		if(newCorpusAct.isCorpusCreated()) {
			onHideCreateCorpusButton();
			corpusList.setSelectedValue(corpusName, true);
		}
	}

	private ImageIcon createNewSessionIcon() {
		final String defaultIconName = "mimetypes/text-xml";
		final String type = (NativeUtilities.isLinux() ? "text-xml" : "xml");
		final ImageIcon xmlIcon =
				IconManager.getInstance().getSystemIconForFileType(type, defaultIconName, IconSize.MEDIUM);
		final ImageIcon addIcon =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);

		final BufferedImage newIcnImg =
				new BufferedImage(IconSize.MEDIUM.getHeight(), IconSize.MEDIUM.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
		final Graphics g = newIcnImg.createGraphics();
		xmlIcon.paintIcon(null, g, 0, 0);
		g.drawImage(addIcon.getImage(), IconSize.MEDIUM.getWidth() - IconSize.XSMALL.getWidth(),
				IconSize.MEDIUM.getHeight() - IconSize.XSMALL.getHeight(), this);
		final ImageIcon xmlNewIcn = new ImageIcon(newIcnImg);
		return xmlNewIcn;
	}

	public void onShowCreateSessionButton() {
		createSessionButton.setVisible(true);
		sessionPanel.getContentContainer().revalidate();

		sessionNameField.requestFocusInWindow();
		sessionNameField.selectAll();
	}

	public void onHideCreateSessionButton() {
		createSessionButton.setVisible(false);
		sessionPanel.getContentContainer().revalidate();
	}

	private MultiActionButton createSessionButton() {
		MultiActionButton retVal = new MultiActionButton();

		final ImageIcon xmlNewIcn = createNewSessionIcon();

		String s1 = "New Session";
		String s2 = "Enter session name and press enter.  Press escape to cancel.";

		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(xmlNewIcn);
		retVal.setAlwaysDisplayActions(true);

		retVal.setOpaque(false);

		ImageIcon cancelIcn = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		ImageIcon cancelIcnL = cancelIcn;

		PhonUIAction btnSwapAct = new PhonUIAction(this, "onHideCreateSessionButton");
		btnSwapAct.putValue(Action.ACTION_COMMAND_KEY, "CANCEL_CREATE_ITEM");
		btnSwapAct.putValue(Action.NAME, "Cancel create");
		btnSwapAct.putValue(Action.SHORT_DESCRIPTION, "Cancel create");
		btnSwapAct.putValue(Action.SMALL_ICON, cancelIcn);
		btnSwapAct.putValue(Action.LARGE_ICON_KEY, cancelIcnL);
		retVal.addAction(btnSwapAct);

		JPanel sessionNamePanel = new JPanel(new BorderLayout());
		sessionNamePanel.setOpaque(false);

		sessionNameField = new JTextField();
		sessionNameField.setDocument(new NameDocument());
		sessionNameField.setText("Session Name");
		sessionNamePanel.add(sessionNameField, BorderLayout.CENTER);

		ActionMap actionMap = retVal.getActionMap();
		actionMap.put(btnSwapAct.getValue(Action.ACTION_COMMAND_KEY), btnSwapAct);
		InputMap inputMap = retVal.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);

		inputMap.put(ks, btnSwapAct.getValue(Action.ACTION_COMMAND_KEY));

		retVal.setActionMap(actionMap);
		retVal.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);

		PhonUIAction createNewSessionAct =
			new PhonUIAction(this, "onCreateSession", sessionNameField);
		createNewSessionAct.putValue(Action.SHORT_DESCRIPTION, "Create new session in selected corpus");
		createNewSessionAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL));

		JButton createBtn = new JButton(createNewSessionAct);
		sessionNamePanel.add(createBtn, BorderLayout.EAST);

		sessionNameField.setAction(createNewSessionAct);

		// swap bottom component in new project button
		retVal.setBottomLabelText(WorkspaceTextStyler.toDescText(s2));
		retVal.add(sessionNamePanel, BorderLayout.CENTER);

		retVal.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {

			}

			@Override
			public void focusGained(FocusEvent e) {
				sessionNameField.requestFocus();
			}
		});

		return retVal;
	}

	public void onCreateSession(PhonActionEvent pae) {
		final JTextField textField = (JTextField)pae.getData();
		final String sessionName = textField.getText().trim();
		if(sessionName.length() == 0) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Session name cannot be empty").start(textField);
			return;
		}

		final NewSessionAction newSessionAct = new NewSessionAction(this, getSelectedCorpus(), sessionName);
		newSessionAct.actionPerformed(pae.getActionEvent());
		if(newSessionAct.isSessionCreated()) {
			onHideCreateSessionButton();
			SwingUtilities.invokeLater( () -> {
				sessionList.setSelectedValue(sessionName, true);
				sessionList.requestFocus();
			});
		}
	}

	/**
	 * Displays the corpus list menu
	 *
	 * @param clickPoint
	 */
	private void showCorpusListContextMenu(Point clickPoint) {
		List<String> corpora = getSelectedCorpora();

		JPopupMenu contextMenu = new JPopupMenu();

		if(corpora.size() == 1) {
			// new session item
			JMenuItem newSessionItem = new JMenuItem(new NewSessionAction(this));
			contextMenu.add(newSessionItem);

			contextMenu.addSeparator();

			JMenuItem templateItem = new JMenuItem(new OpenCorpusTemplateAction(this));
			contextMenu.add(templateItem);

			contextMenu.addSeparator();
		}

		JMenuItem dupItem = new JMenuItem(new DuplicateCorpusAction(this));
		if(corpora.size() > 1) {
			dupItem.setText("Duplicate Corpora");
		}
		contextMenu.add(dupItem);

		if(corpora.size() == 1) {
			// rename
			JMenuItem renameItem = new JMenuItem(new RenameCorpusAction(this));
			contextMenu.add(renameItem);
		}

		// delete
		JMenuItem deleteItem = new JMenuItem(new DeleteCorpusAction(this));
		if(corpora.size() > 1) {
			deleteItem.setText("Delete Corpora");
		}
		contextMenu.add(deleteItem);

		contextMenu.addSeparator();

		contextMenu.add(new SelectProjectMediaFolder(this));
		if(corpora.size() > 0) {
			contextMenu.add(new SelectCorpusMediaFolder(this));
		}

		contextMenu.show(corpusList, clickPoint.x, clickPoint.y);
	}

	/**
	 * Displays the session list menu
	 *
	 * @param clickPoint
	 */
	private void showSessionListContextMenu(Point clickPoint) {
		List<String> selectedSessions = getSelectedSessionNames();

		JPopupMenu contextMenu = new JPopupMenu();

		if(selectedSessions.size() == 1) {
			// open item
			JMenuItem openItem = new JMenuItem(new OpenSessionAction(this));
			contextMenu.add(openItem);

			contextMenu.addSeparator();
		}

		// rename item
		JMenuItem duplicateItem = new JMenuItem(new DuplicateSessionAction(this));
		if(selectedSessions.size() > 1) {
			duplicateItem.setText("Duplicate Sessions");
		}
		contextMenu.add(duplicateItem);

		if(selectedSessions.size() == 1) {
			JMenuItem renameItem = new JMenuItem(new RenameSessionAction(this));
			contextMenu.add(renameItem);
		}

		// delete item
		JMenuItem deleteItem = new JMenuItem(new DeleteSessionAction(this));
		if(selectedSessions.size() > 1) {
			deleteItem.setText("Delete Sessions");
		}
		contextMenu.add(deleteItem);


		contextMenu.addSeparator();

		contextMenu.add(new SelectProjectMediaFolder(this));
		contextMenu.add(new SelectCorpusMediaFolder(this));

		contextMenu.show(sessionList, clickPoint.x, clickPoint.y);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void updateLists() {
		CorpusListModel corpusListModel =
			(CorpusListModel)corpusList.getModel();
		corpusListModel.refresh();
		corpusList.repaint();

		if(getSelectedCorpus() != null)
			corpusDetails.setCorpus(getSelectedCorpus());

		SessionListModel sessionListModel =
			(SessionListModel)sessionList.getModel();
		sessionListModel.refresh();
		if(corpusList.getSelectedIndex() >= 0 && corpusList.getSelectedIndex() < corpusList.getModel().getSize()
				&& corpusList.getSelectedValue() != null)
			sessionListModel.setCorpus(corpusList.getSelectedValue().toString());
		else
			sessionListModel.setCorpus(null);
		sessionList.repaint();
	}

	public void refreshProject() {
		final Project project = getProject();
		final ProjectRefresh impl = project.getExtension(ProjectRefresh.class);
		if(impl != null) {
			impl.refresh();
			updateLists();
		}
	}

	private class CorpusListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			JLabel comp = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			final String corpus = comp.getText();
			final String corpusPath = getProject().getCorpusPath(corpus);

			ImageIcon icon = IconManager.getInstance().getSystemIconForPath(corpusPath, IconSize.SMALL);

			if(gitController.hasGitFolder() && gitController.isOpen()) {
				try {
					final Status status = gitController.status(corpus);

					if(status.hasUncommittedChanges() || status.getUntracked().size() > 0) {
						ImageIcon modifiedIcn =
								IconManager.getInstance().createGlyphIcon('*',
										FontPreferences.getTitleFont(), comp.getForeground(), comp.getBackground());
						icon =
								IconManager.getInstance().createIconStrip(new ImageIcon[] { icon, modifiedIcn });
					}
				} catch (NoWorkTreeException | GitAPIException e) {
					LogUtil.warning(e);
				}
			}
			comp.setIcon(icon);

			return comp;
		}

	}

	private class SessionListCellRenderer extends DefaultListCellRenderer {

			private static final long serialVersionUID = 576253657524546120L;

			@Override
			public Component getListCellRendererComponent(
					JList list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				JLabel comp = (JLabel)
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				final String corpus = getCorpusList().getSelectedValue();
				final String session = comp.getText();

				final String projectLocation = getProject().getLocation();
				final String sessionLocation = getProject().getSessionPath(corpus, session);

				// get relative path for session
				final Path projectPath = FileSystems.getDefault().getPath(projectLocation);
				final Path sessionPath = FileSystems.getDefault().getPath(sessionLocation);
				final Path relPath = projectPath.relativize(sessionPath);
				final String sessionRelPath = relPath.toString();

			    ImageIcon icon =
			    		IconManager.getInstance().getSystemIconForPath(getProject().getSessionPath(corpus, session), IconSize.SMALL);

				if(gitController.hasGitFolder() && gitController.isOpen()) {
					try {
						final Status status = gitController.status(sessionRelPath);

						if(status.hasUncommittedChanges()) {
							ImageIcon modifiedIcn =
									IconManager.getInstance().createGlyphIcon('*', FontPreferences.getTitleFont(), comp.getForeground(), comp.getBackground());
							icon =
									IconManager.getInstance().createIconStrip(new ImageIcon[] { icon, modifiedIcn });
						} else if(status.getUntracked().contains(sessionRelPath)) {
							ImageIcon modifiedIcn =
									IconManager.getInstance().createGlyphIcon('?', FontPreferences.getTitleFont(), comp.getForeground(), comp.getBackground());
							icon =
									IconManager.getInstance().createIconStrip(new ImageIcon[] { icon, modifiedIcn });
						} else if(status.getConflicting().contains(sessionRelPath)) {
							ImageIcon modifiedIcn =
									IconManager.getInstance().createGlyphIcon('C', FontPreferences.getTitleFont(), comp.getForeground(), comp.getBackground());
							icon =
									IconManager.getInstance().createIconStrip(new ImageIcon[] { icon, modifiedIcn });
						}
					} catch (NoWorkTreeException | GitAPIException e) {
						LogUtil.warning(e);
					}
				}

				// see if the transcript it locked...
				SessionListModel model = (SessionListModel)list.getModel();
				if(model.getProject().isSessionLocked(model.getCorpus(), value.toString())) {
					comp.setIcon(
							IconManager.getInstance().getIcon("emblems/emblem-readonly", IconSize.SMALL));
				}

				comp.setIcon(icon);

				return comp;
			}
		}

	public class NameDocument extends PlainDocument {
		/**
		 * Ensure proper project names.
		 *
		 * Project name must start with a letter, and can be followed
		 * by at most 30 letters, numbers, underscores, dashes.
		 */
		private String projectRegex = "[a-zA-Z0-9][- a-zA-Z_0-9]{0,29}";

		@Override
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
			// only allow valid inputs
			String p1 = super.getText(0, offs);
			String p2 = super.getText(offs, getLength()-offs);
			String val = p1 + str + p2;

			if(val.matches(projectRegex)) {
				super.insertString(offs, str, a);
			}
		}
	}

}
