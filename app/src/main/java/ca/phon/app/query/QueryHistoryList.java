package ca.phon.app.query;

import java.awt.Dimension;

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
	
	private ParamSetType prototypeCellValue;
	
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
		retVal.setSize(retVal.width, Math.min(retVal.height, 500));
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
