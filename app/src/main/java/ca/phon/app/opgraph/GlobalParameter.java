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
package ca.phon.app.opgraph;

/**
 * Global parameters which override local node settings.
 */
public enum GlobalParameter {
	CASE_SENSITIVE("__caseSensitive"),
	IGNORE_DIACRITICS("__ignoreDiacritics"),
	RETAIN_DIACRITICS_SET("__retainDiacritics"),
	IGNORE_TONES("__ignoreTones"),
	INVENTORY_GROUPING_COLUMN("__inventoryGroupingColumn");
	
	private String paramId;
	
	private GlobalParameter(String paramId) {
		this.paramId = paramId;
	}
	
	public String getParamId() {
		return this.paramId;
	}
	
}
