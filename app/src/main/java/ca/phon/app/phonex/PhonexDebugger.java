package ca.phon.app.phonex;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;

import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.FSATransition;
import ca.phon.fsa.SimpleFSADebugContext;
import ca.phon.fsa.FSAState.RunningState;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.phonex.PhonexFSA;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.phonex.PhonexPatternException;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.text.PatternEditor;
import ca.phon.ui.text.PatternEditor.SyntaxStyle;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class PhonexDebugger extends JComponent {

	private PatternEditor phonexEntry;	
	
	private JPanel debugPanel;
	private JButton matchButton;
	private JButton findButton;
	private JButton nextInstanceButton;
	private JButton stepButton;
	private JButton stopButton;
	
	private JTextField transcriptionField;
	
	private GroupDataTableModel groupDataTableModel;
	private JTable groupDataTable;

	private SyllabificationDisplay ipaDisplay;
	
	private JScrollPane graphScroller;
	private JLabel graphLbl;
	
	private PhonexPattern pattern;
	private SimpleFSADebugContext<IPAElement> debugCtx;
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
		transcriptionField.setFont(FontPreferences.getUIIpaFont());
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

		groupDataTableModel = new GroupDataTableModel();
		groupDataTable = new JTable(groupDataTableModel);
		
		matchButton = new JButton(new MatchAction(this));
		findButton = new JButton(new FindAction(this));
		stepButton = new JButton(new StepAction(this));
		stopButton = new JButton(new StopAction(this));

		JPanel buttonPanel = new JPanel(new HorizontalLayout());
		buttonPanel.add(matchButton);
		buttonPanel.add(findButton);
		buttonPanel.add(stepButton);
		buttonPanel.add(stopButton);
		
		ipaDisplay = new SyllabificationDisplay();
		
		JPanel debugPanel = new JPanel(new VerticalLayout());
		debugPanel.add(transcriptionField);
		debugPanel.add(ipaDisplay);
		debugPanel.add(buttonPanel);
		debugPanel.add(new JScrollPane(groupDataTable));
		
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.add(scroller, BorderLayout.CENTER);
		westPanel.add(debugPanel, BorderLayout.SOUTH);
		
		add(westPanel, BorderLayout.WEST);
		
		graphLbl = new JLabel();
		graphLbl.setHorizontalAlignment(SwingConstants.CENTER);
		graphScroller = new JScrollPane(graphLbl);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(graphScroller, BorderLayout.CENTER);
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	private void updateSyllabificationView() {
		try {
			IPATranscript transcript = IPATranscript.parseIPATranscript(transcriptionField.getText());
			// TODO syllabify
			ipaDisplay.setTranscript(transcript);
		} catch (ParseException e) {
			ipaDisplay.setTranscript(null);
		}
	}
	
	private void updatePattern() {
		try {
			pattern = PhonexPattern.compile(phonexEntry.getText());
			PhonexFSA fsa = pattern.getFsa();
			
			MutableGraph g = new Parser().read(fsa.getDotText());
			Renderer r = Graphviz.fromGraph(g).render(Format.PNG);
			ImageIcon icn = new ImageIcon(r.toImage());
			graphLbl.setIcon(icn);
		} catch (PhonexPatternException | IOException e) {
			graphLbl.setIcon(null);
		}
		
		debugCtx = null;
		
		groupDataTableModel.fireTableDataChanged();
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
		
		debugCtx = new SimpleFSADebugContext<IPAElement>(pattern.getFsa());
		debugCtx.reset(ipaDisplay.getTranscript().toList().toArray(new IPAElement[0]));
		
		groupDataTableModel.fireTableDataChanged();
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
		// TODO update display....
	}
	
	public void onStep() {
		if(debugCtx == null || debugCtx.getMachineState().getRunningState() != RunningState.Running) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		
		FSATransition<IPAElement> transition = debugCtx.step();
		if(transition == null) {
			onStop();
		}
		groupDataTableModel.fireTableDataChanged();
		
		// TODO Update other items
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
