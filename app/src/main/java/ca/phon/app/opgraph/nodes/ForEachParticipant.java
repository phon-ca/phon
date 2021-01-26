package ca.phon.app.opgraph.nodes;

import ca.phon.formatter.FormatterFactory;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.components.canvas.NodeStyle;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.opgraph.extensions.CustomProcessing;
import ca.phon.opgraph.extensions.Publishable;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.opgraph.nodes.general.script.InputFields;
import ca.phon.opgraph.nodes.reflect.*;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.session.Participant;
import ca.phon.ui.text.FormatterTextField;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@OpNodeInfo(name="For Each Participant", description = "Iterate over a list of participants with optional table data", category = "Table", showInLibrary = true)
public class ForEachParticipant extends MacroNode implements NodeSettings, CustomProcessing, CustomProcessing.CustomProcessor {

	static {
		NodeStyle.installStyleForNode(ForEachParticipant.class, NodeStyle.ITERATION);
	}

	public static final String INPUT_TABLE_KEY_PREFIX = "_inputTable";
	public static final String CURRENT_SPEAKER_KEY = "_currentSpeaker";
	public static final String SPEAKER_TABLE_KEY_PREFIX = "_speakerTable";
	public static final String TABLE_EXISTS_KEY_PREFIX = "_tableExists";

	/** Input field for the value */
	private InputField participantsInputField = new InputField("participants", "Selected participants",
		false, true, Collection.class);

	private final static String NUM_TABLES_KEY = ForEachParticipant.class.getName() + ".numTables";
	private final static int DEFAULT_NUM_TABLES = 1;
	private int numTables = DEFAULT_NUM_TABLES;

	private List<Map<Participant, TableDataSource>> tableMaps = new ArrayList<>();

	private ParticipantTableNode tableNode;

	private static OpGraph createInitialGraph() {
		OpGraph graph = new OpGraph();

		final ContextualItemClassNode node = new ContextualItemClassNode(CURRENT_SPEAKER_KEY, Participant.class);
		node.setName("Current Speaker");
		graph.add(node);

		final ParticipantTableNode participantTableNode = new ParticipantTableNode();
		participantTableNode.setName("Speaker tables");
		graph.add(participantTableNode);

		return graph;
	}

	public ForEachParticipant() {
		this(createInitialGraph());
	}

	public ForEachParticipant(OpGraph graph) {
		super(graph);

		Optional<OpNode> tableNodeOpt =
				graph.getVertices().stream().filter( v -> v instanceof  ParticipantTableNode ).findFirst();
		if(tableNodeOpt.isPresent())
			tableNode = (ParticipantTableNode) tableNodeOpt.get();

		putField(participantsInputField);
		setupInputs();

		putExtension(CustomProcessing.class, this);
		putExtension(NodeSettings.class, this);
	}

	private void setupInputs() {
		List<InputField> toKeep = new ArrayList<>();
		toKeep.add(participantsInputField);

		for(int i = 0; i < numTables; i++) {
			String inputKey = INPUT_TABLE_KEY_PREFIX + (i+1);
			if(getInputFieldWithKey(inputKey) == null) {
				final String tableInputKey = INPUT_TABLE_KEY_PREFIX + (i + 1);
				final InputField tableInputField = new InputField(tableInputKey, "Input table # " + (i + 1),
						true, false, TableDataSource.class);
				final int inputFieldPos = i+2;
				putField(inputFieldPos, tableInputField);
				toKeep.add(tableInputField);
			} else {
				toKeep.add(getInputFieldWithKey(inputKey));
			}
		}

		toKeep.addAll(super.getPublishedInputs());

		List<InputField> oldInputs = new ArrayList<>(getInputFields());
		oldInputs.removeAll(toKeep);
		for(InputField oldInput:oldInputs) {
			removeField(oldInput);
		}
	}

	private DefaultTableDataSource copyTableSchema(TableDataSource table) {
		DefaultTableDataSource retVal = new DefaultTableDataSource();

		for(int col = 0; col < table.getColumnCount(); col++) {
			retVal.setColumnTitle(col, table.getColumnTitle(col));
		}

		return retVal;
	}

	private Map<Participant, TableDataSource> setupTableMap(Collection<Participant> participants, TableDataSource table) {
		Map<Participant, TableDataSource> retVal = new LinkedHashMap<>();

		int speakerCol = table.getColumnIndex("Speaker");
		if(speakerCol < 0) throw new IllegalArgumentException("Table must contain a 'Speaker' column");

		if(participants.contains(Participant.ALL))
			retVal.put(Participant.ALL, table);

		// split table by speaker column
		for(int row = 0; row < table.getRowCount(); row++) {
			Participant rowSpeaker = (Participant) table.getValueAt(row, speakerCol);
			if(rowSpeaker == null) continue;

			Optional<Participant> selectedSpeaker =
				participants.stream()
					.filter(p -> {
						if(p.getId().equals(rowSpeaker.getId())) {
							if(p.getName() == null && rowSpeaker.getName() == null) {
								return true;
							} else if(p.getName() != null) {
								return p.getName().equals(rowSpeaker.getName());
							} else {
								return false;
							}
						} else {
							return false;
						}
					})
					.findFirst();
			if(selectedSpeaker.isPresent()) {
				Object rowData[] = new Object[table.getColumnCount()];
				DefaultTableDataSource speakerTable = (DefaultTableDataSource) retVal.get(selectedSpeaker.get());
				if(speakerTable == null) {
					speakerTable = copyTableSchema(table);
					retVal.put(selectedSpeaker.get(), speakerTable);
				}

				for(int col = 0; col < table.getColumnCount(); col++) {
					rowData[col] = table.getValueAt(row, col);
				}
				speakerTable.addRow(rowData);
			}
		}

		return retVal;
	}

	public int getNumTables() {
		return (this.numTablesField != null ? this.numTablesField.getValue() : this.numTables);
	}

	public void setNumTables(int numTables) {
		this.numTables = numTables;
		setupInputs();
		if(this.numTablesField != null) {
			this.numTablesField.setValue(numTables);
		}
		if(this.tableNode != null) {
			this.tableNode.setNumTables(numTables);
		}
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		@SuppressWarnings("unchecked")
		Collection<Participant> participants = (Collection<Participant>) context.get(participantsInputField);

		tableMaps.clear();
		for(int i = 0; i < numTables; i++) {
			TableDataSource table = (TableDataSource) context.get(INPUT_TABLE_KEY_PREFIX + (i+1));
			if (table != null) {
				Map<Participant, TableDataSource> tableMap = setupTableMap(participants, table);
				tableMaps.add(tableMap);
			} else {
				Map<Participant, TableDataSource> tableMap = new LinkedHashMap<>();
				tableMaps.add(tableMap);
			}
		}

		// Process
		if(graph != null) {
			final Processor processor = new Processor(graph);
			for(ProcessorListener listener:getProcessorListeners())
				processor.addProcessorListener(listener);

			final Iterator<Participant> itr = participants.iterator();
			while(itr.hasNext()) {
				checkCanceled();
				processor.reset(context);

				mapInputs(context);

				final Participant currentValue = itr.next();
				context.put(CURRENT_SPEAKER_KEY, currentValue);

				for(int i = 0; i < numTables; i++) {
					Map<Participant, TableDataSource> tableMap = tableMaps.get(i);
					TableDataSource speakerTable = tableMap.get(currentValue);
					context.put(TABLE_EXISTS_KEY_PREFIX + (i+1), Boolean.valueOf(speakerTable != null));
					if (speakerTable == null)
						speakerTable = new DefaultTableDataSource();
					context.put(SPEAKER_TABLE_KEY_PREFIX + (i+1), speakerTable);
				}

				processor.stepAll();
				if(processor.getError() != null)
					throw processor.getError();

				mapOutputs(context);
			}
		}
	}

	/*
	 * Custom processing
	 */
	private Collection<Participant> processParticipants;

	private OpContext globalContext;
	private Iterator<Participant> iterator;
	private Iterator<OpNode> processIterator;
	private List<Map<Participant, TableDataSource>> processTableMaps = new ArrayList<>();
	private Participant currentValue;

	@Override
	public boolean hasNext() {
		boolean hasMoreElements = (iterator != null && iterator.hasNext());
		boolean hasMoreNodes = (processIterator != null && processIterator.hasNext());
		return hasMoreElements || hasMoreNodes;
	}

	@Override
	public OpNode next() {
		if(currentValue == null || !processIterator.hasNext()) {
			currentValue = iterator.next();
			processIterator = graph.getVertices().iterator();

			globalContext.put(CURRENT_SPEAKER_KEY, currentValue);

			for(int i = 0; i < numTables; i++) {
				Map<Participant, TableDataSource> processTableMap = processTableMaps.get(i);
				TableDataSource speakerTable = processTableMap.get(currentValue);
				globalContext.put(TABLE_EXISTS_KEY_PREFIX + (i+1), Boolean.valueOf(speakerTable != null));
				if (speakerTable == null)
					speakerTable = new DefaultTableDataSource();
				globalContext.put(SPEAKER_TABLE_KEY_PREFIX + (i+1), speakerTable);
			}
		}
		return processIterator.next();
	}

	@Override
	public void initialize(OpContext context) {
		globalContext = context;
		processParticipants = (Collection<Participant>) context.get(participantsInputField);

		processTableMaps.clear();
		for(int i = 0; i < numTables; i++) {
			TableDataSource table = (TableDataSource) context.get(INPUT_TABLE_KEY_PREFIX + (i+1));
			if (table != null) {
				Map<Participant, TableDataSource> processTableMap = setupTableMap(processParticipants, table);
				processTableMaps.add(processTableMap);
			} else {
				Map<Participant, TableDataSource> processTableMap = new LinkedHashMap<>();
				processTableMaps.add(processTableMap);
			}
		}

		mapInputs(context);

		iterator = processParticipants.iterator();
		if(graph != null) {
			processIterator = graph.getVertices().iterator();
		}
	}

	@Override
	public void terminate(OpContext context) {
	}

	@Override
	public CustomProcessor getCustomProcessor() {
		return this;
	}

	/* Node Settings */
	private JPanel settingsPanel;
	private FormatterTextField<Integer> numTablesField;

	@Override
	public Component getComponent(GraphDocument graphDocument) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new VerticalLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			JPanel p = new JPanel(new GridBagLayout());
			p.add(new JLabel("Number of tables: "), gbc);
			gbc.gridx++;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			numTablesField = new FormatterTextField<>(FormatterFactory.createFormatter(Integer.class));
			numTablesField.setValue(numTables);
			p.add(numTablesField);

			numTablesField.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {

				}

				@Override
				public void focusLost(FocusEvent e) {
					Integer numTables = numTablesField.getValue();
					if(numTables != null && numTables >= 0) {
						setNumTables(numTables);
					}
				}

			});

			settingsPanel.add(p);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		retVal.put(NUM_TABLES_KEY, getNumTables());
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey(NUM_TABLES_KEY)) {
			this.numTables = Integer.parseInt(properties.getProperty(NUM_TABLES_KEY, "1"));
			setNumTables(numTables);
		}
	}

	@OpNodeInfo(name="Participant Tables", description = "Tables for current participant", category = "Table", showInLibrary = false)
	public static class ParticipantTableNode extends OpNode {

		private int numTables;

		public ParticipantTableNode() {
			super();
			this.numTables = 1;
			setupOutputs();
		}

		public int getNumTables() {
			return this.numTables;
		}

		public void setNumTables(int numTables) {
			this.numTables = numTables;
			setupOutputs();
		}

		private void setupOutputs() {
			List<OutputField> outputFields = new ArrayList<>();
			for(int i = 0; i < numTables; i++) {
				String speakerKey = SPEAKER_TABLE_KEY_PREFIX + (i+1);
				String existsKey = TABLE_EXISTS_KEY_PREFIX + (i+1);

				if(getOutputFieldWithKey(speakerKey) == null) {
					final OutputField outputField = new OutputField(speakerKey,
							"Table #" + (i + 1), false, TableDataSource.class);
					final OutputField existsField = new OutputField(existsKey,
							"Does Table #" + (i + 1) + " have data", false, Boolean.class);

					outputFields.add(existsField);
					outputFields.add(outputField);

					putField(existsField);
					putField(outputField);
				} else {
					outputFields.add(getOutputFieldWithKey(existsKey));
					outputFields.add(getOutputFieldWithKey(speakerKey));
				}
			}

			List<OutputField> oldOutputs = new ArrayList<>(getOutputFields());
			oldOutputs.removeAll(outputFields);
			for(OutputField oldOutput:oldOutputs) {
				removeField(oldOutput);
			}
		}

		@Override
		public void operate(OpContext opContext) throws ProcessingException {
			// no need to do anything, output fields already use keys found in context
		}

	}

}