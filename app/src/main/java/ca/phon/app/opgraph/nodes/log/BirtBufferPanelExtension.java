/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.nodes.log;

import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.ColumnHint;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.elements.structures.OdaResultSetColumn;
import org.eclipse.birt.report.model.elements.DataSet;

import ca.phon.app.log.BufferPanel;
import ca.phon.extensions.Extension;
import ca.phon.query.report.datasource.DefaultTableDataSource;

/**
 * Extension for {@link BufferPanel}s adding support
 * for exporting the buffers data in a Birt report.
 */
@Extension(BufferPanel.class)
public class BirtBufferPanelExtension {

	private BufferPanel panel;
	
	private DefaultTableDataSource data;
	
	public BirtBufferPanelExtension(BufferPanel panel, DefaultTableDataSource data) {
		super();
		this.panel = panel;
		this.data = data;
	}

	public BufferPanel getPanel() {
		return panel;
	}

	public void setPanel(BufferPanel panel) {
		this.panel = panel;
	}

	public DefaultTableDataSource getData() {
		return data;
	}

	public void setData(DefaultTableDataSource data) {
		this.data = data;
	}
	
	public TableHandle createTable(String dataSetName, ReportDesignHandle designHandle, ElementFactory elementFactory) throws SemanticException {
		TableHandle table = elementFactory.newTableItem( "table", getData().getColumnCount() );
        table.setWidth( "100%" );
        table.setDataSet( designHandle.findDataSet( dataSetName ) );
        table.setProperty("borderBottomStyle", "solid");
        table.setProperty("borderBottomWidth", "medium");
        table.setProperty("borderLeftStyle", "solid");
        table.setProperty("borderLeftWidth", "medium");
        table.setProperty("borderTopStyle", "solid");
        table.setProperty("borderTopWidth", "medium");
        table.setProperty("borderRightStyle", "solid");
        table.setProperty("borderRightWidth", "medium");
        table.setProperty("marginBottom", "10px");
        
        // setup column expressions
        PropertyHandle computedSet = table.getColumnBindings();
        for(int i=0; i < getData().getColumnCount(); i++) {
        	ComputedColumn col = StructureFactory.createComputedColumn();
        	col.setName(getData().getColumnTitle(i));
        	col.setExpression("dataSetRow[\"" + getData().getColumnTitle(i) + "\"]");
        	computedSet.addItem(col);
        }
        
        // header row
        RowHandle tableheader = (RowHandle) table.getHeader( ).get( 0 );
        tableheader.setProperty("borderBottomStyle", "solid");
        tableheader.setProperty("borderBottomWidth", "thin");
        tableheader.setProperty("backgroundColor", "#F0F8FF");
		for (int i = 0; i < getData().getColumnCount(); i++) {
			LabelHandle label = elementFactory.newLabel(getData().getColumnTitle(i));
			label.setText(getData().getColumnTitle(i));
			CellHandle cell = (CellHandle) tableheader.getCells().get(i);
			cell.getContent().add(label);
		}
		
		 // table detail
		RowHandle tabledetail = (RowHandle) table.getDetail().get(0);
		for (int i = 0; i < getData().getColumnCount(); i++) {
			CellHandle cell = (CellHandle) tabledetail.getCells().get(i);
			DataItemHandle data = elementFactory.newDataItem("data_" + getData().getColumnTitle(i));
			data.setResultSetColumn(getData().getColumnTitle(i));
			cell.getContent().add(data);
			if(i == 0) {
				cell.setProperty("borderRightStyle", "solid");
				cell.setProperty("borderRightWidth", "thin");
			}
		}
		
		return table;
	}
	
	public String getDataType(Class<?> clazz) {
		String retVal = "string";
		
		if(Number.class.isAssignableFrom(clazz)) {
			if(clazz == Integer.class || clazz == Long.class) {
				retVal = "integer";
			} else {
				retVal = "float";
			}
		} else if(clazz == Boolean.class) {
			retVal = "boolean";
		}
		
		return retVal;
	}
	
	public DataSetHandle createDataSet(String dataSourceName, String dataSetName, 
			ElementFactory elementFactory) throws SemanticException {
		OdaDataSetHandle dataSet = elementFactory.newOdaDataSet(dataSetName, 
				"org.eclipse.datatools.connectivity.oda.flatfile.dataSet");
		dataSet.setDataSource(dataSourceName);
		
		final StringBuffer buffer = new StringBuffer();
		buffer.append("select ");
		for(int col = 0; col < getData().getColumnCount(); col++) {
			buffer.append('"').append(getData().getColumnTitle(col)).append('"');
			if(col != getData().getColumnCount()-1) {
				buffer.append(',');
			}
		}
		buffer.append(" from \"").append(dataSetName).append(".csv\"");
		dataSet.setQueryText(buffer.toString());
		
		for(int col = 0; col < getData().getColumnCount(); col++) {
			final String colName = getData().getColumnTitle(col);
			final String dataType = getDataType(getData().inferColumnType(col));
			
			OdaResultSetColumn result = new OdaResultSetColumn();
			result.setPosition(col+1);
			result.setColumnName(colName);
			result.setNativeName(colName);
			result.setDataType(dataType);
			dataSet.getPropertyHandle(DataSetHandle.RESULT_SET_PROP).addItem(result);
			
			ColumnHint resultHint = new ColumnHint();
			resultHint.setProperty(ColumnHint.COLUMN_NAME_MEMBER, colName);
			resultHint.setProperty(ColumnHint.ANALYSIS_MEMBER, "dimension");
			resultHint.setProperty(ColumnHint.HEADING_MEMBER, colName);
			dataSet.getPropertyHandle(DataSet.COLUMN_HINTS_PROP).addItem(resultHint);
		}
		
		return dataSet;
	}
	
	public void addTableToReport(String dataSourceName, ReportDesignHandle designHandle)
		throws SemanticException {
		// add data set
		final ElementFactory elementFactory = designHandle.getElementFactory();
		final String dataSetName = panel.getBufferName();
		final DataSetHandle dataSetHandle = createDataSet(dataSourceName,
				dataSetName, elementFactory);
		designHandle.getDataSets().add(dataSetHandle);
		
		LabelHandle titleLabel = elementFactory.newLabel("lbl_"+dataSetName);
		titleLabel.setText(dataSetName);
		titleLabel.setProperty("fontSize", "12pt");
		titleLabel.setProperty("fontWeight", "bold");
		designHandle.getBody().add(titleLabel);
		
		if(data.getRowCount() == 0) {
			LabelHandle noResultsLabel = elementFactory.newLabel("lbl_" + dataSetName + "_noResults");
			noResultsLabel.setText("No results");
			designHandle.getBody().add(noResultsLabel);
		} else {
			final TableHandle tableHandle = createTable(dataSetName, designHandle, designHandle.getElementFactory());
			designHandle.getBody().add(tableHandle);
		}
	}
	
}

