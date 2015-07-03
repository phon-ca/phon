/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
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
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXBusyLabel;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.actions.AnonymizeAction;
import ca.phon.app.project.actions.CheckTranscriptionsAction;
import ca.phon.app.project.actions.DeriveSessionAction;
import ca.phon.app.project.actions.NewCorpusAction;
import ca.phon.app.project.actions.NewSessionAction;
import ca.phon.app.project.actions.RefreshAction;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.app.project.git.actions.CommitAction;
import ca.phon.app.project.git.actions.InitAction;
import ca.phon.app.project.git.actions.PullAction;
import ca.phon.app.project.git.actions.PushAction;
import ca.phon.app.workspace.WorkspaceDialog;
import ca.phon.app.workspace.WorkspaceTextStyler;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.project.ProjectListener;
import ca.phon.project.ProjectRefresh;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MenuManager;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.CollatorFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;
import ca.phon.workspace.Workspace;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * The project window.
 *
 */
public class ProjectWindow extends CommonModuleFrame
	implements WindowListener {
	
	private static final Logger LOGGER = Logger.getLogger(ProjectWindow.class.getName());
	
	private static final long serialVersionUID = -4771564010497815447L;
	
	/** The corpus list */
	private JPanel corpusPanel;
	private MultiActionButton newCorpusButton;
	private MultiActionButton createCorpusButton;
	private JList<String> corpusList;
	private CorpusListModel corpusModel;
	private CorpusDetailsPane corpusDetails;
	
	/** The session list */
	private JPanel sessionPanel;
	private MultiActionButton newSessionButton;
	private MultiActionButton createSessionButton;
	private JList<String> sessionList;
	private SessionListModel sessionModel;
	private SessionDetailsPane sessionDetails;
	
	/** The checkbox */
	private JCheckBox blindModeBox;
	
	/** Label for messages */
	private StatusPanel msgPanel;
	
	/** Project path (used to load the project) */
	private String projectLoadPath = new String();
	
	private final ProjectListener myProjectListener;
	
	/** Constructor */
	public ProjectWindow(Project project, String projectPath) {
		super("");
		
		setWindowName("Project Manager");
		
		putExtension(Project.class, project);
		
		myProjectListener = new ProjectWindowProjectListener(project);
		
		project.addProjectListener(myProjectListener);
		
		
		this.projectLoadPath = projectPath;
		
		this.addWindowListener(this);
		this.setTitle("Phon : " + project.getName() + " : Project Manager");
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		init();
		
		final JMenuBar menuBar = MenuManager.createWindowMenuBar(this);
		setJMenuBar(menuBar);
	}
	
	public String getSelectedCorpus() {
		return corpusList.getSelectedValue();
	}
	
	public String getSelectedSessionName() {
		return sessionList.getSelectedValue();
	}
	
	public SessionPath getSelectedSessionPath() {
		return new SessionPath(getSelectedCorpus(), getSelectedSessionName());
	}
	
	/**
	 * Get the project
	 * @return project
	 */
	public Project getProject() {
		return getExtension(Project.class);
	}
	
	public boolean isRemoteProject() {
		return this.projectLoadPath.startsWith("rmi://");
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
		
		/* Create components */
		newCorpusButton = createNewCorpusButton();
		createCorpusButton = createCorpusButton();
		corpusList = new JList<String>();
		corpusModel = new CorpusListModel(getProject());
		corpusList.setModel(corpusModel);
		corpusList.setCellRenderer(new CorpusListCellRenderer());
		corpusList.setVisibleRowCount(20);
		corpusList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if(corpusList.getSelectedValue() != null) {
					String corpus = 
						corpusList.getSelectedValue().toString();
					sessionModel.setCorpus(corpus);
					corpusDetails.setCorpus(corpus);
					
					if(getProject().getCorpusSessions(corpus).size() == 0) {
						onSwapNewAndCreateSession(newSessionButton);
					} else {
						onSwapNewAndCreateSession(createSessionButton);
					}
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
					if(corpusList.locationToIndex(e.getPoint()) >= 0)
						corpusList.setSelectedIndex(corpusList.locationToIndex(e.getPoint()));
					showCorpusListContextMenu(e.getPoint());
				}
			}
		});
		
		corpusDetails = new CorpusDetailsPane(getProject());
		corpusDetails.setWrapStyleWord(true);
		corpusDetails.setRows(6);
		corpusDetails.setLineWrap(true);
		corpusDetails.setBackground(Color.white);
		corpusDetails.setOpaque(true);
		JScrollPane corpusDetailsScroller = new JScrollPane(corpusDetails);
		
		sessionList = new JList<String>();
		newSessionButton = createNewSessionButton();
		createSessionButton = createSessionButton();
		sessionModel = new SessionListModel(getProject());
		sessionList.setModel(sessionModel);
		sessionList.setCellRenderer(new SessionListCellRenderer());
		sessionList.setVisibleRowCount(20);
		sessionList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if(sessionList.getSelectedValue() != null && !e.getValueIsAdjusting()) {
					String corpus = sessionModel.getCorpus();
					String session = sessionList.getSelectedValue().toString();
					
					sessionDetails.setSession(corpus, session);
				}
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
					
					msgPanel.reset();
					msgPanel.setMessageLabel("Opening '" + corpus + "." + session + "'");
					msgPanel.setIndeterminate(true);
					Runnable th = new Runnable() {
						public void run() {
							openSession(corpus, session);
							msgPanel.setIndeterminate(false);
						}
					};
					PhonWorker.getInstance().invokeLater(th);
					
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
					if(sessionList.locationToIndex(e.getPoint()) >= 0)
						sessionList.setSelectedIndex(sessionList.locationToIndex(e.getPoint()));
					showSessionListContextMenu(e.getPoint());
				}
			}
		});
		
		sessionDetails = new SessionDetailsPane(getProject());
		sessionDetails.setLineWrap(true);
		sessionDetails.setRows(6);
		sessionDetails.setWrapStyleWord(true);
		sessionDetails.setBackground(Color.white);
		sessionDetails.setOpaque(true);
		JScrollPane sessionDetailsScroller = new JScrollPane(sessionDetails);
		
		JScrollPane corpusScroller = new JScrollPane(corpusList);
		JScrollPane sessionScroller = new JScrollPane(sessionList);
		
		blindModeBox = new JCheckBox("Blind transcription");
		blindModeBox.setSelected(false);
		
		msgPanel = new StatusPanel();
		
		corpusPanel = new JPanel(new BorderLayout());
		corpusPanel.add(newCorpusButton, BorderLayout.NORTH);
		corpusPanel.add(corpusScroller, BorderLayout.CENTER);
		corpusPanel.add(corpusDetailsScroller, BorderLayout.SOUTH);
		
		sessionPanel = new JPanel(new BorderLayout());
		sessionPanel.add(newSessionButton, BorderLayout.NORTH);
		sessionPanel.add(sessionScroller, BorderLayout.CENTER);
		sessionPanel.add(sessionDetailsScroller, BorderLayout.SOUTH);
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(corpusPanel);
		splitPane.setRightComponent(sessionPanel);
		splitPane.setResizeWeight(0.5);
		
		// invoke later
		SwingUtilities.invokeLater( () -> {
			splitPane.setDividerLocation(0.5);
		});
		
		// the frame layout
		String projectName = null;
		projectName = getProject().getName();
		
		DialogHeader header = new DialogHeader(projectName,
				StringUtils.abbreviate(projectLoadPath, 80));
		
		add(header, BorderLayout.NORTH);

		CellConstraints cc = new CellConstraints();
		final JPanel topPanel = new JPanel(new FormLayout("pref, fill:pref:grow, right:pref", "pref"));
		topPanel.add(msgPanel, cc.xy(1,1));
		topPanel.add(blindModeBox, cc.xy(3, 1));
		
		add(splitPane, BorderLayout.CENTER);
		add(topPanel, BorderLayout.SOUTH);
		
		// if no corpora are currently available, 'prompt' the user to create a new one
		if(getProject().getCorpora().size() == 0) {
			SwingUtilities.invokeLater( () -> {
				onSwapNewAndCreateCorpus(newCorpusButton);
			});
		} else {
			SwingUtilities.invokeLater( () -> {
				corpusList.setSelectedIndex(0);
			});
		}
	}
	
	private MultiActionButton createNewCorpusButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon folderNewIcn = IconManager.getInstance().getIcon("places/folder", IconSize.SMALL);
		ImageIcon newIcnL = IconManager.getInstance().getIcon("actions/list-add", IconSize.MEDIUM);
		ImageIcon removeIcnL = IconManager.getInstance().getIcon("actions/list-remove", IconSize.MEDIUM);
		ImageIcon renameIcnL = IconManager.getInstance().getIcon("actions/edit-rename", IconSize.MEDIUM);
		
		String s1 = "Corpus";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(folderNewIcn);
//		retVal.getBottomLabel().setText(WorkspaceTextStyler.toDescText(s2));
		retVal.setOpaque(false);
		
		PhonUIAction newAct = new PhonUIAction(this, "onSwapNewAndCreateCorpus", retVal);
		newAct.putValue(Action.LARGE_ICON_KEY, newIcnL);
		newAct.putValue(Action.SMALL_ICON, folderNewIcn);
		newAct.putValue(Action.NAME, "New corpus");
		newAct.putValue(Action.SHORT_DESCRIPTION, "Create a new corpus folder");
		
		PhonUIAction deleteCurrentAct = new PhonUIAction(this, "onDeleteCorpus");
		deleteCurrentAct.putValue(Action.LARGE_ICON_KEY, removeIcnL);
		deleteCurrentAct.putValue(Action.NAME, "Delete corpus");
		deleteCurrentAct.putValue(Action.SHORT_DESCRIPTION, "Delete selected corpus");
		
		PhonUIAction renameCurrentAct = new PhonUIAction(this, "onRenameCorpus");
		renameCurrentAct.putValue(Action.LARGE_ICON_KEY, renameIcnL);
		renameCurrentAct.putValue(Action.NAME, "Rename corpus");
		renameCurrentAct.putValue(Action.SHORT_DESCRIPTION, "Rename selected corpus");
		
		retVal.setDisplayDefaultAction(true);
		retVal.addAction(deleteCurrentAct);
		retVal.addAction(renameCurrentAct);
		
		retVal.setDefaultAction(newAct);
				
		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		return retVal;
	}
	
	public void onDeleteCorpus() {
		if(getSelectedCorpus() == null) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Please select a corpus").start(corpusList);
			return;
		}
		deleteCorpus(getSelectedCorpus());
	}
	
	public void onRenameCorpus() {
		if(getSelectedCorpus() == null) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Please select a corpus").start(corpusList);
			return;
		}
		renameCorpus(getSelectedCorpus());
	}
	
	private MultiActionButton createCorpusButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon newIcn = IconManager.getInstance().getIcon("places/folder", IconSize.SMALL);
		
		String s1 = "Corpus";
		String s2 = "Enter corpus name and press enter.  Press escape to cancel.";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(newIcn);
		retVal.setAlwaysDisplayActions(true);
		
		retVal.setOpaque(false);
		
		ImageIcon cancelIcn = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		ImageIcon cancelIcnL = cancelIcn;
//				new ImageIcon(cancelIcn.getImage().getScaledInstance(IconSize.MEDIUM.getWidth(), IconSize.MEDIUM.getHeight(), Image.SCALE_SMOOTH));
		
		PhonUIAction btnSwapAct = new PhonUIAction(this, "onSwapNewAndCreateCorpus", retVal);
		btnSwapAct.putValue(Action.ACTION_COMMAND_KEY, "CANCEL_CREATE_ITEM");
		btnSwapAct.putValue(Action.NAME, "Cancel create");
		btnSwapAct.putValue(Action.SHORT_DESCRIPTION, "Cancel create");
		btnSwapAct.putValue(Action.SMALL_ICON, cancelIcn);
		btnSwapAct.putValue(Action.LARGE_ICON_KEY, cancelIcnL);
		retVal.addAction(btnSwapAct);
		
		JPanel corpusNamePanel = new JPanel(new BorderLayout());
		corpusNamePanel.setOpaque(false);
		
		final JTextField corpusNameField = new JTextField();
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
//		newProjectButton.revalidate();
		
		retVal.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				corpusNameField.requestFocus();
			}
		});
		
		return retVal;
	}
	
	public void onCreateCorpus(JTextField textField) {
		final String corpusName = textField.getText().trim();
		if(corpusName.length() == 0) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Corpus name cannot be empty").start(textField);
			return;
		}
		
		final EntryPointArgs initInfo = new EntryPointArgs();
		initInfo.put(EntryPointArgs.PROJECT_OBJECT, getProject());
		initInfo.put(EntryPointArgs.CORPUS_NAME, corpusName);
		
		try {
			PluginEntryPointRunner.executePlugin(NewCorpusEP.EP_NAME, initInfo);
			onSwapNewAndCreateCorpus(createCorpusButton);
			corpusList.setSelectedValue(corpusName, true);
		} catch (PluginException e) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast(e.getLocalizedMessage()).start(textField);
		}
	}
	
	public void onSwapNewAndCreateCorpus(MultiActionButton btn) {
		if(btn == newCorpusButton) {
			corpusPanel.remove(newCorpusButton);
			corpusPanel.add(createCorpusButton, BorderLayout.NORTH);
			createCorpusButton.requestFocus();
		} else {
			corpusPanel.remove(createCorpusButton);
			corpusPanel.add(newCorpusButton, BorderLayout.NORTH);
		}
		corpusPanel.revalidate();
		corpusPanel.repaint();
	}
	
	private MultiActionButton createNewSessionButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon newIcn = IconManager.getInstance().getIcon("mimetypes/text-xml", IconSize.SMALL);
		ImageIcon newIcnL = IconManager.getInstance().getIcon("actions/list-add", IconSize.MEDIUM);
		ImageIcon removeIcnL = IconManager.getInstance().getIcon("actions/list-remove", IconSize.MEDIUM);
		ImageIcon renameIcnL = IconManager.getInstance().getIcon("actions/edit-rename", IconSize.MEDIUM);
		ImageIcon openIcnL = IconManager.getInstance().getIcon("actions/view", IconSize.MEDIUM);
		
		String s1 = "Session";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(newIcn);
//		retVal.getBottomLabel().setText(WorkspaceTextStyler.toDescText(s2));
		retVal.setOpaque(false);
		
		PhonUIAction newAct = new PhonUIAction(this, "onSwapNewAndCreateSession", retVal);
		newAct.putValue(Action.LARGE_ICON_KEY, newIcnL);
		newAct.putValue(Action.SMALL_ICON, newIcn);
		newAct.putValue(Action.NAME, "New session");
		newAct.putValue(Action.SHORT_DESCRIPTION, "Create a new session in selected corpus");
		retVal.setDefaultAction(newAct);
		
		PhonUIAction deleteCurrentAct = new PhonUIAction(this, "onDeleteSession");
		deleteCurrentAct.putValue(Action.LARGE_ICON_KEY, removeIcnL);
		deleteCurrentAct.putValue(Action.NAME, "Delete session");
		deleteCurrentAct.putValue(Action.SHORT_DESCRIPTION, "Delete selected session");
		
		PhonUIAction renameCurrentAct = new PhonUIAction(this, "onRenameSession");
		renameCurrentAct.putValue(Action.LARGE_ICON_KEY, renameIcnL);
		renameCurrentAct.putValue(Action.NAME, "Rename session");
		renameCurrentAct.putValue(Action.SHORT_DESCRIPTION, "Rename selected session");
		
		PhonUIAction openCurrentAct = new PhonUIAction(this, "onOpenSession");
		openCurrentAct.putValue(Action.LARGE_ICON_KEY, openIcnL);
		openCurrentAct.putValue(Action.NAME, "Open session");
		openCurrentAct.putValue(Action.SHORT_DESCRIPTION, "Open selected session");
		
		retVal.setDisplayDefaultAction(true);
		retVal.addAction(deleteCurrentAct);
		retVal.addAction(renameCurrentAct);
		retVal.addAction(openCurrentAct);
		
		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		return retVal;
	}
	
	public void onDeleteSession() {
		if(getSelectedCorpus() == null) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Please select a corpus").start(corpusList);
			return;
		}
		if(getSelectedSessionName() == null) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Please select a session").start(sessionList);
			return;
		}
		deleteSession(getSelectedCorpus(), getSelectedSessionName());
	}
	
	public void onRenameSession() {
		if(getSelectedCorpus() == null) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Please select a corpus").start(corpusList);
			return;
		}
		if(getSelectedSessionName() == null) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Please select a session").start(sessionList);
			return;
		}
		renameSession(getSelectedCorpus(), getSelectedSessionName());
	}
	
	public void onOpenSession() {
		if(getSelectedCorpus() == null) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Please select a corpus").start(corpusList);
			return;
		}
		if(getSelectedSessionName() == null) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Please select a session").start(sessionList);
			return;
		}
		openSession(getSelectedCorpus(), getSelectedSessionName());
	}
	
	private MultiActionButton createSessionButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon newIcn = IconManager.getInstance().getIcon("mimetypes/text-xml", IconSize.SMALL);
		
		String s1 = "Session";
		String s2 = "Enter session name and press enter.  Press escape to cancel.";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(newIcn);
		retVal.setAlwaysDisplayActions(true);
		
		retVal.setOpaque(false);
		
		ImageIcon cancelIcn = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		ImageIcon cancelIcnL = cancelIcn;
//				new ImageIcon(cancelIcn.getImage().getScaledInstance(IconSize.MEDIUM.getWidth(), IconSize.MEDIUM.getHeight(), Image.SCALE_SMOOTH));
		
		PhonUIAction btnSwapAct = new PhonUIAction(this, "onSwapNewAndCreateSession", retVal);
		btnSwapAct.putValue(Action.ACTION_COMMAND_KEY, "CANCEL_CREATE_ITEM");
		btnSwapAct.putValue(Action.NAME, "Cancel create");
		btnSwapAct.putValue(Action.SHORT_DESCRIPTION, "Cancel create");
		btnSwapAct.putValue(Action.SMALL_ICON, cancelIcn);
		btnSwapAct.putValue(Action.LARGE_ICON_KEY, cancelIcnL);
		retVal.addAction(btnSwapAct);
		
		JPanel sessionNamePanel = new JPanel(new BorderLayout());
		sessionNamePanel.setOpaque(false);
		
		final JTextField sessionNameField = new JTextField();
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
	
	public void onSwapNewAndCreateSession(MultiActionButton btn) {
		if(btn == newSessionButton) {
			if(corpusList.getSelectedValue() == null) {
				Toolkit.getDefaultToolkit().beep();
				ToastFactory.makeToast("Please select a corpus").start(newSessionButton);
				return;
			}
			sessionPanel.remove(newSessionButton);
			sessionPanel.add(createSessionButton, BorderLayout.NORTH);
			createSessionButton.requestFocus();
		} else {
			sessionPanel.remove(createSessionButton);
			sessionPanel.add(newSessionButton, BorderLayout.NORTH);
		}
		sessionPanel.revalidate();
		sessionPanel.repaint();
	}
	
	public void onCreateSession(JTextField textField) {
		final String sessionName = textField.getText().trim();
		if(sessionName.length() == 0) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast("Session name cannot be empty").start(textField);
			return;
		}
		
		final EntryPointArgs args = new EntryPointArgs();
		args.put(EntryPointArgs.PROJECT_OBJECT, getProject());
		args.put(EntryPointArgs.CORPUS_NAME, corpusList.getSelectedValue());
		args.put(EntryPointArgs.SESSION_NAME, sessionName);
		
		try {
			PluginEntryPointRunner.executePlugin(NewSessionEP.EP_NAME, args);
			onSwapNewAndCreateSession(createSessionButton);
			sessionList.setSelectedValue(sessionName, true);
		} catch (PluginException e) {
			Toolkit.getDefaultToolkit().beep();
			ToastFactory.makeToast(e.getLocalizedMessage()).start(textField);
		}
	}
	
	/**
	 * Opens a session.  If the 'Multi-blind' mode box is 
	 * checked, the user will have to create/choose a set
	 * of personalized transcripts for the session.  Some operations
	 * such as alignment and validation are unvavaible in this mode.
	 * 
	 * @param corpus
	 * @param session
	 */
	private void openSession(String corpus, String session) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getProject());
		initInfo.put("sessionName", corpus + "." + session);
		initInfo.put("blindmode", blindModeBox.isSelected());
		
		try {
			PluginEntryPointRunner.executePlugin("SessionEditor", initInfo);
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * Runs the copy controller.
	 * 
	 * @param proj1
	 * @param corpus
	 * @param session
	 * @param proj2
	 * @param destCorpus
	 * @param force
	 * @param move
	 */
	private void copySession(
			Project proj1, String corpus, String session,
			Project proj2, String destCorpus,
			boolean force, boolean move) {
		
		try {
			List<String> proj2Sessions = 
				proj2.getCorpusSessions(destCorpus);
			if(proj2Sessions.contains(session)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm");
				
				String proj1LastModified = 
					sdf.format(proj1.getSessionModificationTime(corpus, session).toDate());
				
				String proj2LastModified = 
					sdf.format(proj2.getSessionModificationTime(corpus, session).toDate());
				
				String msg = 
					"Replace '" + corpus + "." + session + "' modified " + proj2LastModified + 
					" with file modified on " + proj1LastModified + "?";
				
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(this);
				props.setRunAsync(false);
				props.setTitle("Session Already Exists");
				props.setHeader("Session Already Exists");
				props.setMessage(msg);
				props.setOptions(MessageDialogProperties.yesNoOptions);
				
				int result = 
					NativeDialogs.showMessageDialog(props);
				
				if(result != 0) {
					return;
				}
				
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			ToastFactory.makeToast(e.getLocalizedMessage()).start(this.getRootPane());
		}
		
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
			initInfo.put("project", proj1);
			initInfo.put("corpusName", corpus);
			initInfo.put("sessionName", session);
			initInfo.put("destproject", proj2);
			initInfo.put("destcorpus", destCorpus);
			initInfo.put("overwrite", true);
			initInfo.put("move", move);
		
		PluginEntryPointRunner.executePluginInBackground("CopySession", initInfo);
	}
	
	/**
	 * Run the delete session controller
	 * 
	 * @param corpus
	 * @param session
	 */
	private void deleteSession(String corpus, String session) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getProject());
		initInfo.put("corpusName", corpus);
		initInfo.put("sessionName", session);
		
		PluginEntryPointRunner.executePluginInBackground("DeleteSession", initInfo);
	}
	
	/**
	 * Run the export session controller
	 * 
	 * @param corpus
	 * @param session
	 */
	private void saveSessionAs(String corpus, String session) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getProject());
		initInfo.put("corpusName", corpus);
		initInfo.put("sessionName", session);
		
		PluginEntryPointRunner.executePluginInBackground("ExportSession", initInfo);
	}
	
	/**
	 * Run the rename session controller
	 * 
	 * @param corpus
	 * @param session
	 */
	private void renameSession(String corpus, String session) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getProject());
		initInfo.put("corpusName", corpus);
		initInfo.put("sessionName", session);
		
		PluginEntryPointRunner.executePluginInBackground("RenameSession", initInfo);
	}
	
	/**
	 * Run the duplicate session controller
	 * 
	 * @param corpus
	 * @param session
	 */
	private void duplicateSession(String corpus, String session) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getProject());
		initInfo.put("corpusName", corpus);
		initInfo.put("sessionName", session);
		
		PluginEntryPointRunner.executePluginInBackground("DuplicateSession", initInfo);
	}

	/**
	 * Return the corpus template controller
	 *
	 * @para corpus
	 */
	private void openCorpusTemplate(String corpus) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getProject());
		initInfo.put("corpusName", corpus);

		PluginEntryPointRunner.executePluginInBackground("CorpusTemplate", initInfo);
	}
			
	/**
	 * Runs the new session controller
	 * 
	 * @param corpus
	 */
	private void createNewSession(String corpus) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getProject());
		initInfo.put("corpusName", corpus);
		
		PluginEntryPointRunner.executePluginInBackground("NewSession", initInfo);
	}
	
	/**
	 * Run the delete corpus controller
	 * 
	 * @param corpus
	 */
	private void deleteCorpus(String corpus) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getProject());
		initInfo.put("corpusName", corpus);
		
		PluginEntryPointRunner.executePluginInBackground("DeleteCorpus", initInfo);
	}
	
	/**
	 * Run the rename corpus controller
	 */
	private void renameCorpus(String corpus) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getProject());
		initInfo.put("corpusName", corpus);
		
		PluginEntryPointRunner.executePluginInBackground("RenameCorpus", initInfo);
	}
	
//	/**
//	 * Run the copy corpus controller
//	 * 
//	 * @param proj1
//	 * @param corpus
//	 * @param proj2
//	 * @param force
//	 * @param move
//	 */
//	private void copyCorpus(IPhonProject proj1, String corpus,
//			IPhonProject proj2, boolean force, boolean move) {
//		HashMap<String, Object> initInfo = new HashMap<String, Object>();
//		initInfo.put("project", proj1);
//		initInfo.put("corpus", corpus);
//		initInfo.put("destproject", proj2);
//		initInfo.put("force", force);
//		initInfo.put("move", move);
//		
//		ModuleInformation mi = ResourceLocator.getInstance().getModuleInformationByAction(
//				"ca.phon.module.core.CopyCorpusController");
//		LoadModule lm = new LoadModule(mi, initInfo);
//		lm.start();
//	}
	
	/** 
	 * Displays the corpus list menu
	 * 
	 * @param clickPoint
	 */
	private void showCorpusListContextMenu(Point clickPoint) {
		int selectedCorpusIndex = 
			corpusList.locationToIndex(clickPoint);
		if(selectedCorpusIndex < 0)
			return;
		
		final String corpus = corpusList.getModel().getElementAt(
				selectedCorpusIndex).toString();
		
		JPopupMenu contextMenu = new JPopupMenu();
		
		// new session item
		JMenuItem newSessionItem = new JMenuItem("New Session");
		newSessionItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createNewSession(corpus);
			}
			
		});
		contextMenu.add(newSessionItem);
		
		contextMenu.addSeparator();
		
		// rename
		JMenuItem renameItem = new JMenuItem("Rename Corpus");
		renameItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				renameCorpus(corpus);
			}
			
		});
		contextMenu.add(renameItem);
		
		// delete
		JMenuItem deleteItem = new JMenuItem("Delete Corpus");
		deleteItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				deleteCorpus(corpus);
			}
			
		});
		contextMenu.add(deleteItem);

		contextMenu.addSeparator();

		JMenuItem templateItem = new JMenuItem("Edit corpus template...");
		templateItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				openCorpusTemplate(corpus);
			}
		});
		contextMenu.add(templateItem);
		
		contextMenu.show(corpusList, clickPoint.x, clickPoint.y);
	}
	
	/**
	 * Displays the session list menu
	 * 
	 * @param clickPoint
	 */
	private void showSessionListContextMenu(Point clickPoint) {
		int selectedSessionIndex = 
			sessionList.locationToIndex(clickPoint);
		if(selectedSessionIndex < 0)
			return;
		
		final String corpus = corpusList.getSelectedValue().toString();
		if(sessionList.getModel().getElementAt(selectedSessionIndex) == null)
			return;
		
		final String session = 
			sessionList.getModel().getElementAt(selectedSessionIndex).toString();
		
		JPopupMenu contextMenu = new JPopupMenu();
		
		// open item
		JMenuItem openItem = new JMenuItem("Open Session");
		openItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				openSession(corpus, session);
			}
			
		});
		contextMenu.add(openItem);
		
		contextMenu.addSeparator();
		
		// rename item
		JMenuItem renameItem = new JMenuItem("Rename Session");
		renameItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				renameSession(corpus, session);
			}
			
		});
		contextMenu.add(renameItem);
		
		// delete item
		JMenuItem deleteItem = new JMenuItem("Delete Session");
		deleteItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				deleteSession(corpus, session);
			}
			
		});
		contextMenu.add(deleteItem);
		
		JMenuItem duplicateItem = new JMenuItem("Duplicate Session");
		duplicateItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				duplicateSession(corpus, session);
			}
			
		});
		contextMenu.add(duplicateItem);
		
		JMenuItem saveAsItem = new JMenuItem("Save As");
		saveAsItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				saveSessionAs(corpus, session);
			}
			
		});
		contextMenu.add(saveAsItem);
		contextMenu.addSeparator();
		
		JMenu moveToMenu = new JMenu("Move Session To");
		JMenu copyToMenu = new JMenu("Copy Session To");
		
		final String corpusListMsg = "<html><b>This project:</b></html>";
		moveToMenu.add(corpusListMsg).setEnabled(false);
		copyToMenu.add(corpusListMsg).setEnabled(false);
		
		List<String> projectCorpora = null;
		
		Collator collator = CollatorFactory.defaultCollator();
		projectCorpora = getProject().getCorpora();
		Collections.sort(projectCorpora, collator);

		// corpora in this project
		for(int i = 0; i < projectCorpora.size(); i++) {
			final String thisCorpus = projectCorpora.get(i);
			
			if(thisCorpus.equals(corpus))
				continue;
			
			JMenuItem currentCopyItem = new JMenuItem(thisCorpus);
			currentCopyItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					copySession(
							getProject(), corpus, session,
							getProject(), thisCorpus, false, false);
				}
				
			});
			copyToMenu.add(currentCopyItem);
			
			JMenuItem currentMoveItem = new JMenuItem(thisCorpus);
			currentMoveItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					copySession(
							getProject(), corpus, session,
							getProject(), thisCorpus, false, true);
				}
				
			});
			moveToMenu.add(currentMoveItem);
		}
		
		final String openProjectsMsg = "<html><b>Open project:</b></html>";
		boolean openProjectMsgAdded = false;
		
		List<CommonModuleFrame> openWindows = CommonModuleFrame.getOpenWindows();
		for(int i = 0; i < openWindows.size(); i++) {
			final CommonModuleFrame cmf = openWindows.get(i);
			if(!(cmf instanceof ProjectWindow)) {
				continue;
			}
			final ProjectWindow projWindow = ProjectWindow.class.cast(cmf);
			final Project proj = projWindow.getProject();
			if(proj == null) continue;
			
			if(proj != getProject()) {
				if(!openProjectMsgAdded) {
					copyToMenu.add(openProjectsMsg).setEnabled(false);
					moveToMenu.add(openProjectsMsg).setEnabled(false);
					openProjectMsgAdded = true;
				}
				List<String> projCorpora = null;
				String projName = new String();
				projCorpora = 
					proj.getCorpora();
				Collections.sort(projCorpora, collator);
				projName = proj.getName();
				
				JMenu projMoveToMenu = new JMenu(projName);
				JMenu projCopyToMenu = new JMenu(projName);
				for(int j = 0; j < projCorpora.size(); j++) {
					final String projCorpus = 
						projCorpora.get(j);
					
					JMenuItem projMoveToItem = 
						new JMenuItem(projCorpus);
					projMoveToItem.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							copySession(
									getProject(), corpus, session,
									proj, projCorpus, false, true);
						}
						
					});
					projMoveToMenu.add(projMoveToItem);
					
					JMenuItem projCopyToItem = 
						new JMenuItem(projCorpus);
					projCopyToItem.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							copySession(
									getProject(), corpus, session,
									proj, projCorpus, false, false);
						}
						
					});
					projCopyToMenu.add(projCopyToItem);
				}
				
				copyToMenu.add(projCopyToMenu);
				moveToMenu.add(projMoveToMenu);
			}
		}
		
		final String workspaceMsg = "<html><b>Workspace project:</b></html>";
		boolean workspaceMsgAdded = false;
		
		// workspace projects
		final List<Project> workspaceProjects = Workspace.userWorkspace().getProjects();
		for(final Project p:workspaceProjects) {
			if(p.getLocation().equals(getProject().getLocation())) {
				continue;
			}
			if(!workspaceMsgAdded) {
				copyToMenu.add(workspaceMsg).setEnabled(false);
				moveToMenu.add(workspaceMsg).setEnabled(false);
				workspaceMsgAdded = true;
			}
			List<String> projCorpora = null;
			String projName = new String();
			projCorpora = 
				p.getCorpora();
			Collections.sort(projCorpora, collator);
			projName = p.getName();
			
			JMenu projMoveToMenu = new JMenu(projName);
			JMenu projCopyToMenu = new JMenu(projName);
			for(int j = 0; j < projCorpora.size(); j++) {
				final String projCorpus = 
					projCorpora.get(j);
				
				JMenuItem projMoveToItem = 
					new JMenuItem(projCorpus);
				projMoveToItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						copySession(
								getProject(), corpus, session,
								p, projCorpus, false, true);
					}
					
				});
				projMoveToMenu.add(projMoveToItem);
				
				JMenuItem projCopyToItem = 
					new JMenuItem(projCorpus);
				projCopyToItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						copySession(
								getProject(), corpus, session,
								p, projCorpus, false, false);
					}
					
				});
				projCopyToMenu.add(projCopyToItem);
			}
			
			copyToMenu.add(projCopyToMenu);
			moveToMenu.add(projMoveToMenu);
		}
		
		contextMenu.add(copyToMenu);
		contextMenu.add(moveToMenu);
		
		contextMenu.show(sessionList, clickPoint.x, clickPoint.y);
	}

	public void windowActivated(WindowEvent e) {

	}

	public void windowClosed(WindowEvent e) {
		getProject().removeProjectListener(myProjectListener);
		
		// are there any other project windows open?
		boolean otherProjectsOpen = false;
		
		// close all other project windows
		for(CommonModuleFrame f:CommonModuleFrame.getOpenWindows()) {
			
			if(f instanceof ProjectWindow && f != this)
				otherProjectsOpen = true;
			else if(f instanceof WorkspaceDialog)
				otherProjectsOpen = true; // also don't close if workspace window is still open
			
//			if(f.getProject() == project) {
//				PhonQuitEvent pqe = new PhonQuitEvent();
//				pqe.setIssuingFrame(this);
//				pqe.setChildFrame(f);
//				f.sendEvent(pqe);
//			}
		}
		
//		// close the project file!
//		try {
//			getProject().close();
//		} catch (RemoteException e1) {
//			LOGGER.warning(e1.getMessage());
//		} catch (IOException e1) {
//			LOGGER.warning(e1.getMessage());
//		}
		
		// open the open-project window on Windows if no other project window
		// is open
		if(!otherProjectsOpen) {
//			ModuleInformation mi = ResourceLocator.getInstance().getModuleInformationByAction(
//				"ca.phon.modules.core.OpenProjectController");
//			LoadModule lm = new LoadModule(mi, new HashMap<String, Object>());
//			lm.start();
			try {
				PluginEntryPointRunner.executePlugin("Exit");
			} catch (PluginException e1) {
				LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
				System.exit(1);
			}
		}
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {

	}

	public void windowDeiconified(WindowEvent e) {

	}

	public void windowIconified(WindowEvent e) {

	}

	public void windowOpened(WindowEvent e) {

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
	
	public StatusPanel getStatusPanel() {
		return msgPanel;
	}
	
	public void refreshProject() {
		final Project project = getProject();
		final ProjectRefresh impl = project.getExtension(ProjectRefresh.class);
		if(impl != null) {
			impl.refresh();
			updateLists();
		}
	}
	
	public class StatusPanel extends JComponent {
		
		private JLabel msgLabel = new JLabel("");
		private JXBusyLabel progressBar = 
				new JXBusyLabel(new Dimension(16, 16));
		
		public StatusPanel() {
			super();
			init();
		}
		
		private void init() {
			setLayout(new HorizontalLayout(3));
			
			add(progressBar);
			add(msgLabel);
		}
		
		public void setIndeterminate(final boolean v) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					progressBar.setBusy(v);
					if(!v) 
						msgLabel.setText("");
				}
				
			});
		}
		
		public void setMessageLabel(String txt) {
			final String text = txt;
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					msgLabel.setText(text);
				}
				
			});
		}
		
		public void reset() {
			setIndeterminate(false);
			setMessageLabel("");
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
