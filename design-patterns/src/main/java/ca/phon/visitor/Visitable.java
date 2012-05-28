package ca.phon.visitor;

/**
 * Interface for classes which wish to implement the visitor
 * pattern.
 * 
 * @param T the parameterized type of the visitor to accept.
 *  This should be the base type of the objects being visited.
 */
public interface Visitable<T> {
	
	/**
	 * Accept the given visitor.
	 * 
	 * @param the visitor
	 */
	public void accept(Visitor<T> visitor);

}
