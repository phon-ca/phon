package ca.phon.ipa.tree;

public interface IpaTernaryTreeNodeVisitor<V> {

	/**
	 * Accept the given tree node
	 * 
	 * @param node
	 * @return <code>true</code> if this visit
	 *  should stop when using {@link IpaTernaryTreeNode#acceptVisitFirst(IpaTernaryTreeNodeVisitor)}
	 */
	public boolean visit(IpaTernaryTreeNode<V> node);
	
	
}
