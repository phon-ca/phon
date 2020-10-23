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
package ca.phon.script.js;

import java.lang.ref.*;

import org.mozilla.javascript.*;

import ca.phon.extensions.*;

/**
 * JavaScript wrapper object for {@link IExtendable} objects.
 */
public class ExtendableJavaObject extends NativeJavaObject {
	
	private static final long serialVersionUID = 8127591195524443349L;
	
	private final Context context;
	
	private final WeakReference<IExtendable> extendableObjectRef;
	
	public ExtendableJavaObject(Context cx, Scriptable scope, IExtendable extendableJavaObject,
			Class<?> staticType) {
		super(scope, extendableJavaObject, staticType);
		this.context = cx;
		extendableObjectRef = new WeakReference<IExtendable>(extendableJavaObject);
	}
	
	

	@Override
	public Object get(String name, Scriptable start) {
		Object retVal = super.get(name, start);
		
		if(retVal == NOT_FOUND) {
			// check for extension class
			final IExtendable extendableObject = extendableObjectRef.get();
			if(extendableObject != null) {
				for(Class<?> extensionClass:extendableObject.getExtensions()) {
					final String classShortName = extensionClass.getSimpleName();
					if(classShortName != null && classShortName.equalsIgnoreCase(name)) {
						retVal = extendableObject.getExtension(extensionClass);
						retVal = context.getWrapFactory().wrap(context, start, retVal, null);
						break;
					}
				}
			}
		}
		
		return retVal;
	}

	@Override
	public boolean has(String name, Scriptable start) {
		boolean retVal = super.has(name, start);
		
		if(!retVal) {
			// check for extension class
			final IExtendable extendableObject = extendableObjectRef.get();
			if(extendableObject != null) {
				for(Class<?> extensionClass:extendableObject.getExtensions()) {
					final String classShortName = extensionClass.getSimpleName();
					if(classShortName != null && classShortName.equalsIgnoreCase(name)) {
						retVal = extendableObject.getExtension(extensionClass) != null;
						break;
					}
				}
			}
		}
		
		return retVal;
	}

}
