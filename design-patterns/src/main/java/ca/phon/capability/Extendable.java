package ca.phon.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

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
	
	private final static Logger LOGGER = Logger.getLogger(Extendable.class.getName());
	
	/**
	 * Capabilities
	 */
	private final Map<Class<?>, Object> capabilities = 
			Collections.synchronizedMap(new HashMap<Class<?>, Object>());
	
	protected Extendable() {
		loadAutomaticExtensions();
	}
	
	/**
	 * Load automatic extensions as defined in the classpath
	 * META-INF/services files
	 */
	private void loadAutomaticExtensions() {
		ServiceLoader<IExtension> services =
				ServiceLoader.load(IExtension.class);
		for(IExtension extension:services) {
			Class<?> extClass = extension.getClass();
			Class<?> myClass = getClass();
			
			// check for the @Capability annotation
			Extension ext = extClass.getAnnotation(Extension.class);
			if(ext != null) {
				// make sure capability defines the correct class
				if(ext.value().isAssignableFrom(myClass)) {
					extension.installExtension(this);
				}
			} else {
				LOGGER.warning(extClass.getName() + 
						" missing @Capability annotation.  Not loaded into object " + toString());
			}
		}
	}

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
		if(capAnnotation == null || !capAnnotation.value().isAssignableFrom(targetClass)) {
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
