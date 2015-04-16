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
package ca.phon.visitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.visitor.annotation.Visits;

/**
 * Multiple dispatch visitor adapter.  The generic {@link #visit(Object)}
 * method will look for other methods in this class with have the
 * {@link Visits} annotation declaring the given objects specific type.
 * Other wise, the {@link #fallbackVisit(Object)} method is called.
 * @param <T>
 */
public abstract class VisitorAdapter<T> implements Visitor<T> {
	
	private final static Logger LOGGER = Logger.getLogger(VisitorAdapter.class
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (IllegalAccessException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (InvocationTargetException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
		for(Method m:adapterClass.getMethods()) {
			// check for the visits annotation
			Visits visits = m.getAnnotation(Visits.class);
			if(visits != null) {
				// check type
				Class<?>[] paramTypes = m.getParameterTypes();
				
				if(paramTypes.length == 1 && paramTypes[0].isAssignableFrom(type)) {
					retVal = m;
					break;
				}
			}
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
