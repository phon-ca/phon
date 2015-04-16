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
package ca.phon.util;

import java.lang.reflect.Method;

/**
 * Various helper methods which use reflection.
 *
 */
public class ReflectionHelper {
	
	public static void printSuperclassInfo(Class<?> clazz) {
		if(clazz != null)
			recursivePrintSuperclassInfo(clazz);
	}
	
	private static void recursivePrintSuperclassInfo(Class<?> clazz) {
		Class<?> superclazz = clazz.getSuperclass();
		
		if(superclazz != null) {
			System.out.println(clazz.getName() + " extends " + superclazz.getName());
			System.out.println(superclazz.getName() + " Methods:");
			Method[] ms = superclazz.getMethods();
			for(Method m:ms) {
				System.out.println("\t" + m.getName());
			}
			recursivePrintSuperclassInfo(superclazz);
		} else {
			return;
		}
	}
	
	/**
	 * Ensure that the calling method is within
	 * the given depth of the stack.
	 * 
	 * @param methodSig
	 * @param backCount
	 * @return
	 */
	public static boolean ensureCallingMethod(String className, String methodSig, int depth) {
		boolean retVal = false;
		
		StackTraceElement[] stackTrace = 
			new StackTraceElement[0];
		
		try {
			throw new Exception();
		} catch (Exception e) {
			stackTrace = e.getStackTrace();
		}
		
		for(int i = 1; i <= depth && i < stackTrace.length; i++) {
			StackTraceElement ele = stackTrace[i];
			if(ele.getClassName().equals(className)
					&& ele.getMethodName().equals(methodSig)) {
				retVal = true;
				break;
			}
		}	
		
		return retVal;
	}
}
