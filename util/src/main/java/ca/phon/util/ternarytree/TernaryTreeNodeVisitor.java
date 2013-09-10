package ca.phon.util.ternarytree;


public interface TernaryTreeNodeVisitor<V> {

	/**
	 * Accept the given tree node
	 * 
	 * @param node
	 * @return <code>true</code> if this visit
	 *  should stop when using {@link TernaryTreeNode#acceptVisitFirst(TernaryTreeNodeVisitor)}
	 */
	public boolean visit(TernaryTreeNode<V> node);
	
}
