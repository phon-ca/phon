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

package ca.phon.script.params;

import java.util.Collection;

import ca.phon.util.Tuple;

/**
 * A group of script parameters.
 */
public class ScriptParamGroup extends Tuple<ScriptParam, ScriptParam[]>{

	public ScriptParamGroup(ScriptParam sep, Collection<ScriptParam> params) {
		this(sep, params.toArray(new ScriptParam[0]));
	}

	public ScriptParamGroup(ScriptParam sep, ScriptParam[] params) {
		super(sep, params);
	}

	public ScriptParam getSeparator() {
		return super.getObj1();
	}

	public ScriptParam[] getParams() {
		return super.getObj2();
	}

	public boolean hasChanged() {
		boolean retVal = false;

		for(ScriptParam sp:getObj2()) {
			retVal |= sp.hasChanged();
		}

		return retVal;
	}
}
