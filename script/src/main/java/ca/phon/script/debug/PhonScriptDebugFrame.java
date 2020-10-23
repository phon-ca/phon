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
package ca.phon.script.debug;

import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.*;

public class PhonScriptDebugFrame implements DebugFrame {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PhonScriptDebugFrame.class.getName());
	
	private DebuggableScript fnOrScript;
	
	public PhonScriptDebugFrame(DebuggableScript fnOrScript) {
		this.fnOrScript = fnOrScript;
	}

	@Override
	public void onDebuggerStatement(Context arg0) {
	}

	@Override
	public void onEnter(Context arg0, Scriptable arg1, Scriptable arg2,
			Object[] arg3) {
		
	}

	@Override
	public void onExceptionThrown(Context arg0, Throwable arg1) {
		LOGGER.error( arg1.getLocalizedMessage(), arg1);
	}

	@Override
	public void onExit(Context arg0, boolean arg1, Object arg2) {
		
	}

	@Override
	public void onLineChange(Context arg0, int arg1) {
		System.out.println(fnOrScript.getFunctionName() + "(" + arg1 + ")");
	}
	
}
