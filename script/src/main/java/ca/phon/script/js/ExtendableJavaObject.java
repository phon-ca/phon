/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.script.js;

import java.lang.ref.WeakReference;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import ca.phon.extensions.IExtendable;

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
