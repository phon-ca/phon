/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.script;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.script.scripttable.AbstractScriptTableModel;
import ca.phon.script.scripttable.ListScriptTableModel;
import ca.phon.script.scripttable.ScriptTableModel;
import ca.phon.util.Tuple;
import junit.framework.Assert;

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
