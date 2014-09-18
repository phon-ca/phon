/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import groovy.ui.view.GTKDefaults;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.lang3.StringUtils;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.workspace.WorkspaceDialog;
import ca.phon.plugin.PluginAction;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.project.ProjectListener;
import ca.phon.project.ProjectRefresh;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MenuManager;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.CollatorFactory;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;
import ca.phon.workspace.Workspace;

import com.jgoodies.forms.factories.DefaultComponentFactory;
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
	private JList corpusList;
	private CorpusListModel corpusModel;
	private CorpusDetailsPane corpusDetails;
	
	/** The session list */
	private JList sessionList;
	private SessionListModel sessionModel;
	private SessionDetailsPane sessionDetails;
	
	/** The checkbox */
	private JCheckBox blindModeBox;
	
	/** Label for messages */
	private MessagePanel msgPanel;
	
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
		JMenuItem refreshItem = new JMenuItem("Refresh");
		KeyStroke refreshKs = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		refreshItem.setAccelerator(refreshKs);
		refreshItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshProject();
			}
			
		});
		projectMenu.add(refreshItem);
		projectMenu.addSeparator();
		
		// create corpus item
		JMenuItem newCorpusItem = new JMenuItem("New Corpus...");
		newCorpusItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				final Project pfe = getExtension(Project.class);
				if(pfe != null) {
					HashMap<String, Object> initInfo = 
							new HashMap<String, Object>();
					initInfo.put("project", pfe);
					PluginEntryPointRunner.executePluginInBackground("NewCorpus", initInfo);
				}
			}
			
		});
		projectMenu.add(newCorpusItem);
		
		//		 create corpus item
		JMenuItem newSessionItem = new JMenuItem("New Session...");
		newSessionItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				HashMap<String, Object> initInfo = 
					new HashMap<String, Object>();
				initInfo.put("project", getProject());
				
				PluginEntryPointRunner.executePluginInBackground("NewSession", initInfo);
			}
			
		});
		projectMenu.add(newSessionItem);
		
		projectMenu.addSeparator();
		
		JMenuItem anonymizeParticipantInfoItem = new JMenuItem("Anonymize Participant Information...");
		anonymizeParticipantInfoItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				HashMap<String, Object> initInfo = new HashMap<String, Object>();
				initInfo.put("project", getProject());
				
				PluginEntryPointRunner.executePluginInBackground(AnonymizeParticipantInfoEP.EP_NAME, initInfo);
			}
		});
		projectMenu.add(anonymizeParticipantInfoItem);
		
		JMenuItem repairItem = new JMenuItem("Check Transcriptions...");
		repairItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				HashMap<String, Object> initInfo = new HashMap<String, Object>();
				initInfo.put("project", getProject());
				
				PluginEntryPointRunner.executePluginInBackground("CheckIPA", initInfo);
			}
			
		});
		projectMenu.add(repairItem);
		
		// merge/split sessions
		JMenuItem deriveItem = new JMenuItem("Derive Session...");
		deriveItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				HashMap<String, Object> initInfo = new HashMap<String, Object>();
				initInfo.put("project", getProject());
				
				PluginEntryPointRunner.executePluginInBackground("DeriveSession", initInfo);
			}
			
		});
		projectMenu.add(deriveItem);
	}

	private void init() {
		/* Layout */
		FormLayout innerLayout = new FormLayout(
				"fill:pref:grow, 3dlu, fill:pref:grow",
				"1dlu, pref, 3dlu, fill:pref:grow, 3dlu, fill:100px:noGrow, 5dlu");
		getContentPane().setLayout(innerLayout);
		
		int colGroups[][] = {{1,3}};
		innerLayout.setColumnGroups(colGroups);
		
		Dimension listSize = new Dimension(200, 300);
		
		/* Create components */
		corpusList = new JList();
		corpusList.setMinimumSize(listSize);
		
		corpusModel = new CorpusListModel(getProject());
		corpusList.setModel(corpusModel);
		corpusList.setCellRenderer(new CorpusListCellRenderer());
		corpusList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if(corpusList.getSelectedValue() != null) {
					String corpus = 
						corpusList.getSelectedValue().toString();
					sessionModel.setCorpus(corpus);
					corpusDetails.setCorpus(corpus);
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
		corpusDetails.setLineWrap(true);
		corpusDetails.setBackground(Color.white);
		corpusDetails.setOpaque(true);
		JScrollPane corpusDetailsScroller = new JScrollPane(corpusDetails);
		
		sessionList = new JList();
		sessionList.setMinimumSize(listSize);
		sessionModel = new SessionListModel(getProject());
		sessionList.setModel(sessionModel);
		sessionList.setCellRenderer(new SessionListCellRenderer());
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
					
//					JProgressBar openProgress = new JProgressBar();
//					openProgress.setIndeterminate(true);
//					openProgress.setMaximumSize(
//							new Dimension(openProgress.getMaximumSize().width, 
//									blindModeBox.getHeight()-10));
//					msgLabel.add(openProgress);
//					msgLabel.setText("Opening '" + corpus + "." + session + "'");
					
//					msgPanel.removeAll();
//					msgPanel.add(openProgress);
//					msgPanel.add(new JLabel("Opening '" + corpus + "." + session + "'"));
//					msgPanel.revalidate();
					msgPanel.reset();
					msgPanel.allowCancel(false);
					msgPanel.setMessageLabel("Opening '" + corpus + "." + session + "'");
					msgPanel.setItermediate(true);
					msgPanel.showPanel(true);
//					msgPanel.revalidate();
					
//					SwingUtilities.invokeLater(new Runnable() {
//
//						public void run() {
////							msgPanel.revalidate();
////							msgPanel.repaint();
//							openSession(corpus, session);
//						}
//						
//					});
					Runnable th = new Runnable() {
						public void run() {
							openSession(corpus, session);
							msgPanel.setItermediate(false);
							msgPanel.showPanel(false);
//							msgPanel.showPanel(false);
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
		sessionDetails.setWrapStyleWord(true);
		sessionDetails.setBackground(Color.white);
		sessionDetails.setOpaque(true);
		JScrollPane sessionDetailsScroller = new JScrollPane(sessionDetails);
		
		JScrollPane corpusScroller = new JScrollPane(corpusList);
		JScrollPane sessionScroller = new JScrollPane(sessionList);
		
		blindModeBox = new JCheckBox("Blind transcription");
		blindModeBox.setSelected(false);
		
		msgPanel = new MessagePanel();
		msgPanel.showPanel(false);
//		msgPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		
		JPanel innerPanel = new JPanel(innerLayout);
		
		CellConstraints cc = new CellConstraints();
		
		innerPanel.add(DefaultComponentFactory.getInstance().createSeparator("Corpus"),
				cc.xy(1,2));
		innerPanel.add(corpusScroller, cc.xy(1, 4));
		innerPanel.add(corpusDetailsScroller, cc.xy(1,6));
		
		innerPanel.add(DefaultComponentFactory.getInstance().createSeparator("Session"),
				cc.xy(3,2));
		innerPanel.add(sessionScroller, cc.xy(3,4));
		innerPanel.add(sessionDetailsScroller, cc.xy(3,6));
		
		
		// the frame layout
		FormLayout frameLayout = new FormLayout(
				"5dlu, fill:pref:grow, 5dlu, right:pref, 5dlu",
				"pref, 3dlu, 40px, fill:pref:grow, 5dlu");
		
		String projectName = null;
		projectName = getProject().getName();
		
		DialogHeader header = new DialogHeader(projectName,
				StringUtils.abbreviate(projectLoadPath, 80));
//				StringUtils.shortenStringUsingToken(projectLoadPath, "...", 80));
		
		getContentPane().setLayout(frameLayout);
		getContentPane().add(header, cc.xyw(1,1,5));
		getContentPane().add(msgPanel, cc.xy(2,3));
		getContentPane().add(blindModeBox, cc.xy(4,3));
		getContentPane().add(innerPanel, cc.xyw(2, 4, 3));
		
//		if(!isRemoteProject()) {
//			// show the sharing panel
//			JComponent sharingComp = 
//				createSharingPanel();
//			ShrinkPanel sp = new ShrinkPanel("Sharing", sharingComp);
//			sp.setBorder(BorderFactory.createLineBorder(Color.decode("0xbbbbbb")));
//			sp.toggleVisible();
//			
//			getContentPane().add(sp, cc.xyw(2, 6, 2));
//		}
	}
	
//	private JComponent createSharingPanel() {
//		JPanel retVal = new JPanel();
//		
//		FormLayout layout = new FormLayout(
//				"pref, 3dlu, fill:pref:grow",
//				"pref, 3dlu, pref:grow");
//		
//		shareButton = new JToggleButton("Share Project");
//		ImageIcon shareIcon = IconManager.getInstance().getIcon(
//				"apps/internet-web-browser", IconSize.MEDIUM);
//		shareButton.setIcon(shareIcon);
//		shareButton.setVerticalTextPosition(SwingConstants.BOTTOM);
//		shareButton.setHorizontalTextPosition(SwingConstants.CENTER);
//		shareButton.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				if(ProjectRMIServer.getInstance().getSharedProjects().contains(project)) {
//					ProjectRMIServer.getInstance().unRegisterProject(project);
//					
//					sharingStatusLabel.setText("Not sharing project");
//					rmiPathLabel.setText("rmi://");
//				} else {
//					String rmiPath = 
//						ProjectRMIServer.getInstance().registerProject(project);
//					
//					if(rmiPath != null) {
//					sharingStatusLabel.setText("Sharing project as:");
//					rmiPathLabel.setText(rmiPath);
//					}
//				}
//			}
//			
//		});
//		
//		sharingStatusLabel = new JLabel("Not sharing project");
//		
//		rmiPathLabel = new JLabel("rmi://");
//		
//		CellConstraints cc = new CellConstraints();
//		
//		retVal.setLayout(layout);
//		
//		retVal.add(shareButton, cc.xywh(1, 1, 1, 3));
//		retVal.add(sharingStatusLabel, cc.xy(3, 1));
//		retVal.add(rmiPathLabel, cc.xy(3, 3));
//		
//		return retVal;
//	}
	
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
		if(!otherProjectsOpen && !OSInfo.isMacOs()) {
//			ModuleInformation mi = ResourceLocator.getInstance().getModuleInformationByAction(
//				"ca.phon.modules.core.OpenProjectController");
//			LoadModule lm = new LoadModule(mi, new HashMap<String, Object>());
//			lm.start();
			// quit
			System.exit(0);
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
	
	public MessagePanel getMessagePanel() {
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
	
	private class MessagePanel extends JComponent {
		private JLabel msgLabel = new JLabel("Hello World");
		private JProgressBar progressBar = new JProgressBar();
		private JButton cancelButton = new JButton();
		
		public MessagePanel() {
			super();
			init();
		}
		
		private void init() {
			FormLayout layout = new FormLayout(
					"fill:default:grow, pref",
					"pref, pref");
			setLayout(layout);
			CellConstraints cc = new CellConstraints();
			
			progressBar.setMaximumSize(new Dimension(250, 20));
			
			// by default the cancel button is not shown
			cancelButton.setIcon(IconManager.getInstance().getIcon("actions/process-stop", IconSize.SMALL));
			cancelButton.setToolTipText("Stop Task");
			cancelButton.setVisible(false);
			cancelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					cancelButton.setEnabled(false);
				}
				
			});
			
			add(msgLabel, cc.xy(1,1));
			add(progressBar, cc.xy(1,2));
			add(cancelButton, cc.xy(2,2));
		}
		
		public void allowCancel(boolean v) {
			final boolean val = v;
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					cancelButton.setEnabled(val);
					cancelButton.setVisible(val);
				}
				
			});
		}
		
		public void setItermediate(boolean v) {
			final boolean val = v;
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					progressBar.setIndeterminate(val);
				}
				
			});
		}
		
		public void setProgressBarValue(int v) {
			final int val = v;
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					progressBar.setValue(val);
				}
				
			});
		}
		
		public void setProgressBarRange(int min, int max) {
			final int minimum = min;
			final int maximum = max;
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					progressBar.setMinimum(minimum);
					progressBar.setMaximum(maximum);
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
		
		public boolean isCanceled() {
			return (cancelButton.isVisible() && !cancelButton.isEnabled());
		}
		
		public void reset() {
			setItermediate(true);
			setMessageLabel("");
			allowCancel(false);
		}
		
		public void showPanel(boolean v) {
			final boolean val = v;
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					msgLabel.setVisible(val);
					progressBar.setVisible(val);
					
					if(cancelButton.isVisible() && !val)
						cancelButton.setVisible(false);
				}
				
			});
		}
	}
	
}
