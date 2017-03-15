package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.nodes.project.SessionSelectorNode;
import ca.phon.project.Project;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.resources.ResourceLoader;

public class AnalysisGraphGeneratorPanel extends JPanel {

	private JButton addButton;
	private JButton removeButton;
	private TristateCheckBoxTree analysisTree;

	private JButton moveUpButton;
	private JButton moveDownButton;
	private JList<OpNode> analysisNodeList;
	private List<OpNode> analysisNodes;

	private JComboBox<OpNode> settingsNodeBox;
	private CardLayout settingsLayout;
	private JPanel settingsPanel;

	private final AnalysisOpGraphEditorModel model;

	private final Project project;

	/**
	 * Constructor
	 *
	 * @param project if <code>null</code> project graphs will not be displayed
	 */
	public AnalysisGraphGeneratorPanel(Project project) {
		super();

		final EditorModelInstantiator instantiator = new AnalysisEditorModelInstantiator();
		model = (AnalysisOpGraphEditorModel)instantiator.createModel(new OpGraph());

		this.project = project;

		init();
	}

	private void init() {
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		final JPanel p1 = new JPanel(new GridBagLayout());

		final PhonUIAction addAct = new PhonUIAction(this, "onAdd");
		addAct.putValue(PhonUIAction.NAME, "Add >");
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add checked analyses");
		addButton = new JButton(addAct);

		final PhonUIAction removeAct = new PhonUIAction(this, "onRemove");
		removeAct.putValue(PhonUIAction.NAME, "< Remove");
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected analses");
		removeButton = new JButton(removeAct);

		final TristateCheckBoxTreeModel analysisTreeModel = createTreeModel();
		analysisTree = new TristateCheckBoxTree(analysisTreeModel);
		final JScrollPane analysisScroller = new JScrollPane(analysisTree);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		gbc.gridheight = 3;
		p1.add(analysisScroller, gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		p1.add(addButton, gbc);

		gbc.gridy = 1;
		p1.add(removeButton, gbc);

		final JPanel p2 = new JPanel(new GridBagLayout());

		final PhonUIAction upAct = new PhonUIAction(this, "onMoveUp");
		upAct.putValue(PhonUIAction.NAME, "Up");
		upAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected analysis up");
		moveUpButton = new JButton(upAct);

		final PhonUIAction downAct = new PhonUIAction(this, "onMoveDown");
		downAct.putValue(PhonUIAction.NAME, "Down");
		downAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected analysis down");
		moveDownButton = new JButton(downAct);

		analysisNodes = new ArrayList<>();
		analysisNodeList = new JList<>(new AnalysisListModel());
		analysisNodeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		analysisNodeList.addListSelectionListener( (e) -> {

		});
		final JScrollPane analysisNodeScroller = new JScrollPane(analysisNodeList);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		gbc.gridheight = 3;
		p2.add(analysisNodeScroller, gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		p2.add(moveUpButton, gbc);

		gbc.gridy = 1;
		p2.add(moveDownButton, gbc);

		final JPanel p3 = new JPanel(new BorderLayout());

		this.settingsNodeBox = new JComboBox<>();
		this.settingsLayout = new CardLayout();
		this.settingsPanel = new JPanel(settingsLayout);

		p3.add(settingsNodeBox, BorderLayout.NORTH);
		p3.add(settingsPanel, BorderLayout.CENTER);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.5;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		add(p1, gbc);
		++gbc.gridx;
		add(p2, gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		add(p3, gbc);
	}

	public void onAdd() {

	}

	public void onRemove() {

	}

	private TristateCheckBoxTreeModel createTreeModel() {
		final TristateCheckBoxTreeNode root =
				new TristateCheckBoxTreeNode("All Analyses", TristateCheckBoxState.UNCHECKED, true, false);

		final AnalysisLibrary library = new AnalysisLibrary();
		final ResourceLoader<URL> stockAnalysisLoader = library.getStockGraphs();
		final Iterator<URL> stockItr = stockAnalysisLoader.iterator();
		if(stockItr.hasNext()) {
			final TristateCheckBoxTreeNode stockNode =
					new TristateCheckBoxTreeNode("Stock Analyses", TristateCheckBoxState.UNCHECKED, true, false);
			while(stockItr.hasNext()) {
				final URL analysisURL = stockItr.next();

				try {
					final String fullPath = URLDecoder.decode(analysisURL.getPath(), "UTF-8");
					String relativePath =
							fullPath.substring(fullPath.indexOf(AnalysisLibrary.ANALYSIS_FOLDER + "/")+AnalysisLibrary.ANALYSIS_FOLDER.length()+1);

					TristateCheckBoxTreeNode parentNode = stockNode;
					int splitIdx = -1;
					while((splitIdx = relativePath.indexOf('/')) >= 0) {
						final String nodeName = relativePath.substring(0, splitIdx);

						TristateCheckBoxTreeNode node = null;
						for(int i = 0; i < parentNode.getChildCount(); i++) {
							final TristateCheckBoxTreeNode childNode = (TristateCheckBoxTreeNode)parentNode.getChildAt(i);
							if(childNode.getUserObject().equals(nodeName)) {
								node = childNode;
								break;
							}
						}
						if(node == null) {
							node = new TristateCheckBoxTreeNode(nodeName, TristateCheckBoxState.UNCHECKED, true, false);
							parentNode.add(node);
						}
						parentNode = node;
					}

					final TristateCheckBoxTreeNode analysisNode =
							new TristateCheckBoxTreeNode(analysisURL, TristateCheckBoxState.UNCHECKED, false, false);
					parentNode.add(analysisNode);
				} catch (UnsupportedEncodingException e) {

				}
			}
			root.add(stockNode);
		}

		// TODO user graphs

		// TODO project graphs

		return new TristateCheckBoxTreeModel(root);
	}

	public AnalysisOpGraphEditorModel getModel() {
		return this.model;
	}

	public OpGraph getGraph() {
		return this.model.getDocument().getGraph();
	}

	private class AnalysisListModel extends AbstractListModel<OpNode> {

		@Override
		public int getSize() {
			return analysisNodes.size();
		}

		@Override
		public OpNode getElementAt(int index) {
			return analysisNodes.get(index);
		}

	}

}
