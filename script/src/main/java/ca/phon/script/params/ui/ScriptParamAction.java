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
package ca.phon.script.params.ui;

import java.lang.ref.WeakReference;

import javax.swing.AbstractAction;

import ca.phon.script.params.ScriptParam;

/**
 * Abstract class for script parameter actions.
 */
public abstract class ScriptParamAction extends AbstractAction {
	
	private static final long serialVersionUID = 4191137819061729454L;
	
	private final WeakReference<ScriptParam> paramRef;
	
	private final String paramId;
	
	public ScriptParamAction(ScriptParam param, String id) {
		super();
		this.paramRef = new WeakReference<ScriptParam>(param);
		this.paramId = id;
	}

	public String getParamId() {
		return paramId;
	}
	
	public ScriptParam getScriptParam() {
		return paramRef.get();
	}

}
