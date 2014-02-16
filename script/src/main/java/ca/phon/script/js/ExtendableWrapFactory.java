package ca.phon.script.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import ca.phon.extensions.IExtendable;

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
