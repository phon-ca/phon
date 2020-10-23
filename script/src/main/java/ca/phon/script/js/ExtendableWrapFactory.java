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
package ca.phon.script.js;

import org.mozilla.javascript.*;

import ca.phon.extensions.*;

public class ExtendableWrapFactory extends WrapFactory {

	@Override
	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
			Object javaObject, Class<?> staticType) {
		if(javaObject != null) {
			if(IExtendable.class.isAssignableFrom(javaObject.getClass())) {
				return new ExtendableJavaObject(cx, scope, IExtendable.class.cast(javaObject), staticType);
			} else {
				return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
			}
		} else {
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
		}
	}
	
}
