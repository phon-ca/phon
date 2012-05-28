package ca.phon.visitor;

/**
 * Interface for visiting object of the parameterized
 * type.
 * 
 * @param T the base type of the objects to visit
 */
public interface Visitor<T> {
	
	/**
	 * Generic visit method.
	 * 
	 * @param obj
	 */
	public void visit(T obj);
	
}
