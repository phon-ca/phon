package ca.phon.util.resources;

import java.util.Iterator;

/**
 * Responsible for finding resources for a resource loader.
 * 
 * Implementing classes must defind an iterator
 * for loading objects of the parameterized type.
 */
public interface ResourceHandler<T> extends Iterable<T> {
	
	/**
	 * Return an iterator for instances of
	 * type T.
	 * 
	 * @return an iterator providing instances
	 *  of the parameterized type
	 *  
	 */
	public Iterator<T> iterator();

}
