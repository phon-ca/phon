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
package ca.phon.script.params;

public class SeparatorScriptParam extends ScriptParam {
	
	public static final String COLLAPSED_PROP = SeparatorScriptParam.class.getName() + ".collapsed";

	private boolean collapsed = false;
	
	public SeparatorScriptParam(String id, String title, boolean collapsed) {
		super(new String[] {id + ".collapsed"}, new Object[] {collapsed});
		setParamDesc(title);
		setParamType("separator");
		setCollapsed(collapsed);
	}
	
	@Deprecated
	public SeparatorScriptParam(String desc) {
		this(desc, false);
	}
	
	@Deprecated
	public SeparatorScriptParam(String desc, boolean collapsed) {
		super(new String[0], new Object[0]);
		setParamType("separator");
		setParamDesc(desc);
		setCollapsed(collapsed);
	}
	
	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		final boolean wasCollapsed = this.collapsed;
		this.collapsed = collapsed;
		if(getParamId() != null)
			super.setValue(getParamId(), collapsed);
		super.propSupport.firePropertyChange(COLLAPSED_PROP, wasCollapsed, collapsed);
	}

	@Override
	public void setValue(String paramId, Object val) {
		if(paramId.equals(getParamId()) && val != null) {
			setCollapsed(Boolean.parseBoolean(val.toString()));
		} else {
			super.setValue(paramId, val);
		}
	}
	
	@Override
	public String getStringRepresentation() {
		String retVal = "{";

		retVal += "separator, ";
		retVal += "\"" + super.getParamDesc() + "\", ";
		retVal += collapsed;

		retVal += "}";

		return retVal;
	}
}
