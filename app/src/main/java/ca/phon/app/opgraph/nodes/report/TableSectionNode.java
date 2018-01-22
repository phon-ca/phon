package ca.phon.app.opgraph.nodes.report;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.swingx.VerticalLayout;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.report.tree.*;

@OpNodeInfo(name="Table Section", category="Report Tree", description="Add/Create a new table section for the report", showInLibrary=true)
public class TableSectionNode extends ReportSectionNode implements NodeSettings {

	private final InputField tableNameField =
			new InputField("tableName", "Name of table (buffer)", true, true, String.class);

	/* UI */
	private JPanel settingsPanel;
	private JRadioButton includeColumnsButton;
	private JRadioButton excludeColumnsButton;
	private JTextArea columnsArea;

	private final static String INCLUDE_COLUMNS_PROP = TableSectionNode.class.getName() + ".includeColumns";
	private boolean includeColumns = true;

	private final static String COLUMNS_PROP = TableSectionNode.class.getName() + ".columns";
	private List<String> columns = new ArrayList<>();

	public TableSectionNode() {
		super();

		putField(tableNameField);

		putExtension(NodeSettings.class, this);
	}

	@Override
	protected ReportTreeNode createReportSectionNode(OpContext context) {
		final String title =
				(context.get(sectionNameInput) != null ? context.get(sectionNameInput).toString() : "");
		final String tableName =
				(context.get(tableNameField) != null ? context.get(tableNameField).toString() : "");

		return new TableNode(title, tableName, isIncludeColumns(), getColumns());
	}

	public boolean isIncludeColumns() {
		return (this.includeColumnsButton != null ? this.includeColumnsButton.isSelected() : this.includeColumns);
	}

	public void setIncludeColumns(boolean includeColumns) {
		this.includeColumns = includeColumns;
		if(this.includeColumnsButton != null) {
			this.includeColumnsButton.setSelected(includeColumns);
			this.excludeColumnsButton.setSelected(!includeColumns);
		}
	}

	public List<String> getColumns() {
		return this.columns;
	}

	public void setColumns(String columnTxt) {
		updateColumns(columnTxt);
		if(this.columnsArea != null) {
			this.columnsArea.setText(columnTxt);
		}
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
		if(this.columnsArea != null) {
			this.columnsArea.setText(
					this.columns.stream().collect(Collectors.joining("\n")) );
		}
	}

	private void updateColumns(String txt) {
		columns.clear();

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
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new BorderLayout());

			final ButtonGroup bg = new ButtonGroup();
			includeColumnsButton = new JRadioButton("Include columns");
			excludeColumnsButton = new JRadioButton("Exclude columns");
			includeColumnsButton.setSelected(includeColumns);
			excludeColumnsButton.setSelected(!includeColumns);
			bg.add(includeColumnsButton);
			bg.add(excludeColumnsButton);

			columnsArea = new JTextArea();
			columnsArea.setText(getColumns().stream().collect(Collectors.joining("\n")));
			final JScrollPane scroller = new JScrollPane(columnsArea);
			columnsArea.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					updateColumns(columnsArea.getText());
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					updateColumns(columnsArea.getText());
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
				}

			});

			final JPanel topPanel = new JPanel(new VerticalLayout());
			topPanel.add(includeColumnsButton);
			topPanel.add(excludeColumnsButton);

			settingsPanel.add(topPanel, BorderLayout.NORTH);
			settingsPanel.add(scroller, BorderLayout.CENTER);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		retVal.setProperty(INCLUDE_COLUMNS_PROP, Boolean.toString(isIncludeColumns()));
		retVal.setProperty(COLUMNS_PROP, getColumns().stream().collect(Collectors.joining("\n")) );
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setIncludeColumns(Boolean.parseBoolean(properties.getProperty(INCLUDE_COLUMNS_PROP, "true")));
		setColumns(properties.getProperty(COLUMNS_PROP, ""));
	}

}
