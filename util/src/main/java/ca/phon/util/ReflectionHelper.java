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
