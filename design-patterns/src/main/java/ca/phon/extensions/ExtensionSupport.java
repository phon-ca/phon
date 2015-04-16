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
package ca.phon.extensions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;

/**
 * <p>Class which provides extension support for objects
 * that implement the {@link IExtendable} interface.</p>
 * 
 * <p>This class can also perform automatic installation 
 * of extensions declared in the 
 * <code>META-INF/services/ca.phon.extensions.ExtensionProvider</code>.
 * file.</p>
 * 
 * <p>Example:<br/>
 * 
 * The {@link IExtendable} object:
 * <pre>
 * public class MyExtendableObject implements IExtendable {
 * 
 *    private final ExtensionSupport = new ExtensionSupport(MyExtendableObject.class, this);
 * 
 *    ...
 * }
 * </pre>
 * 
 * Custom extension (note the requirement of the {@link Extension} annotation:)
 * <pre>
 * &#64;MyExtension(MyExtendableObject.class)
 * public class MyExtension {
 * }
 * </pre>
 * 
 * Extension provider (note the requirement of the {@link Extension} annotation:)
 * <pre>
 * &#64;Extension(MyExtendableObject.class)
 * public class MyExtensionProvider implements ExtensionProvider {
 * 
 *     &#64;Overrides
 *     public void installExtension(IExtendable obj) {
 *         obj.putExtension(MyExtension.class, new MyExtension());
 *     }
 * 
 * }
 * </pre>
 * 
 * In the <code>META-INF/services/ca.phon.extensions.ExtensionProvider</code> file:
 * <pre>
 * my.class.path.MyExtensionProvider
 * </pre>
 * 
 * Now, every time a new instance of <code>MyExtendableObject</code> is created
 * a new <code>MyExtension</code> object will also be created and attached to
 * our {@link IExtendable} object.
 * </p>
 *  
 */
public final class ExtensionSupport implements IExtendable {
	
	private final static Logger LOGGER = Logger.getLogger(ExtensionSupport.class.getName());
	
	private final static Map<Class<?>, List<ExtensionProvider>> _extMap =
			Collections.synchronizedMap(new HashMap<Class<?>, List<ExtensionProvider>>());
	
	private static <T extends IExtendable> void initExtensions(Class<T> type, T obj) {
		
	}
	
	/**
	 * extensions
	 */
	private final Map<Class<?>, Object> extensions = 
			Collections.synchronizedMap(new HashMap<Class<?>, Object>());
	/**
	 * Declared type
	 */
	private final Class<? extends IExtendable> declaredType;
	
	/**
	 * parent object ref
	 */
	private final WeakReference<IExtendable> parent;
	
	/**
	 * Create a new extension support object for the
	 * given object.  This class will automatically load
	 * any extensions 
	 * @param declaredType
	 * @param obj
	 */
	public <T extends IExtendable> ExtensionSupport(Class<? extends T> declaredType, T obj) {
		this.declaredType = declaredType;
		parent = new WeakReference<IExtendable>(obj);
	}
	
	/**
	 * Load automatic extensions as defined in the classpath
	 * META-INF/services files
	 */
	public void initExtensions() {
		List<ExtensionProvider> providers = _extMap.get(declaredType);
		if(providers == null) {
			providers = new ArrayList<ExtensionProvider>();
			_extMap.put(declaredType, providers);
			final ServiceLoader<ExtensionProvider> services =
					ServiceLoader.load(ExtensionProvider.class);
			for(ExtensionProvider provider:services) {
				final Class<?> providerClass = provider.getClass();
				
				// check for the @Extension annotation
				final Extension extensionClass = providerClass.getAnnotation(Extension.class);
				if(extensionClass != null && extensionClass.value().isAssignableFrom(declaredType)) {
					providers.add(provider);
				}
			}
		}
		for(ExtensionProvider provider:providers) {
			provider.installExtension(parent.get());
		}
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return Collections.unmodifiableSet(extensions.keySet());
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		final Object inst = extensions.get(cap);
		final T retVal = cap.cast(inst);
		return retVal;
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		final Extension capAnnotation = 
				cap.getAnnotation(Extension.class);
		// TODO decided if this really needs to have this restriction
//		if(capAnnotation == null || !capAnnotation.value().isAssignableFrom(declaredType)) {
//			throw new IllegalArgumentException("Class " + cap.getName() + 
//					" must have the anntotation '@" + Extension.class.getName() + 
//					"(" + declaredType.getName() + ")");
//		}
		
		extensions.put(cap, impl);
		return impl;
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		final Object removed = extensions.remove(cap);
		T retVal = null;
		if(removed != null) {
			retVal = cap.cast(removed);
		}
		return retVal;
	}

}
