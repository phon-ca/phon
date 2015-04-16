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
package ca.phon.script;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.script.scripttable.AbstractScriptTableModel;
import ca.phon.script.scripttable.ListScriptTableModel;
import ca.phon.script.scripttable.ScriptTableModel;
import ca.phon.util.Tuple;

/**
 * Run test on {@link AbstractScriptTableModel}
 *
 */
@RunWith(JUnit4.class)
public class TestScriptTableModel {

	// data for testing
	private final static String COL0_SCRIPT = 
			"function getName() { return colNames[col]; }\nfunction getValue() {return rowData.getObj1();\n}";
	
	private final static String COL1_SCRIPT = 
			"function getName() { return colNames[col]; }\nfunction getValue() {return rowData.getObj2();\n}";
	
	private final static String[] COL_NAMES = new String[] { "Column 0", "Column 1" };
	
	@Test
	public void testModel() throws PhonScriptException {
		final List<Tuple<Integer, Boolean>> rowData = setupData();
		final ListScriptTableModel<Tuple<Integer, Boolean>> model = new ListScriptTableModel<Tuple<Integer, Boolean>>(rowData);
		final Map<String, Object> staticMappings = new HashMap<String, Object>();
		staticMappings.put("colNames", COL_NAMES);
		
		model.setColumnScript(0, new BasicScript(COL0_SCRIPT));
		model.setColumnMappings(0, staticMappings);
		
		model.setColumnScript(1, new BasicScript(COL1_SCRIPT));
		model.setColumnMappings(1, staticMappings);
		
		testColumnNames(model);
		testCellValues(model);
	}
	
	private List<Tuple<Integer, Boolean>> setupData() {
		final List<Tuple<Integer, Boolean>> retVal = new ArrayList<Tuple<Integer, Boolean>>();
		
		retVal.add(new Tuple<Integer, Boolean>(1, false));
		retVal.add(new Tuple<Integer, Boolean>(2, true));
		retVal.add(new Tuple<Integer, Boolean>(3, true));
		
		return retVal;
	}
	
	// ensure column names are correct
	public void testColumnNames(ScriptTableModel model) {
		Assert.assertEquals(COL_NAMES.length, model.getColumnCount());
		
		for(int i = 0; i < model.getColumnCount(); i++) {
			Assert.assertEquals(COL_NAMES[i], model.getColumnName(i));
		}
	}
	
	// ensure cell values are correct
	public void testCellValues(ScriptTableModel model) {
		final List<Tuple<Integer, Boolean>> rowData = setupData();
		Assert.assertEquals(rowData.size(), model.getRowCount());
		
		for(int i = 0; i < model.getColumnCount(); i++) {
			for(int j = 0; j < model.getRowCount(); j++) {
				final Object val = model.getValueAt(j, i);
				if(i == 0) {
					Assert.assertEquals(rowData.get(j).getObj1(), val);
				} else if(i == 1) {
					Assert.assertEquals(rowData.get(j).getObj2(), val);
				}
			}
		}
	}
}
