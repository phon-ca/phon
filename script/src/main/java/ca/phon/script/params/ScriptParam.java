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
package ca.phon.script.params;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import ca.phon.script.PhonScriptContext;

/**
 * A parameter for a script.  These parameters are setup by the {@link PhonScriptContext}
 * when running the script.
 * 
 * Each {@link ScriptParam} can have more than one paramId which
 * maps to the same property name in the script.
 *
 */
public abstract class ScriptParam {
	
	public final static String ENABLED_PROP = ScriptParam.class.getName() + ".enabled";
	
	public final static String VISIBLE_PROP = ScriptParam.class.getName() + ".visible";
	
	/** The param type */
	private String paramType;

	/** The description */
	private String paramDesc;
	
	private transient boolean visible = true;
	
	private transient boolean enabled = true;
	
	/** The paramid's avd values */
	private HashMap<String, Object> values =
		 new LinkedHashMap<String, Object>();
	
	/** The default values */
	private HashMap<String, Object> defValues = 
		new LinkedHashMap<String, Object>();
	
	/**
	 * Property change support
	 */
	protected final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(propertyName, listener);
	}

	/**
	 * Constructor
	 */
	public ScriptParam() {
		this(new String[0], new Object[0]);
	}
	
	public ScriptParam(String[] ids, Object[] defaultValues) {
		super();
		
		for(int i = 0; i < ids.length; i++) {
			setValue(ids[i], null);
			setDefaultValue(ids[i], defaultValues[i]);
		}
	}
	
	/**
	 * Returns the first (and many times only) param id
	 * for this ScriptParam
	 * 
	 * @return first param id
	 */
	public String getParamId() {
		if(getParamIds().size() == 0) return null;
		return getParamIds().iterator().next();
	}
	
	public Collection<String> getParamIds() {
		return values.keySet();
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public String getParamDesc() {
		return paramDesc;
	}

	public void setParamDesc(String paramDesc) {
		this.paramDesc = paramDesc;
	}
	
	public Object getValue(String paramId) {
		Object retVal = 
			(values.get(paramId) == null ? getDefaultValue(paramId) : values.get(paramId));
		return retVal;
	}
	
	public void setValue(String paramId, Object val) {
		Object oldval = getValue(paramId);
		values.put(paramId, val);
		propSupport.firePropertyChange(paramId, oldval, val);
	}

	public Object getDefaultValue(String paramId) {
		return defValues.get(paramId);
	}

	public void setDefaultValue(String paramId, Object defaultValue) {
		defValues.put(paramId, defaultValue);
	}

	/**
	 * Has the value of any of the paramIds changed
	 * from the default value.
	 * 
	 * @return
	 */
	public boolean hasChanged() {
		boolean retVal = false;

		for(String id:getParamIds()) {
			Object def = getDefaultValue(id);
			Object val = getValue(id);
			retVal |= !def.equals(val);

		}

		return retVal;
	}

	/**
	 * Get the string representation of this
	 * param.
	 */
	public abstract String getStringRepresentation();

	@Override
	public String toString() {
		return getStringRepresentation();
	}
	
	/**
	 * Is this param enabled.  If a script param is not enabled
	 * it will not be added to the scriptable scope for the script
	 * during execution.  As well, if a UI is visible, the component
	 * controlling this param should also appear 'disabled'
	 * 
	 * @return <code>true</code> if this param is enabled, <code>false</code>
	 *  otherwise
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
	
	/**
	 * Set the enabled state of this ScriptParam
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		final boolean oldVal = this.enabled;
		this.enabled = enabled;
		propSupport.firePropertyChange(ENABLED_PROP, oldVal, enabled);
	}
	
	/**
	 * Get the visiblity status of this param.  This property
	 * affects any UI component that is controlling this param.
	 * 
	 * An invisible param may still be enabled and setup during
	 * script execution.
	 * 
	 * @return <code>true</code> if this param should be invisible,
	 *  <code>false</code> otherwise
	 */
	public boolean getVisible() {
		return this.visible;
	}
	
	/**
	 * Set visibility status of param
	 * 
	 * @param visible
	 */
	public void setVisible(Boolean visible) {
		final Boolean oldVal = this.visible;
		this.visible = visible;
		propSupport.firePropertyChange(VISIBLE_PROP, oldVal, visible);
	}
	
}
