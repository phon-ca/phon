package ca.phon.util.ternarytree;

public interface TernaryTreeNodeVisitor<V> {

	/**
	 * Accept the given tree node
	 * 
	 * @param node
	 */
	public void visit(TernaryTreeNode<V> node);
	
}
