/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.phonex;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.treetable.*;

import ca.phon.app.syllabifier.*;
import ca.phon.fsa.*;
import ca.phon.fsa.FSAState.*;
import ca.phon.fsa.SimpleFSA.*;
import ca.phon.ipa.*;
import ca.phon.phonex.*;
import ca.phon.syllabifier.*;
import ca.phon.ui.fonts.*;
import ca.phon.ui.text.*;
import ca.phon.ui.text.PatternEditor.*;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.*;
import guru.nidi.graphviz.parse.*;

public class PhonexDebugger extends JComponent {

	private PatternEditor phonexEntry;	
	
	private JPanel debugPanel;
	private JButton matchButton;
	private JButton findButton;
	private JButton nextInstanceButton;
	private JButton stepButton;
	private JButton stopButton;
	
	private JTextField transcriptionField;
	private SyllabifierComboBox syllabifierBox;
	
	private JTabbedPane tabPane;
	private GroupDataTableModel groupDataTableModel;
	private JTable groupDataTable;

	private JXTreeTable detailsTable;
	
	private DebuggerSyllabificationDisplay ipaDisplay;
	
	private JScrollPane graphScroller;
	private JLabel graphLbl;
	
	private PhonexPattern pattern;
	private SimpleFSADebugContext<IPAElement> debugCtx;
	private FSATransition<IPAElement> lastTransition = null;
	// used during find operation
	private int currentInstance = -1;
	private List<Integer> instances = new ArrayList<>();
	
	public PhonexDebugger() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		phonexEntry = new PatternEditor(SyntaxStyle.PHONEX);
		phonexEntry.setColumns(80);
		phonexEntry.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updatePattern();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updatePattern();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
			}
			
		});
		phonexEntry.setSyntaxEditingStyle("text/phonex");
		RTextScrollPane scroller = new RTextScrollPane(phonexEntry);
		scroller.setLineNumbersEnabled(true);
		scroller.setBorder(BorderFactory.createTitledBorder("phonex"));
		ErrorStrip strip = new ErrorStrip(phonexEntry);
		strip.setOpaque(false);
		
		transcriptionField = new JTextField();
		transcriptionField.setFont(FontPreferences.getTierFont());
		transcriptionField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateSyllabificationView();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateSyllabificationView();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
			}
			
		});
		
		syllabifierBox = new SyllabifierComboBox();

		groupDataTableModel = new GroupDataTableModel();
		groupDataTable = new JTable(groupDataTableModel);
		
		detailsTable = new JXTreeTable();
		updateDetailsTreeTable();
		
		matchButton = new JButton(new MatchAction(this));
		findButton = new JButton(new FindAction(this));
		stepButton = new JButton(new StepAction(this));
		stopButton = new JButton(new StopAction(this));

		JPanel buttonPanel = new JPanel(new HorizontalLayout());
		buttonPanel.add(matchButton);
		buttonPanel.add(findButton);
		buttonPanel.add(stepButton);
		buttonPanel.add(stopButton);
		
		ipaDisplay = new DebuggerSyllabificationDisplay();
		ipaDisplay.setUI(new DebuggerSyllabificationUI(ipaDisplay));
		
		debugPanel = new JPanel(new VerticalLayout());
		debugPanel.setBorder(BorderFactory.createTitledBorder("Debug"));
		debugPanel.add(transcriptionField);
		debugPanel.add(syllabifierBox);
		debugPanel.add(buttonPanel);
		
		tabPane = new JTabbedPane();
		tabPane.addTab("Group Data", new JScrollPane(groupDataTable));
		tabPane.addTab("Details", new JScrollPane(detailsTable));
		debugPanel.add(tabPane);
		
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.add(scroller, BorderLayout.CENTER);
		westPanel.add(debugPanel, BorderLayout.SOUTH);
		
		add(westPanel, BorderLayout.WEST);
		
		graphLbl = new JLabel();
		graphLbl.setHorizontalAlignment(SwingConstants.CENTER);
		graphScroller = new JScrollPane(graphLbl);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(ipaDisplay, BorderLayout.NORTH);
		centerPanel.add(graphScroller, BorderLayout.CENTER);
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	private void updateSyllabificationView() {
		try {
			IPATranscript transcript = IPATranscript.parseIPATranscript(transcriptionField.getText());
			Syllabifier syllabifier = (Syllabifier)syllabifierBox.getSelectedItem();
			if(syllabifier != null) {
				syllabifier.syllabify(transcript.toList());
			}
			ipaDisplay.setTranscript(transcript);
		} catch (ParseException e) {
			ipaDisplay.setTranscript(null);
		}
	}
	
	private void updatePattern() {
		try {
			pattern = PhonexPattern.compile(phonexEntry.getText());
			updateGraph();
		} catch (PhonexPatternException e) {
			graphLbl.setIcon(null);
		}
		
		debugCtx = null;
		
		groupDataTableModel.fireTableDataChanged();
	}
	
	private void updateGraph() {
		if(pattern == null) {
			return;
		}
		PhonexFSA fsa = pattern.getFsa();
		try {
			MutableGraph g = new Parser().read(getDotText(fsa));
			g.nodeAttrs().add(Color.WHITE.fill());	
			Renderer r = Graphviz.fromGraph(g).render(Format.PNG);
			ImageIcon icn = new ImageIcon(r.toImage());
			graphLbl.setIcon(icn);
		} catch (IOException e) {
			graphLbl.setIcon(null);
		}
	}
	
	private String getDotText(SimpleFSA<IPAElement> fsa) {
		if(debugCtx != null) {
			Color stateColor = Color.BLUE;
			switch(debugCtx.getMachineState().getRunningState()) {
			case Running:
				break;
				
			case Halted:
				stateColor = Color.INDIANRED;
				break;
				
			case EndOfInput:
				stateColor = (fsa.isFinalState(debugCtx.getMachineState().getCurrentState())
						? Color.SPRINGGREEN
								: Color.INDIANRED);
				break;
			}
			String highlightedState = debugCtx.getMachineState().getCurrentState();
			Color transitionColor = stateColor;
		
			String retVal = "digraph G {\n";
			
			for(String state:fsa.getStates()) {
				String stateDesc = "\t" + state + " [color=\"" + (state.equals(highlightedState) ? stateColor.value : "black") + "\""
						+ ",shape=\"" + (fsa.isFinalState(state) ? "doublecircle" : "circle") + "\""
						+ (state.equals(highlightedState) ? ",style=\"bold\"" : "")
						+ "]\n";
				retVal += stateDesc;
			}
			
			for(FSATransition<IPAElement> transition:fsa.getTransitions()) {
				retVal += "edge [color=\"" + (debugCtx.getTransitions().contains(transition) ? transitionColor.value : "black") + "\""
						+ "];\n";
				String transLbl = new String();
				for(int initGrp:transition.getInitGroups()) {
					transLbl += (transLbl.length() == 0 ? "[" : ",") + "+" + initGrp;
				}
				for(int grpIdx:transition.getMatcherGroups()) {
					transLbl += (transLbl.length() == 0 ? "[" : ",") + grpIdx;
				}
				transLbl += (transLbl.length() > 0 ? "]":"");
				transLbl = transition.getImage() + (transLbl.length() > 0 ? " " + transLbl : "");
				String transDesc = "\t" + transition.getFirstState() + " -> " +
					transition.getToState() + " ["
							+ "fontcolor=\"" + (debugCtx.getTransitions().contains(transition) ? transitionColor.value : "black") + "\"," 		
					+ "label=\"" + transLbl + "\""
							+ "];\n";
				retVal += transDesc;
			}
			
			retVal += "}\n";
			return retVal;
		} else {
			return fsa.getDotText();
		}
	}
	
	/* Actions */
	public void onMatch() {
		if(pattern == null || ipaDisplay.getTranscript() == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		
		matchButton.setEnabled(false);
		findButton.setEnabled(false);
		
		stepButton.setEnabled(true);
		stopButton.setEnabled(true);
		
		transcriptionField.setEnabled(false);
		phonexEntry.setEnabled(false);
		ipaDisplay.setEnabled(false);
		ipaDisplay.setDebugIndex(0);
		
		debugCtx = new SimpleFSADebugContext<IPAElement>(pattern.getFsa());
		debugCtx.reset(ipaDisplay.getTranscript().toList().toArray(new IPAElement[0]));
		
		groupDataTableModel.fireTableDataChanged();
		
		updateDetailsTreeTable();
		updateGraph();
		// TODO update display....
	}
	
	public void onFind() {
		if(pattern == null || ipaDisplay.getTranscript() == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		
		PhonexMatcher matcher = pattern.matcher(ipaDisplay.getTranscript());
		instances.clear();
		currentInstance = -1;
		while(matcher.find()) {
			instances.add(matcher.start());
		}
		
		if(instances.size() == 0) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		currentInstance = 0;
		
		matchButton.setEnabled(false);
		findButton.setEnabled(false);
		
		stepButton.setEnabled(true);
		stopButton.setEnabled(true);
		
		transcriptionField.setEnabled(false);
		phonexEntry.setEnabled(false);
		ipaDisplay.setEnabled(false);
		
		debugCtx = new SimpleFSADebugContext<IPAElement>(pattern.getFsa());
		debugCtx.reset(ipaDisplay.getTranscript().toList().toArray(new IPAElement[0]));
		debugCtx.getMachineState().setTapeIndex(instances.get(currentInstance));
		
		groupDataTableModel.fireTableDataChanged();
		
		updateDetailsTreeTable();
		updateGraph();
	}
	
	public void onStep() {
		if(debugCtx == null || debugCtx.getMachineState().getRunningState() != RunningState.Running) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		
		lastTransition = debugCtx.step();
		if(lastTransition == null) {
			ipaDisplay.setDebugIndex(-1);
			onStop();
		}
		groupDataTableModel.fireTableDataChanged();
		
		ipaDisplay.setDebugIndex(debugCtx.getMachineState().getTapeIndex());
		
		updateDetailsTreeTable();
		updateGraph();
	}
	
	public void onStop() {
		// finished execution of machine
		matchButton.setEnabled(true);
		findButton.setEnabled(true);
		
		stepButton.setEnabled(false);
		stopButton.setEnabled(false);
		
		transcriptionField.setEnabled(true);
		phonexEntry.setEnabled(true);
		ipaDisplay.setEnabled(true);
		ipaDisplay.setDebugIndex(-1);
	}
	
	private void updateDetailsTreeTable() {
		TreeTableNode rootNode = createDetailsTree();
		DefaultTreeTableModel model = new DefaultTreeTableModel(rootNode);
		model.setColumnIdentifiers(List.of("Name", "Value"));
		
		detailsTable.setTreeTableModel(model);
	}
	
	private TreeTableNode createDetailsTree() {
		if(debugCtx != null) {
			DetailsTreeTableNode root = new DetailsTreeTableNode("Debug Context");
		
			// add machine state
			setupMachineStateNode(root, debugCtx.getMachineState());
			
			if(debugCtx.getCachedState().getRunningState() == RunningState.EndOfInput && 
					debugCtx.getCachedState().getTapeIndex() >= debugCtx.getMachineState().getTapeIndex()) {
				DetailsTreeTableNode longestMatchNode = new DetailsTreeTableNode("Cached match");
				setupMachineStateNode(longestMatchNode, debugCtx.getCachedState());
				longestMatchNode.setValueAt(Arrays.toString(debugCtx.getCachedState().getGroup(0)), 1);
				root.add(longestMatchNode);
			}
			
			DetailsTreeTableNode decisionsNode = new DetailsTreeTableNode("Decision Stack");
			decisionsNode.setValueAt(debugCtx.getDecisionStack().size(), 1);
			for(int i = debugCtx.getDecisionStack().size()-1; i >= 0; i--) {
				DecisionTracker<IPAElement> dt = debugCtx.getDecisionStack().get(i);
				DetailsTreeTableNode decisionNode = new DetailsTreeTableNode(String.format("[%d]", i));
				setupDecisionTrackerNode(decisionNode, dt);
				decisionsNode.add(decisionNode);
			}
			if(debugCtx.getDecisionStack().size() > 0)
				root.add(decisionsNode);
			
			return root;
		} else {
			return new DefaultMutableTreeTableNode("No active debug context");
		}
	}
	
	private void setupDecisionTrackerNode(DetailsTreeTableNode root, DecisionTracker<IPAElement> decision) {
		root.setValueAt(decision.choices.get(decision.choiceIndex).getImage(), 1);
		
		DetailsTreeTableNode stateNode = new DetailsTreeTableNode("State");
		stateNode.setValueAt(decision.choices.get(0).getFirstState(), 1);
		root.add(stateNode);
		
		DetailsTreeTableNode tapeIndexNode = new DetailsTreeTableNode("Tape index");
		tapeIndexNode.setValueAt(decision.tapeIndex, 1);
		root.add(tapeIndexNode);
		
		DetailsTreeTableNode choicesNode = new DetailsTreeTableNode("Choices");
		choicesNode.setValueAt(decision.choices.size(), 1);
		for(FSATransition<IPAElement> choice:decision.choices) {
			DetailsTreeTableNode choiceNode = new DetailsTreeTableNode(choice.getImage());
			if(decision.choices.get(decision.choiceIndex) == choice) {
				choiceNode.setValueAt("\u2713", 1);
			}
			choicesNode.add(choiceNode);
		}
		root.add(choicesNode);
	}
	
	private void setupMachineStateNode(DetailsTreeTableNode root, FSAState<IPAElement> machineState) {
		DetailsTreeTableNode tapeNode = new DetailsTreeTableNode("Tape");
		tapeNode.detailsValue = Arrays.toString(machineState.getTape());
		for(int i = 0; i < machineState.getTape().length; i++) {
			DetailsTreeTableNode tapeEleNode = new DetailsTreeTableNode(String.format("[%d]", i));
			tapeEleNode.detailsValue = machineState.getTape()[i];
			tapeNode.add(tapeEleNode);
		}
		root.add(tapeNode);
		
		DetailsTreeTableNode tapeLengthNode = new DetailsTreeTableNode("Tape length");
		tapeLengthNode.setValueAt(machineState.getTape().length, 1);
		root.add(tapeLengthNode);
		
		DetailsTreeTableNode tapeIndexNode = new DetailsTreeTableNode("Tape index");
		tapeIndexNode.detailsValue = machineState.getTapeIndex();
		root.add(tapeIndexNode);
		
		DetailsTreeTableNode runningState = new DetailsTreeTableNode("Execution state");
		runningState.setValueAt(machineState.getRunningState(), 1);
		root.add(runningState);
		
		DetailsTreeTableNode currentStateNode = new DetailsTreeTableNode("Current node");
		currentStateNode.setValueAt(machineState.getCurrentState(), 1);
		root.add(currentStateNode);
		
		DetailsTreeTableNode lastTransitionNode = new DetailsTreeTableNode("Last transition");
		if(lastTransition != null) {
			lastTransitionNode.setValueAt(String.format("%s -- %s -> %s", lastTransition.getFirstState(), lastTransition.getImage(), lastTransition.getToState()), 1);
		}
		root.add(lastTransitionNode);
		
		DetailsTreeTableNode lookBehindOffset = new DetailsTreeTableNode("Look-behind offset");
		lookBehindOffset.setValueAt(machineState.getLookBehindOffset(), 1);
		root.add(lookBehindOffset);
		
		DetailsTreeTableNode lookAheadOffset = new DetailsTreeTableNode("Look-ahead offset");
		lookAheadOffset.setValueAt(machineState.getLookAheadOffset(), 1);
		root.add(lookAheadOffset);
		
		DetailsTreeTableNode groupNode = new DetailsTreeTableNode("Groups");
		groupNode.setValueAt(machineState.numberOfGroups(), 1);
		for(int i = 0; i <= machineState.numberOfGroups(); i++) {
			DetailsTreeTableNode gNode = new DetailsTreeTableNode(String.format("[%d]", i));
			gNode.setValueAt(Arrays.toString(machineState.getGroup(i)), 1);
			groupNode.add(gNode);
		}
		root.add(groupNode);
	}
	
	private class DetailsTreeTableNode extends AbstractMutableTreeTableNode {
		
		private Object detailsValue;
		
		public DetailsTreeTableNode() {
			this("");
		}
		
		public DetailsTreeTableNode(Object userObj) {
			super(userObj, true);
		}
		
		@Override
		public Object getValueAt(int column) {
			switch(column) {
			case 0:
				return getUserObject();
				
			case 1:
				return detailsValue;
				
			default:
				return getUserObject();
			}
		}

		@Override
		public void setValueAt(Object value, int col) {
			switch(col) {
			case 0:
				setUserObject(value);
				break;
				
			case 1:
				detailsValue = value;
				break;
			
			default:
				setUserObject(value);
			}
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
	}
	
	private class GroupDataTableModel extends AbstractTableModel {

		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0:
				return "Group Number (Name)";
			
			case 1:
				return "Value";
				
			default:
				return super.getColumnName(column);
			}
		}

		@Override
		public int getRowCount() {
			if(pattern != null) {
				return pattern.numberOfGroups() + 1;
			}
			return 0;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			int grpNum = rowIndex;
			switch(columnIndex) {
			case 0:
				String colName = grpNum + "";
				String grpName = pattern.groupName(grpNum);
				if(grpName != null && grpName.strip().length() > 0)
					colName += " (" + grpName + ")";
				return colName;
				
			case 1:
				if(debugCtx != null) {
					FSAState<IPAElement> machineState = debugCtx.getMachineState();
					IPAElement[] grpElements = machineState.getGroup(grpNum);
					if(grpElements != null && grpElements.length > 0) {
						return (new IPATranscriptBuilder()).append(Arrays.asList(grpElements)).toIPATranscript().toString();
					}
				}
				return "";
				
			default:
				return null;
			}
		}
		
	}
	
}
