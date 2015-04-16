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
package ca.phon.app.query.report;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.jdesktop.swingx.JXTree;

import ca.phon.query.report.ReportIO;
import ca.phon.query.report.io.AggregrateInventory;
import ca.phon.query.report.io.CommentSection;
import ca.phon.query.report.io.Group;
import ca.phon.query.report.io.InventorySection;
import ca.phon.query.report.io.ObjectFactory;
import ca.phon.query.report.io.ParamSection;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.query.report.io.ResultListing;
import ca.phon.query.report.io.Section;
import ca.phon.query.report.io.SummarySection;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel used for editing report templates.
 *
 */
public class ReportEditor extends JPanel implements SectionListener {
	
	private final static Logger LOGGER = Logger.getLogger(ReportEditor.class.getName());
	

	private final QName _SECTION_QNAME = new QName("http://phon.ling.mun.ca/ns/report", "report-section");
	
	private final QName _GROUP_SECTION_QNAME = new QName("http://phon.ling.mun.ca/ns/report", "group-report-section");
	
	/** The report we are editing */
	private ReportDesign report;
	
	/* UI */
	private JXTree reportTree;
	private ReportTreeModel reportTreeModel;
	
	private JButton addSectionButton;
	private JButton removeSectionButton;
	
	private JButton moveUpButton;
	private JButton moveDownButton;
	
	private JButton newButton;
	private JButton saveButton;
	private JButton openButton;
	
	private JPanel panels;
	
	// keep track of panels for sections
	Map<Section, String> sectionPanels = 
		new HashMap<Section, String>();
	
	public ReportEditor() {
		super();
		
		ObjectFactory factory = new ObjectFactory();
		this.report = factory.createReportDesign();
		
		initPanel();
	}
	
	public ReportEditor(ReportDesign design) {
		super();
		
		this.report = design;
		
		initPanel();
	}
	
	private void initPanel() {
		
		CellConstraints cc = new CellConstraints();
		
		setLayout(new BorderLayout());
		
		panels = new JPanel(new CardLayout());
		
		ImageIcon addIcon = 
			IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);
		ImageIcon removeIcon = 
			IconManager.getInstance().getIcon("actions/list-remove", IconSize.XSMALL);
		ImageIcon upIcon = 
			IconManager.getInstance().getIcon("actions/go-up", IconSize.XSMALL);
		ImageIcon downIcon = 
			IconManager.getInstance().getIcon("actions/go-down", IconSize.XSMALL);
		ImageIcon saveIcon = 
			IconManager.getInstance().getIcon("actions/document-save", IconSize.XSMALL);
		ImageIcon openIcon =
			IconManager.getInstance().getIcon("actions/document-open", IconSize.XSMALL);
		ImageIcon newIcon =
			IconManager.getInstance().getIcon("actions/document-new", IconSize.XSMALL);
		
		
		addSectionButton = new JButton(addIcon);
		addSectionButton.setToolTipText("Add section");
		addSectionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JPopupMenu menu = createContextMenu();
				menu.show(addSectionButton, 0, 0);
			}
			
		});
		
		removeSectionButton = new JButton(removeIcon);
		removeSectionButton.setToolTipText("Remove section");
		removeSectionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				TreePath tp = reportTree.getSelectionPath();
				if(tp != null) {
					removeSectionAtPath(tp);
				}
			}
			
		});
		
		moveUpButton = new JButton(upIcon);
		moveUpButton.setToolTipText("Move section up");
		moveUpButton.setEnabled(false);
		moveUpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath tp = reportTree.getSelectionPath();
				if(tp != null) {
					moveSectionUp(tp);
				}
			}
			
		});
		
		moveDownButton = new JButton(downIcon);
		moveDownButton.setToolTipText("Move section down");
		moveDownButton.setEnabled(false);
		moveDownButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath tp = reportTree.getSelectionPath();
				if(tp != null) {
					moveSectionDown(tp);
				}
			}
			
		});
		
		saveButton = new JButton(saveIcon);
		saveButton.setToolTipText("Save report design...");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveReportFormat();
			}
			
			
		});
		
		newButton = new JButton(newIcon);
		newButton.setToolTipText("New empty report design...");
		newButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newReportFormat();
				
			}
		});
		
		openButton = new JButton(openIcon);
		openButton.setToolTipText("Open report design...");
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				openReportFormat();
			}
			
		});
		
		FormLayout btnLayout = new FormLayout(
				"left:pref, left:pref, left:pref, fill:pref:grow, right:pref, right:pref" + ("," + removeSectionButton.getPreferredSize().width + "px"),
				"pref");
		JPanel btnPanel = new JPanel(btnLayout);
//		if(PhonUtilities.isDebugMode()) {
			btnPanel.add(newButton, cc.xy(1, 1));
			btnPanel.add(saveButton, cc.xy(2,1));
			btnPanel.add(openButton, cc.xy(3, 1));
//		}
		btnPanel.add(addSectionButton, cc.xy(5, 1));
		btnPanel.add(removeSectionButton, cc.xy(6, 1));
		
		FormLayout btnLayout2 = new FormLayout(
				"pref", "pref, 3dlu, pref, fill:pref:grow");
		JPanel btnPanel2 = new JPanel(btnLayout2);
		btnPanel2.add(moveUpButton, cc.xy(1,1));
		btnPanel2.add(moveDownButton, cc.xy(1, 3));
		
		JPanel leftPanel = new JPanel(new BorderLayout());
		
		reportTreeModel = new ReportTreeModel(report);
		reportTree = new JXTree(reportTreeModel);
		reportTree.addMouseListener(new TreeMouseListener());
		reportTree.expandAll();
		reportTree.setCellRenderer(new ReportTreeCellRenderer());
		reportTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object section = e.getPath().getLastPathComponent();
					CardLayout cl = (CardLayout)panels.getLayout();
					cl.show(panels, sectionPanels.get(section));
					setupMovementButtonsForPath(e.getPath());
			}
		});
		
		ActionMap actionMap = reportTree.getActionMap();
		InputMap inputMap = reportTree.getInputMap(WHEN_FOCUSED);
		KeyStroke ks1 = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		KeyStroke ks2 = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		String remId = "remove_section";
		PhonUIAction removeSectionAct = new PhonUIAction(this, "onRemoveSection");
		removeSectionAct.putValue(Action.NAME, "Remove section");
		actionMap.put(remId, removeSectionAct);
		inputMap.put(ks1, remId);
		inputMap.put(ks2, remId);
		
		reportTree.setActionMap(actionMap);
		reportTree.setInputMap(WHEN_FOCUSED, inputMap);
		
		leftPanel.add(btnPanel, BorderLayout.NORTH);
		leftPanel.add(new JScrollPane(reportTree), BorderLayout.CENTER);
		leftPanel.add(btnPanel2, BorderLayout.EAST);
		
		leftPanel.setBorder(BorderFactory.createTitledBorder("Report Outline"));
		
//		add(leftPanel, cc.xy(1,1));
		
		SectionPanelFactory panelFactory = new SectionPanelFactory();
		addSectionPanel(report, panelFactory);
		
//		add(panels, cc.xy(3,1));

		FormLayout splitLayout = new FormLayout(
				"300px:nogrow, fill:default:grow",
				"fill:default:grow");
		JPanel splitPane = new JPanel(splitLayout);
		splitPane.add(leftPanel, cc.xy(1, 1));
//		JScrollPane panelsScroller = new JScrollPane(panels);
//		panelsScroller.setBorder(BorderFactory.createEmptyBorder());
		splitPane.add(panels, cc.xy(2, 1));
//		JSplitPane splitPane = new JSplitPane();
//		splitPane.setLeftComponent(leftPanel);
//		splitPane.setRightComponent(panels);
//		splitPane.setDividerLocation(300);
		add(splitPane, BorderLayout.CENTER);
		
		// setup bottom panel
		FormLayout btmLayout = new FormLayout(
				"left:pref, pref, fill:pref:grow, right:pref",
				"pref, pref");
		JPanel btmPanel = new JPanel(btmLayout);
		
		
		
		reportTree.setSelectionPath(new TreePath(report));
	}
	
	private static int sIndex = 0;
	private void addSectionPanel(Section section, SectionPanelFactory panelFactory) {
		
		String sKey = "SECTION_" + (sIndex++);
		SectionPanel<? extends Section> p = panelFactory.createSectionPanel(section);
		p.addSectionListener(this);
		panels.add(p, sKey);
		sectionPanels.put(section, sKey);
		
		if(section == report) {
			for(JAXBElement<? extends Section> rSect:report.getReportSection()) {
				addSectionPanel(rSect.getValue(), panelFactory);
			}
		} else if(section instanceof Group) {
			Group group = (Group)section;
			for(JAXBElement<? extends Section> gSect:group.getGroupReportSection()) {
				addSectionPanel(gSect.getValue(), panelFactory);
			}
		}
	}
	
	/**
	 * Return the current design object
	 * 
	 * @return the report design
	 */
	public ReportDesign getReportDesign() {
		return this.report;
	}

	@Override
	public void nameChanged(Section section) {
		reportTree.repaint();
	}
	
	/**
	 * Setup movement buttons based on current selection
	 */
	private void setupMovementButtonsForPath(TreePath tp) {
		Section selectedSection = (Section)tp.getLastPathComponent();
		if(tp.getLastPathComponent() == report || tp.getParentPath() == null) {
			// disable both
			moveUpButton.setEnabled(false);
			moveDownButton.setEnabled(false);
		} else {
			Section parentSection = (Section)tp.getParentPath().getLastPathComponent();
			int childIndex = reportTreeModel.getIndexOfChild(parentSection, selectedSection);
			
			if(childIndex == 0) {
				moveUpButton.setEnabled(false);
			} else {
				moveUpButton.setEnabled(true);
			}
			
			if(childIndex < reportTreeModel.getChildCount(parentSection)-1) {
				moveDownButton.setEnabled(true);
			} else {
				moveDownButton.setEnabled(false);
			}
		}
	}
	
	private void newReportFormat() {
		final String msg = 
			"Starting a new report will remove any current changes.  Save report design?";
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setRunAsync(false);
		props.setOptions(MessageDialogProperties.yesNoCancelOptions);
		props.setTitle("Begin New Report");
		props.setHeader("Begin New Report");
		props.setMessage(msg);
		
		int retVal = NativeDialogs.showMessageDialog(props);
		if(retVal == 0) {
			// if not saved - return
			if(!saveReportFormat()) {
				props.setMessage("Cancelled by user.");
				props.setOptions(MessageDialogProperties.okOptions);
				NativeDialogs.showMessageDialog(props);
				return;
			}
		} else {
			return;
		}
		
		ReportDesign design = (new ObjectFactory()).createReportDesign();
		design.setName("Report");
		this.report = design;
		
		sectionPanels.clear();
		panels.removeAll();
		
		SectionPanelFactory panelFactory = new SectionPanelFactory();
		addSectionPanel(report, panelFactory);
		
		reportTreeModel = new ReportTreeModel(report);
		reportTree.setModel(reportTreeModel);
	}
	
	private boolean saveReportFormat() {
		boolean hasSaved = false;
		
		String reportBaseName = getReportDesign().getName();
		String reportName = reportBaseName;
		String defExt = ".xml";
		
		FileFilter[] filters = new FileFilter[1];
		filters[0] = FileFilter.xmlFilter;
		
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setRunAsync(false);
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setTitle("Save Report Format");
		props.setInitialFile(reportName + defExt);
		
		final String saveFile = NativeDialogs.showSaveDialog(props);
		if(saveFile != null) {
			try {
				ReportIO.writeDesign(report, saveFile);
				hasSaved = true;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		return hasSaved;
	}
	
	private void openReportFormat() {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setRunAsync(false);
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(false);
		props.setCanChooseFiles(true);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setTitle("Open Report Format");
		
		final List<String> selectedFile = NativeDialogs.showOpenDialog(props);
		if(selectedFile.size() > 0) {
			try {
				ReportDesign design = ReportIO.readDesign(selectedFile.get(0));
				this.report = design;
				
				sectionPanels.clear();
//				CardLayout cl = (CardLayout)panels.getLayout();
				panels.removeAll();
				
				SectionPanelFactory panelFactory = new SectionPanelFactory();
				addSectionPanel(report, panelFactory);
				
				reportTreeModel = new ReportTreeModel(design);
				reportTree.setModel(reportTreeModel);
			} catch (IOException e) {
				ToastFactory.makeToast(e.getLocalizedMessage()).start(openButton);
				
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	/**
	 * Add a new section to the report at the given
	 * tree path.
	 */
	private void addSectionAtPath(Section section, TreePath tp) {
		Section selectedSection = (Section)tp.getLastPathComponent();
		ObjectFactory factory = new ObjectFactory();
		
		JAXBElement<Section> ele =
			new JAXBElement<Section>(
					_SECTION_QNAME, Section.class, null, section);
		if(section instanceof Group || selectedSection == report) {
			report.getReportSection().add(ele);
//			report.getReportSection().add(0, facotysection);
		} else if(selectedSection instanceof Group) {
			ele = new JAXBElement<Section>(_GROUP_SECTION_QNAME, Section.class, null, section);
			((Group)selectedSection).getGroupReportSection().add(0, ele);
		} else {
			// insert the new section in the parent group/report
			// after this one
			Section parentSection = 
				(Section)tp.getParentPath().getLastPathComponent();
			int sectionIndex = reportTreeModel.getIndexOfChild(parentSection, selectedSection);
			if(parentSection == report) {
				report.getReportSection().add(sectionIndex+1, ele);
			} else {
				Group group = (Group)parentSection;
				ele = new JAXBElement<Section>(_GROUP_SECTION_QNAME, Section.class, null, section);
				group.getGroupReportSection().add(sectionIndex+1, ele);
			}
		}
		
		// create  a section panel
		addSectionPanel(section, new SectionPanelFactory());
		TreePath changedTp =
			( selectedSection == report || selectedSection instanceof Group ? tp : tp.getParentPath() );
		if(section instanceof Group) 
			changedTp = new TreePath(report);
		reportTreeModel.fireTreeChanged(changedTp);
		
		reportTree.setSelectionPath(changedTp.pathByAddingChild(section));
		reportTree.expandPath(changedTp.pathByAddingChild(section));
	}
	
	/**
	 * Remove the given section.
	 */
	private void removeSectionAtPath(TreePath tp) {
		Section selectedSection = (Section)tp.getLastPathComponent();
		if(selectedSection == report) return;
		
		Section parentSection = (Section)tp.getParentPath().getLastPathComponent();

		int selectedChild = reportTreeModel.getIndexOfChild(parentSection, selectedSection);
		if(parentSection == report) {
			report.getReportSection().remove(selectedChild);
//			sectionPanels.remove(selectedSection);
		} else {
			Group group = (Group)parentSection;
			group.getGroupReportSection().remove(selectedChild);
//			sectionPanels.remove(selectedSection);
		}

		reportTreeModel.fireTreeChanged(tp.getParentPath());
		
		if(selectedChild == 0)
			reportTree.setSelectionPath(tp.getParentPath());
		else {
			TreePath sibPath = tp.getParentPath().pathByAddingChild(
					reportTreeModel.getChild(parentSection, selectedChild-1));
			reportTree.setSelectionPath(sibPath);
		}
	}
	
	public void onRemoveSection(PhonActionEvent pae) {
		TreePath tp = reportTree.getSelectionPath();
		if(tp != null) 
			removeSectionAtPath(tp);
	}
	
	private void moveSectionUp(TreePath tp) {
		Section selectedSection = (Section)tp.getLastPathComponent();
		if(selectedSection == report) return;
		
		Section parentSection = (Section)tp.getParentPath().getLastPathComponent();
		int selectedChild = reportTreeModel.getIndexOfChild(parentSection, selectedSection);
		if(selectedChild > 0) {
			int newIndex = selectedChild - 1;
			
			List<JAXBElement<? extends Section>> sectionList = null;
			if(parentSection == report) {
				sectionList = report.getReportSection();
			} else {
				Group group = (Group)parentSection;
				sectionList = group.getGroupReportSection();
			}
			
			sectionList.remove(selectedChild);
			
			JAXBElement<Section> ele = 
				new JAXBElement<Section>(
						(parentSection == report ? _SECTION_QNAME : _GROUP_SECTION_QNAME), 
						Section.class, null, selectedSection);
			sectionList.add(newIndex, ele);
			
			reportTreeModel.fireTreeChanged(tp.getParentPath());
			reportTree.setSelectionPath(tp);
		}
	}
	
	private void moveSectionDown(TreePath tp) {
		Section selectedSection = (Section)tp.getLastPathComponent();
		if(selectedSection == report) return;
		
		Section parentSection = (Section)tp.getParentPath().getLastPathComponent();
		int selectedChild = reportTreeModel.getIndexOfChild(parentSection, selectedSection);
		if(selectedChild < reportTreeModel.getChildCount(parentSection)-1) {
			int newIndex = selectedChild + 1;
			
			List<JAXBElement<? extends Section>> sectionList = null;
			if(parentSection == report) {
				sectionList = report.getReportSection();
			} else {
				Group group = (Group)parentSection;
				sectionList = group.getGroupReportSection();
			}
			
			sectionList.remove(selectedChild);
			
			JAXBElement<Section> ele = 
				new JAXBElement<Section>(
						(parentSection == report ? _SECTION_QNAME : _GROUP_SECTION_QNAME),
						Section.class, null, selectedSection);
			sectionList.add(newIndex, ele);
			
			reportTreeModel.fireTreeChanged(tp.getParentPath());
			reportTree.setSelectionPath(tp);
		}
	}
	
	/**
	 * Generates the appropriate popup menu
	 * for the currently selected tree item.
	 * 
	 * @return the menu or <code>null</code> if there
	 * is no current selection.
	 */
	private JPopupMenu createContextMenu() {
		JPopupMenu retVal = new JPopupMenu();
		
		final TreePath tp = reportTree.getSelectionPath();
		if(tp != null) {
			final Section selectedSection = (Section)tp.getLastPathComponent();
			
			// add default entries
			JMenuItem commentItem = new JMenuItem("Comment");
			commentItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ObjectFactory factory = new ObjectFactory();

					CommentSection commentSection = factory.createCommentSection();
//					commentSection.setFlavor(SectionFlavor.COMMENT);
					commentSection.setName("Comment");
					
					addSectionAtPath(commentSection, tp);
				}
				
			});
			retVal.add(commentItem);
			
			if( (selectedSection == report || tp.getParentPath().getLastPathComponent() == report)
					&& !(selectedSection instanceof Group) ) {
				JMenuItem paramItem = new JMenuItem("Parameter List");
				paramItem.addActionListener(new ActionListener() {
	
					@Override
					public void actionPerformed(ActionEvent arg0) {
						ObjectFactory factory = new ObjectFactory();
						
						ParamSection paramSection = factory.createParamSection();
						paramSection.setName("Parameters");
						
						addSectionAtPath(paramSection, tp);
					}
				
				});
				retVal.add(paramItem);
				
				JMenuItem summaryItem = new JMenuItem("Summary of Result Sets");
				summaryItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						ObjectFactory factory = new ObjectFactory();
						
						SummarySection summarySection = factory.createSummarySection();
						summarySection.setName("Summary");
						
						addSectionAtPath(summarySection, tp);
					}
					
				});
				retVal.add(summaryItem);
				
				JMenuItem aggregatedInventoryItem = new JMenuItem("Aggregated Inventory");
				aggregatedInventoryItem.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						ObjectFactory factory = new ObjectFactory();
						
						AggregrateInventory agInfo = factory.createAggregrateInventory();
						agInfo.setName("Aggregated Inventory");
						
						addSectionAtPath(agInfo, tp);
					}
				});
				retVal.add(aggregatedInventoryItem);
			}
			
			// if we are not on the report node and report
			// is not our parent then we are in a group
			if( (selectedSection != report && tp.getParentPath().getLastPathComponent() != report)
					|| selectedSection instanceof Group ) {
				JMenuItem inventoryItem = new JMenuItem("Inventory");
				inventoryItem.addActionListener(new ActionListener() {
	
					@Override
					public void actionPerformed(ActionEvent e) {
						ObjectFactory factory = new ObjectFactory();
						
						InventorySection invSection = factory.createInventorySection();
						invSection.setName("Inventory");
//						invSection.setFlavor(SectionFlavor.INVENTORY);
//						invSection.setType(InventoryType.RESULT);
						
						addSectionAtPath(invSection, tp);
					}
				
				});
				retVal.add(inventoryItem);
				
				JMenuItem listingItem = new JMenuItem("Result List");
				listingItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						ObjectFactory factory = new ObjectFactory();
						
						ResultListing listingSection = factory.createResultListing();
						listingSection.setName("Result List");
						
						
						
						addSectionAtPath(listingSection, tp);
					}
					
				});
				retVal.add(listingItem);
			}
			
			JMenuItem groupItem = new JMenuItem("Report Section");
			groupItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ObjectFactory factory = new ObjectFactory();
					
					Group grp = factory.createGroup();
					grp.setName("Report Section");
//					grp.setFlavor(SectionFlavor.GROUP);
					
					addSectionAtPath(grp, tp);
				}
				
			});
			retVal.add(groupItem);
			
		}
		
		return retVal;
	}
	
	private class TreeMouseListener extends MouseInputAdapter {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			if(arg0.isPopupTrigger()) {
				reportTree.setSelectionRow(reportTree.getRowForLocation(arg0.getPoint().x, arg0.getPoint().y));
				
				JPopupMenu menu = createContextMenu();
				menu.show((Component)arg0.getSource(), arg0.getPoint().x, arg0.getPoint().y);
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if(arg0.isPopupTrigger()) {
				reportTree.setSelectionRow(reportTree.getRowForLocation(arg0.getPoint().x, arg0.getPoint().y));
				
				JPopupMenu menu = createContextMenu();
				menu.show((Component)arg0.getSource(), arg0.getPoint().x, arg0.getPoint().y);
			}
		}
		
	}
}
