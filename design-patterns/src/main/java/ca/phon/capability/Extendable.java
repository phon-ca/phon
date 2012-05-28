package ca.phon.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class to help implement extendable objects.
 * This class performs runtime checking of cabailities
 * to ensure that the provided capability class has an
 * annotation {@link Capability} that matches the target
 * class.
 * 
 * 
 */
public abstract class Extendable implements IExtendable {
	
	/**
	 * Capabilities
	 */
	private final Map<Class<?>, Object> capabilities = 
			Collections.synchronizedMap(new HashMap<Class<?>, Object>());

	@Override
	public Class<?>[] getCapabilities() {
		return capabilities.keySet().toArray(new Class<?>[0]);
	}

	@Override
	public <T> T getCapability(Class<T> cap) {
		Object inst = capabilities.get(cap);
		T retVal = cap.cast(inst);
		return retVal;
	}

	@Override
	public <T> T putCapability(Class<T> cap, T impl) {
		// check the capability annotation
		Class<?> targetClass = getClass();
		Capability capAnnotation = 
				cap.getAnnotation(Capability.class);
		if(capAnnotation == null || !targetClass.isAssignableFrom(capAnnotation.value())) {
			throw new IllegalArgumentException("Class " + cap.getName() + 
					" must have the anntotation '@" + Capability.class.getName() + 
					"(" + targetClass.getName() + ")");
		}
		
		capabilities.put(cap, impl);
		return impl;
	}

	@Override
	public void removeCapability(Class<?> cap) {
		capabilities.remove(cap);
	}

}
