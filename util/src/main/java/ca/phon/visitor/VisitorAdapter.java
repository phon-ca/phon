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
package ca.phon.visitor;

import java.lang.reflect.*;
import java.util.*;

import ca.phon.visitor.annotation.*;

/**
 * Multiple dispatch visitor adapter.  The generic {@link #visit(Object)}
 * method will look for other methods in this class with have the
 * {@link Visits} annotation declaring the given objects specific type.
 * Other wise, the {@link #fallbackVisit(Object)} method is called.
 * @param <T>
 */
public abstract class VisitorAdapter<T> implements Visitor<T> {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(VisitorAdapter.class
			.getName());

	@Override
	public void visit(T obj) {
		if(obj == null) 
			throw new NullPointerException();
		// get the class type of the given object
		Class<?> objType = obj.getClass();
		Method visitMethod = findVisitMethod(objType);
		
		if(visitMethod != null) {
			try {
				visitMethod.invoke(this, obj);
			} catch (IllegalArgumentException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			} catch (IllegalAccessException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			} catch (InvocationTargetException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		} else {
			fallbackVisit(obj);
		}
	}
	
	/**
	 * Look for a method having the {@link Visits} annotation
	 * of the given type.
	 * 
	 * @param type
	 */
	private Method findVisitMethod(Class<?> type) {
		Method retVal = null;
		Class<?> adapterClass = this.getClass();
		List<Method> potentialMethods = new ArrayList<>();
		for(Method m:adapterClass.getMethods()) {
			// check for the visits annotation
			Visits visits = m.getAnnotation(Visits.class);
			if(visits != null) {
				// check type
				Class<?>[] paramTypes = m.getParameterTypes();
				
				if(paramTypes.length == 1 && paramTypes[0] == type) {
					retVal = m;
					break;
				} else if(paramTypes.length == 1 && paramTypes[0].isAssignableFrom(type)) {
					potentialMethods.add(m);
				}
			}
		}
		if(retVal == null && potentialMethods.size() > 0) {
			retVal = potentialMethods.get(0);
		}
		return retVal;
	}

	/**
	 * Generic, i.e., fallback, processing method.
	 * 
	 * @param T the object to visit
	 */
	public abstract void fallbackVisit(T obj);
}
