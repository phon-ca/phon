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
package ca.phon.query.script.params;

import ca.phon.ipa.*;
import ca.phon.script.params.ScriptParam;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DiacriticOptionsScriptParam extends ScriptParam {

	public static final String IGNORE_DIACRITICS_PARAM = "ignoreDiacritics";
	public static final String SELECTED_DIACRITICS_PARAM = "selectedDiacritics";
	public static final String SELECTION_MODE_PARAM = "selectionMode";
	
	public static enum SelectionMode {
		ONLY("Only"),
		EXCEPT("Except");
		
		private String text;
		
		private SelectionMode(String text) {
			this.text = text;
		}
		
		@Override
		public String toString() {
			return text;
		}
		
	}
	
	private String ignoreDiacriticsParamId;
	
	private String selectedDiacriticsParamId;
	
	private String selectionModeParamId;

	private String verb = "Ignore";
	
	public DiacriticOptionsScriptParam(String id, String desc, boolean defaultIgnoreDiacritics, Collection<Diacritic> defaultSelectedDiacritics) {
		this(id, desc, defaultIgnoreDiacritics, SelectionMode.EXCEPT, defaultSelectedDiacritics);
	}
	
	public DiacriticOptionsScriptParam(String id, String desc, boolean defaultIgnoreDiacritics, SelectionMode selectionMode, Collection<Diacritic> defaultSelectedDiacritics) {
		super();
		
		setParamType("diacriticOptions");
		setParamDesc(desc);
		
		ignoreDiacriticsParamId = id + "." + IGNORE_DIACRITICS_PARAM;
		selectionModeParamId = id + "." + SELECTION_MODE_PARAM;
		selectedDiacriticsParamId = id + "." + SELECTED_DIACRITICS_PARAM;
		
		setValue(ignoreDiacriticsParamId, defaultIgnoreDiacritics);
		setDefaultValue(ignoreDiacriticsParamId, defaultIgnoreDiacritics);
		
		setValue(selectionModeParamId, selectionMode.toString());
		setDefaultValue(selectionModeParamId, selectionMode.toString());
		
		setValue(selectedDiacriticsParamId, defaultSelectedDiacritics);
		setDefaultValue(selectedDiacriticsParamId, defaultSelectedDiacritics);
	}

	public String getVerb() {
		return this.verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public String getIgnoreDiacriticsParamId() {
		return this.ignoreDiacriticsParamId;
	}
	
	public boolean isIgnoreDiacritics() {
		return Boolean.valueOf(getValue(getIgnoreDiacriticsParamId()).toString());
	}
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		setValue(getIgnoreDiacriticsParamId(), ignoreDiacritics);
	}
	
	public String getSelectionModeParamId() {
		return this.selectionModeParamId;
	}
	
	public SelectionMode getSelectionMode() {
		return SelectionMode.valueOf(getValue(getSelectionModeParamId()).toString().toUpperCase());
	}
	
	public void setSelectionMode(SelectionMode mode) {
		setValue(getSelectionModeParamId(), mode.toString());
	}
	
	public String getSelectedDiacriticsParamId() {
		return this.selectedDiacriticsParamId;
	}
	
	public IPATranscript stripDiacritics(IPATranscript ipa) {
		Predicate<Diacritic> pred = (dia) -> {
			var cnt = getSelectedDiacritics().parallelStream()
				.filter( (d) -> dia.getText().contentEquals(d.getText()) )
				.count();
			return (getSelectionMode() == SelectionMode.EXCEPT ? cnt > 0 : cnt == 0);
		};
		return ipa.stripDiacritics(pred);
	}
	
	private Collection<Diacritic> collectionFromString(String txt) {
		IPAElementFactory factory = new IPAElementFactory();
		return (txt.replaceAll("[\\[\\]]", "").trim().length() > 0 ? 
				Arrays.stream(txt.replaceAll("[\\[\\]]", "").split(",")).map( (s) -> factory.createDiacritic( s.trim().charAt(0)) ).collect(Collectors.toList())
				: new ArrayList<>());
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Diacritic> getSelectedDiacritics() {
		return (Collection<Diacritic>)super.getValue(getSelectedDiacriticsParamId());
	}
	
	public void clearSelectedDiacritics() {
		setValue(getSelectedDiacriticsParamId(), new ArrayList<Diacritic>());
	}
	
	public void setSelectedDiacritics(Collection<Diacritic> selectedDiacritics) {
		setValue(getSelectedDiacriticsParamId(), selectedDiacritics);
	}
	
	@Override
	public void setValue(String paramId, Object val) {
		if(getSelectedDiacriticsParamId().equals(paramId) && !(val instanceof Collection)) {
			setSelectedDiacritics(collectionFromString(val.toString()));
		} else if(getIgnoreDiacriticsParamId().contentEquals(paramId) & !(val instanceof Boolean)) {
			setIgnoreDiacritics(Boolean.valueOf(val.toString()));
		} else {
			super.setValue(paramId, val);
		}
	}

	@Override
	public String getStringRepresentation() {
		return new String("{diacriticOptions}");
	}

}
