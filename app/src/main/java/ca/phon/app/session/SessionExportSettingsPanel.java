package ca.phon.app.session;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.query.OpenResultSetSelector;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.project.Project;
import ca.phon.query.db.ReportHelper;
import ca.phon.query.db.ResultSet;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * UI for editing session export settings.
 * 
 */
public class SessionExportSettingsPanel extends JPanel {

	private Project project;
	
	private Session session;
	
	private SessionExportSettings settings;
	
	private RecordFilterPanel recordFilterPanel;
	
	private JCheckBox includeParticipantInfoBox;
	private boolean participantInfoDefaults[] = {
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false
	};
	private String participantInfoParamIds[] = {
			"includeRole",
			"includeAge",
			"includeBirthday",
			"includeSex",
			"includeLanguage",
			"includeGroup",
			"includeEducation",
			"includeSES"
	};
	private JCheckBox includeRole;
	private JCheckBox includeAge;
	private JCheckBox includeBirthday;
	private JCheckBox includeSex;
	private JCheckBox includeLanguage;
	private JCheckBox includeGroup;
	private JCheckBox includeEducation;
	private JCheckBox includeSES;
	
	private TierViewTableModel tableModel;
	private JXTable tierViewTable;
	private JButton moveTierUpBtn;
	private JButton moveTierDownBtn;
	
	private JCheckBox includeSyllabificationBox;
	private SyllabificationDisplay syllabificationDisplay;
	
	private JCheckBox includeAlignmentBox;
	private PhoneMapDisplay alignmentDisplay;
	
	private JCheckBox includeQueryResults;
	private JCheckBox filterUsingQueryResults;
	private OpenResultSetSelector resultSetSelector;
	private JXTable resultSetValuesTable;
	
	public SessionExportSettingsPanel(Project project, Session session, SessionExportSettings settings) {
		super();
		
		this.project = project;
		this.session = session;
		this.settings = settings;
		
		init();
	}
	
	private void init() {
		final List<TierViewItem> tierView = new ArrayList<>();
		final SessionFactory factory = SessionFactory.newFactory();
		session.getTierView().forEach( (tvi) -> {
			final TierViewItem item = factory.createTierViewItem(tvi.getTierName(), tvi.isVisible(), tvi.getTierFont());
			tierView.add(item);
		});
		
		recordFilterPanel = new RecordFilterPanel(project, session);
		recordFilterPanel.getQueryOptionsPanel().setVisible(false);
		recordFilterPanel.setBorder(BorderFactory.createTitledBorder("Record Filter"));
		
		includeParticipantInfoBox = new JCheckBox("Include participant information");
		includeParticipantInfoBox.addActionListener( (e) -> {
			var enabled = includeParticipantInfoBox.isSelected();
			includeRole.setEnabled(enabled);
			includeAge.setEnabled(enabled);
			includeBirthday.setEnabled(enabled);
			includeSex.setEnabled(enabled);
			includeLanguage.setEnabled(enabled);
			includeGroup.setEnabled(enabled);
			includeEducation.setEnabled(enabled);
			includeSES.setEnabled(enabled);
		});
		includeParticipantInfoBox.setSelected(settings.isIncludeParticipantInfo());
		
		includeRole = new JCheckBox("Role");
		includeRole.setSelected(settings.isIncludeRole());

		includeAge = new JCheckBox("Age");
		includeAge.setSelected(settings.isIncludeAge());
		
		includeBirthday = new JCheckBox("Birthday");
		includeBirthday.setSelected(settings.isIncludeBirthday());
		
		includeSex = new JCheckBox("Sex");
		includeSex.setSelected(settings.isIncludeSex());
		
		includeLanguage = new JCheckBox("Language");
		includeLanguage.setSelected(settings.isIncludeLanguage());
		
		includeGroup = new JCheckBox("Group");
		includeGroup.setSelected(settings.isIncludeGroup());
		
		includeEducation = new JCheckBox("Education");
		includeEducation.setSelected(settings.isIncludeEducation());
		
		includeSES = new JCheckBox("SES");
		includeSES.setSelected(settings.isIncludeSES());

		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		checkBoxPanel.add(includeRole);
		checkBoxPanel.add(includeAge);
		checkBoxPanel.add(includeBirthday);
		checkBoxPanel.add(includeSex);
		checkBoxPanel.add(includeLanguage);
		checkBoxPanel.add(includeGroup);
		checkBoxPanel.add(includeEducation);
		checkBoxPanel.add(includeSES);
		
		JPanel participantOptionsPanel = new JPanel(new VerticalLayout());
		participantOptionsPanel.setBorder(BorderFactory.createTitledBorder("Participant Options"));
		participantOptionsPanel.add(includeParticipantInfoBox);
		participantOptionsPanel.add(checkBoxPanel);
		
		tableModel = new TierViewTableModel(tierView);
		tierViewTable = new JXTable(tableModel);
		tierViewTable.setVisibleRowCount(7);
		tierViewTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		final JScrollPane tableScroller = new JScrollPane(tierViewTable);
		
		final PhonUIAction moveUpAct = new PhonUIAction(this, "moveTierUp");
		moveUpAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier up");
		moveUpAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-up", IconSize.SMALL));
		moveTierUpBtn = new JButton(moveUpAct);
		
		final PhonUIAction moveDownAct = new PhonUIAction(this, "moveTierDown");
		moveDownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier down");
		moveDownAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-down", IconSize.SMALL));
		moveTierDownBtn = new JButton(moveDownAct);
		
		JPanel btnPanel = new JPanel(new VerticalLayout());
		btnPanel.add(moveTierUpBtn);
		btnPanel.add(moveTierDownBtn);
		
		JPanel tierTablePanel = new JPanel(new BorderLayout());
		tierTablePanel.add(tableScroller, BorderLayout.CENTER);
		tierTablePanel.add(btnPanel, BorderLayout.EAST);
		
		includeSyllabificationBox = new JCheckBox("Include syllabification");
		includeSyllabificationBox.setSelected(settings.isIncludeSyllabification());
		
		final IPATranscript ipaT = (new IPATranscriptBuilder()).append("ˈb:Oʌ:Nɹ:Cθ:Cˌd:Oe͜ɪ:N").toIPATranscript();
		final IPATranscript ipaA = (new IPATranscriptBuilder()).append("ˈb:Oʌː:Nˌt:Oe͜ɪ:N").toIPATranscript();
		final PhoneMap alignment = (new PhoneAligner()).calculatePhoneAlignment(ipaT, ipaA);
		
		syllabificationDisplay = new SyllabificationDisplay();
		syllabificationDisplay.setTranscript(ipaT);
		syllabificationDisplay.setFont(FontPreferences.getUIIpaFont());
		
		includeAlignmentBox = new JCheckBox("Include alignment");
		includeAlignmentBox.setSelected(settings.isIncludeAlignment());
		
		alignmentDisplay = new PhoneMapDisplay();
		alignmentDisplay.setPhoneMapForGroup(0, alignment);
		alignmentDisplay.setBackground(Color.WHITE);
		alignmentDisplay.setFont(FontPreferences.getUIIpaFont());
		
		JPanel tierOptionsPanel = new JPanel(new GridBagLayout());
		tierOptionsPanel.setBorder(BorderFactory.createTitledBorder("Tier Options"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 4;
		gbc.gridwidth = 1;
		
		tierOptionsPanel.add(tierTablePanel, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.insets = new Insets(0, 5, 0, 5);
		tierOptionsPanel.add(includeSyllabificationBox, gbc);
		
		++gbc.gridy;
		gbc.ipadx = 5;
		gbc.insets = new Insets(0, 25, 0, 5);
		tierOptionsPanel.add(syllabificationDisplay, gbc);
		
		++gbc.gridy;
		gbc.insets = new Insets(5, 5, 0, 5);
		tierOptionsPanel.add(includeAlignmentBox, gbc);
		
		++gbc.gridy;
		gbc.insets = new Insets(0, 25, 0, 5);
		tierOptionsPanel.add(alignmentDisplay, gbc);
		
		var resultSetEnabledListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				var enabled = includeQueryResults.isSelected() | filterUsingQueryResults.isSelected();
				resultSetSelector.getResultSetTable().setEnabled(enabled);
				resultSetValuesTable.setEnabled(enabled);
			}
			
		};
		includeQueryResults = new JCheckBox("Include query results");
		includeQueryResults.addActionListener(resultSetEnabledListener);
		includeQueryResults.setSelected(false);
		
		filterUsingQueryResults = new JCheckBox("Filter records using query results");
		filterUsingQueryResults.addActionListener(resultSetEnabledListener);
		filterUsingQueryResults.setSelected(false);
		
		JPanel queryOptionsPanel = new JPanel(new HorizontalLayout(5));
		queryOptionsPanel.add(includeQueryResults);
		queryOptionsPanel.add(filterUsingQueryResults);
		
		resultSetSelector = new OpenResultSetSelector(session);
		resultSetSelector.getResultSetTable().setEnabled(false);
		resultSetSelector.getResultSetTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultSetSelector.getResultSetTable().getSelectionModel().addListSelectionListener( (e) -> {
			var selectedResultSets = resultSetSelector.getSelectedResultSets();
			if(selectedResultSets.size() > 0) {
				var rs = selectedResultSets.get(0);
				if(rs.numberOfResults(true) > 0) {
					var result = rs.getResult(0);
					var rvList = new ArrayList<String>();
					
					for(var rv:ReportHelper.getExtraResultValues(result)) {
						rvList.add(rv.getName());
					}
					
					rvList.addAll(result.getMetadata().keySet());
					
					var tableModel = new ResultSetValuesTableModel(rvList);
					resultSetValuesTable.setModel(tableModel);
				}
			}
		});
		
		var resultSetTableModel = new ResultSetValuesTableModel(new ArrayList<>());
		resultSetValuesTable = new JXTable(resultSetTableModel);
		resultSetValuesTable.setEnabled(false);
		resultSetValuesTable.setVisibleRowCount(5);
		var resultSetTableScroller = new JScrollPane(resultSetValuesTable);
		
		JPanel resultSetPanel = new JPanel(new VerticalLayout());
		resultSetPanel.setBorder(BorderFactory.createTitledBorder("Query Results"));
		resultSetPanel.add(queryOptionsPanel);
		resultSetPanel.add(resultSetSelector);
		resultSetPanel.add(resultSetTableScroller);
		
		setLayout(new VerticalLayout(5));
		add(recordFilterPanel);
		add(participantOptionsPanel);
		add(tierOptionsPanel);
		add(resultSetPanel);
	}
	
	public void moveTierUp() {
		final int selectedRow = tierViewTable.getSelectedRow();
		if(selectedRow > 0 && selectedRow < tableModel.tierView.size()) {
			final TierViewItem tvi = tableModel.tierView.remove(selectedRow);
			tableModel.fireTableRowsDeleted(selectedRow, selectedRow);
			int newRow = selectedRow - 1;
			tableModel.tierView.add(newRow, tvi);
			tableModel.fireTableRowsInserted(newRow, newRow);
			
			tierViewTable.getSelectionModel().setSelectionInterval(newRow, newRow);
		}
	}
	
	public void moveTierDown() {
		final int selectedRow = tierViewTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < tableModel.tierView.size()-1) {
			final TierViewItem tvi = tableModel.tierView.remove(selectedRow);
			tableModel.fireTableRowsDeleted(selectedRow, selectedRow);
			int newRow = selectedRow + 1;
			tableModel.tierView.add(newRow, tvi);
			tableModel.fireTableRowsInserted(newRow, newRow);
			
			tierViewTable.getSelectionModel().setSelectionInterval(newRow, newRow);
		}
	}
	
	/**
	 * Update settings based on current component values and return.
	 */
	public SessionExportSettings getSettings() {
		settings.setRecordFilter(recordFilterPanel.getRecordFilter());
		
		settings.setIncludeParticipantInfo(includeParticipantInfoBox.isSelected());
		settings.setIncludeRole(includeRole.isSelected());
		settings.setIncludeAge(includeAge.isSelected());
		settings.setIncludeBirthday(includeBirthday.isSelected());
		settings.setIncludeSex(includeSex.isSelected());
		settings.setIncludeLanguage(includeLanguage.isSelected());
		settings.setIncludeGroup(includeGroup.isSelected());
		settings.setIncludeEducation(includeEducation.isSelected());
		settings.setIncludeSES(includeSES.isSelected());
		
		settings.setTierView(((TierViewTableModel)tierViewTable.getModel()).tierView);
		settings.setIncludeSyllabification(includeSyllabificationBox.isSelected());
		settings.setIncludeAlignment(includeAlignmentBox.isSelected());

		List<ResultSet> selectedResults = resultSetSelector.getSelectedResultSets();
		
		if(selectedResults.size() > 0) {
			settings.setIncludeQueryResults(includeQueryResults.isSelected());
			settings.setFilterRecordsUsingQueryResults(filterUsingQueryResults.isSelected());
			
			ResultSet rs = selectedResults.get(0);
			settings.setResultSet(rs);
			
			ResultSetValuesTableModel tableModel = (ResultSetValuesTableModel)resultSetValuesTable.getModel();
			var rvList = 
					tableModel.resultValueList.stream()
						.filter( (rvName) -> !tableModel.resultValueVisible.get(rvName) )
						.collect( Collectors.toList() );
			settings.setResultValues(rvList);
			settings.setExcludeResultValues(true);
		}
		
		return settings;
	}
	
	private class ResultSetValuesTableModel extends AbstractTableModel {
		
		private List<String> resultValueList;
		
		private Map<String, Boolean> resultValueVisible;
		
		public ResultSetValuesTableModel(List<String> rvList) {
			super();
			
			resultValueList = rvList;
			resultValueVisible = new HashMap<>();
			// exclude the 'Alignment' metadata field by default
			for(String rvName:rvList) resultValueVisible.put(rvName, !rvName.equals("Alignment"));
		}

		@Override
		public int getRowCount() {
			return resultValueList.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0:
				return "Visible";
			
			case 1:
				return "Tier";
				
			default:
				return "";
			}
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return Boolean.class;
				
			case 1:
				return String.class;
				
			default:
				return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			var rvName = resultValueList.get(rowIndex);
			resultValueVisible.put(rvName, (Boolean)aValue);
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String rvName = resultValueList.get(rowIndex);
			switch(columnIndex) {
			case 0:
				return resultValueVisible.get(rvName);
				
			case 1:
				return rvName;
				
			default:
				return "";
			}
		}
		
	}
	
	private class TierViewTableModel extends AbstractTableModel {
		
		private List<TierViewItem> tierView;
		
		public TierViewTableModel() {
			this(new ArrayList<TierViewItem>());
		}
		
		public TierViewTableModel(List<TierViewItem> tierView) {
			this.tierView = tierView;
		}
		
		public void setTierView(List<TierViewItem> tierView) {
			this.tierView = tierView;
			super.fireTableDataChanged();
		}

		public List<TierViewItem> getTierView() {
			return this.tierView;
		}
		
		@Override
		public int getRowCount() {
			return tierView.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0:
				return "Visible";
				
			case 1:
				return "Tier Name";
				
			case 2:
				return "Font";
				
			default:
				return super.getColumnName(column);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return Boolean.class;
				
			default:
				return super.getColumnClass(columnIndex);
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			final TierViewItem tvi = tierView.remove(rowIndex);
			final TierViewItem newItem = SessionFactory.newFactory().createTierViewItem(tvi.getTierName(), Boolean.valueOf(aValue.toString()), tvi.getTierFont());
			tierView.add(rowIndex, newItem);
			super.fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			final TierViewItem tierViewItem = tierView.get(rowIndex);
			
			switch(columnIndex) {
			case 0:
				return tierViewItem.isVisible();
				
			case 1:
				return tierViewItem.getTierName();
				
			case 2:
				return tierViewItem.getTierFont();
				
			default:
				return "";
			}
		}
		
	}

}
