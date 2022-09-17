/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.script.params.history;

import ca.phon.ui.PhonGuiConstants;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 *  
 */
public class ParamHistoryList extends JList<ParamSetType> {

	private final ParamHistoryManager paramHistoryManager;
		
	public ParamHistoryList(ParamHistoryManager history) {
		this.paramHistoryManager = history;
		
		init();
	}
	
	private void init() {
		super.setCellRenderer(new ParamSetListCellRenderer());
		setModel(new ParamHistoryListModel());
	}
	
	public static class ParamSetListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			// generate HTML label text
			if(value instanceof ParamSetType) {
				StringBuffer buffer = new StringBuffer();
				buffer.append("<html><p style='padding-top:5px; padding-bottom:5px;'>");
				ParamSetType paramSet = (ParamSetType)value;
				String paramSetName = getParamSetName(paramSet);
				if(paramSetName != null && paramSetName.trim().length() > 0) {
					buffer.append("<b>").append(paramSetName).append("</b><br/>");
				}
				for(ParamType param:paramSet.getParam()) {
					buffer.append(getParamName(param)).append(" ");
					
					String paramValue = getAbbreviatedValue(param);
					if(paramValue.contains("\n")) {
						buffer.append("<pre>");
						buffer.append(paramValue);
						buffer.append("</pre>");
					} else {
						buffer.append("<code>").append(paramValue).append("</code>&nbsp;<br/>");
					}
					
				}
				buffer.append("</p></html>");
				
				retVal.setText(buffer.toString());
			}
			
			if(index % 2 == 1 && !isSelected) {
				retVal.setBackground(PhonGuiConstants.PHON_UI_STRIP_COLOR);
			}
			
			return retVal;
		}
		
		protected String getParamName(ParamType param) {
			return param.getId();
		}
		
		protected String getParamValue(ParamType param) {
			return param.getValue();
		}
		
		protected String getAbbreviatedValue(ParamType param) {
			return StringUtils.abbreviate(getParamValue(param), 100);
		}
		
		protected String getParamSetName(ParamSetType paramSet) {
			return paramSet.getName();
		}
		
	}
	
	private class ParamHistoryListModel extends AbstractListModel<ParamSetType> {
		
		private String filter;
		
		public ParamHistoryListModel() {
			super();
		}
		
		public String getFilter() {
			return this.filter;
		}
		
		public void setFilter(String filter) {
			this.filter = filter;
		}
		
		public List<ParamSetType> getParamSets() {
			return paramHistoryManager.getParamHistory().getParamSet();
		}
		
		@Override
		public int getSize() {
			return getParamSets().size();
		}

		@Override
		public ParamSetType getElementAt(int index) {
			return getParamSets().get(getParamSets().size()-index-1);
		}
		
	}
	
}
