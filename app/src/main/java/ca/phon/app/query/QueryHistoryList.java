package ca.phon.app.query;

import java.awt.Dimension;

import javax.swing.JComponent;

import org.apache.commons.lang3.StringUtils;

import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.script.params.history.ObjectFactory;
import ca.phon.script.params.history.ParamHistoryList;
import ca.phon.script.params.history.ParamHistoryManager;
import ca.phon.script.params.history.ParamSetType;
import ca.phon.script.params.history.ParamType;

public class QueryHistoryList extends ParamHistoryList {

	private ScriptParameters scriptParameters;
	
	private ParamHistoryManager stockQueries;
		
	public QueryHistoryList(ScriptParameters scriptParameters, ParamHistoryManager stockQueries, ParamHistoryManager history) {
		super(history);
		this.scriptParameters = scriptParameters;
		this.stockQueries = stockQueries;
		
		init();
	}
	
	private void init() {		
		setCellRenderer(new QueryHistoryListCellRenderer());
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		Dimension retVal = super.getPreferredScrollableViewportSize();
		
		ParamSetType prototype = getPrototypeParamSet();
		JComponent prototypeCell = 
				(JComponent)getCellRenderer().getListCellRendererComponent(this, prototype, -1, true, true);
		int h = getVisibleRowCount() * prototypeCell.getPreferredSize().height;
		int w = prototypeCell.getPreferredSize().width;
		
		retVal.setSize(w, h);
		
		return retVal;
	}

	private ParamSetType getPrototypeParamSet() {
		ObjectFactory factory = new ObjectFactory();
		ParamSetType retVal = factory.createParamSetType();
		
		retVal.setName("My Query");
		ParamType p = factory.createParamType();
		p.setId("my.param1.id");
		p.setValue(StringUtils.repeat('w', 80));
		retVal.getParam().add(p);
		
		p.setId("my.param2.id");
		p.setValue(StringUtils.repeat('w', 80));
		retVal.getParam().add(p);
		
		return retVal;
	}
	
	private class QueryHistoryListCellRenderer extends ParamHistoryList.ParamSetListCellRenderer {

		@Override
		protected String getParamName(ParamType param) {
			for(ScriptParam scriptParam:scriptParameters) {
				if(scriptParam.getParamIds().contains(param.getId())) {
					return scriptParam.getParamDesc();
				}
			}
			return super.getParamName(param);
		}

		@Override
		protected String getParamSetName(ParamSetType paramSet) {
			String retVal = super.getParamSetName(paramSet);
			if(retVal == null) {
				ParamSetType stockQuery = stockQueries.getParamSet(paramSet.getHash());
				if(stockQuery != null)
					retVal = stockQuery.getName();
			}
			return retVal;
		}
		
	}
	
}
