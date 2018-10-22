package ca.phon.app.opgraph.nodes.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;

@OpNodeInfo(name="Column Percentage", category="Table", description="Calculate percentage using a divisor and dividend columns.", showInLibrary=true)
public class ColumnPercentageNode extends TableOpNode implements NodeSettings {

	private JPanel settingsPanel;
	private JTextField divisorColumnField;
	private JTextArea dividendColumnsArea;
	
	private JRadioButton useLastRowButton;
	private JRadioButton sumColumnsButton;

	// if sumColumns is false, only last row is used for calculations
	private boolean sumColumns;
	
	private String divsorColumn;
	
	private List<String> dividendColumns = new ArrayList<>();
	
	public ColumnPercentageNode() {
		super();
		
		putExtension(NodeSettings.class, this);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource inputTable = (DefaultTableDataSource)context.get(tableInput);
		context.put(tableOutput, inputTable);
		
		List<String> allColumns = new ArrayList<>();
		allColumns.add(getDivisorColumn());
		allColumns.addAll(getDividendColumns());
		final Map<String, Double> columnSums = new HashMap<>();
		allColumns.forEach( (colName) -> columnSums.put(colName, 0.0) );
		if(isSumColumns()) {
			ColumnSumNode sumNode = new ColumnSumNode();
			columnSums.putAll(sumNode.sumColumns(inputTable, allColumns));
		} else {
			if(inputTable.getRowCount() > 0) {
				var rowData = inputTable.getRow(inputTable.getRowCount()-1);
				
				for(String colName:allColumns) {
					int colIdx = inputTable.getColumnIndex(colName);
					if(colIdx >= 0) {
						var rowVal = rowData[colIdx];
						if(rowVal instanceof Number)
							columnSums.put(colName, ((Number)rowVal).doubleValue());
					}
				}
			}
		}
		
		var rowData = new Object[inputTable.getColumnCount()];
		for(var col = 0; col< inputTable.getColumnCount(); col++) rowData[col] = "";
		double divisor = columnSums.get(getDivisorColumn());
		if(divisor == 0.0) return;
		
		for(String dividendCol:getDividendColumns()) {
			int colIdx = inputTable.getColumnIndex(dividendCol);
			if(colIdx >= 0) {
				double p = columnSums.get(dividendCol) / divisor;
				rowData[colIdx] = Double.valueOf( p * 100.0 );
			}
		}
		inputTable.addRow(rowData);
		
	}
	
	public String getDivisorColumn() {
		return (this.divisorColumnField != null ? this.divisorColumnField.getText() : this.divsorColumn);
	}
	
	public void setDivisorColumn(String divisorColumn) {
		this.divsorColumn = divisorColumn;
		if(this.divisorColumnField != null)
			this.divisorColumnField.setText(divisorColumn);
	}
	
	public boolean isSumColumns() {
		return (this.sumColumnsButton != null ? this.sumColumnsButton.isSelected() : this.sumColumns);
	}
	
	public void setSumColumns(boolean sumColumns) {
		this.sumColumns = sumColumns;
		if(this.sumColumnsButton != null) {
			this.sumColumnsButton.setSelected(sumColumns);
			this.useLastRowButton.setSelected(!sumColumns);
		}
	}
	
	public List<String> getDividendColumns() {
		return (this.dividendColumnsArea != null ? parseColumns(dividendColumnsArea.getText()) : this.dividendColumns);
	}

	public void setDividendColumns(String columnTxt) {
		this.dividendColumns = parseColumns(columnTxt);
		if(this.dividendColumnsArea != null) {
			this.dividendColumnsArea.setText(columnTxt);
		}
	}

	public void setDividendColumns(List<String> columns) {
		this.dividendColumns = columns;
		if(this.dividendColumnsArea != null) {
			this.dividendColumnsArea.setText(
					this.dividendColumns.stream().collect(Collectors.joining("\n")) );
		}
	}

	private List<String> parseColumns(String txt) {
		List<String> columns = new ArrayList<>();
		try ( BufferedReader reader = new BufferedReader(new StringReader(txt)) ) {
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0) continue;
				columns.add(line);
			}
		} catch (IOException e) {
			LogUtil.warning(e);
		}
		return columns;
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new BorderLayout());
			
			divisorColumnField = new JTextField();
			divisorColumnField.setText(divsorColumn);
			
			ButtonGroup grp = new ButtonGroup();
			useLastRowButton = new JRadioButton("Use last row for calculation");
			sumColumnsButton = new JRadioButton("Sum columns before calculation");
			grp.add(sumColumnsButton);
			grp.add(useLastRowButton);
			
			useLastRowButton.setSelected(!sumColumns);
			sumColumnsButton.setSelected(sumColumns);
			
			JPanel topPanel = new JPanel(new VerticalLayout());
			topPanel.add(sumColumnsButton);
			topPanel.add(useLastRowButton);
			topPanel.add(new JLabel("Divisor Column"));
			topPanel.add(divisorColumnField);
			
			String columnsTxt = getDividendColumns().stream().collect(Collectors.joining("\n"));
			dividendColumnsArea = new JTextArea();
			dividendColumnsArea.setText(columnsTxt);
			final JScrollPane scroller = new JScrollPane(dividendColumnsArea);
			
			settingsPanel.add(topPanel, BorderLayout.NORTH);
			settingsPanel.add(scroller, BorderLayout.CENTER);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		retVal.setProperty("sumColumns", Boolean.toString(isSumColumns()));
		retVal.setProperty("divisorColumn", getDivisorColumn());
		retVal.setProperty("dividendColumns", getDividendColumns().stream().collect(Collectors.joining("\n")) );
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setSumColumns(Boolean.parseBoolean(properties.getProperty("sumColumns", "true")));
		setDivisorColumn(properties.getProperty("divisorColumn", ""));
		setDividendColumns(properties.getProperty("dividendColumns", ""));
	}
	
}
