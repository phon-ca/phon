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
package ca.phon.xml;

import java.util.*;

/**
 * Obtain instances of {@link XMLSerializer} objects.
 * 
 */
public class XMLSerializerFactory {
	
	public XMLSerializerFactory() {
		
	}
	
	/**
	 * Obtain an XMLSerializer instance for the given class.
	 * 
	 * @param type
	 * @return serializer for the given type or <code>null</code> of
	 *  no serializer is found
	 */
	public XMLSerializer newSerializerForType(Class<?> type) {
		final ServiceLoader<XMLSerializer> loader = ServiceLoader.load(XMLSerializer.class);
		final Iterator<XMLSerializer> itr = loader.iterator();
		
		XMLSerializer retVal = null;
		while(itr.hasNext() && retVal == null) {
			final XMLSerializer currentSerializer = itr.next();
			if(currentSerializer.declaredType() == type) {
				retVal = currentSerializer;
			}
		}
		
		return retVal;
	}
	

}
