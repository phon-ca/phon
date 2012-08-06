package ca.phon.util.ternarytree;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Node for ternary trees.
 * 
 * @param <V>
 */
public class TernaryTreeNode<V> implements Comparable<TernaryTreeNode<V>> {
	
	/**
	 * Node char
	 */
	private char ch;
	
	/**
	 * Node value, a node is 'terminated' if it's
	 * value is non-<code>null</code>.
	 */
	private V value;
	
	/**
	 * Atomic reference to left child
	 */
	private final AtomicReference<TernaryTreeNode<V>> leftRef = 
			new AtomicReference<TernaryTreeNode<V>>();
	
	/**
	 * Atomic reference to right child
	 */
	private final AtomicReference<TernaryTreeNode<V>> rightRef =
			new AtomicReference<TernaryTreeNode<V>>();
	
	
	/**
	 * Atomic reference to center child
	 */
	private final AtomicReference<TernaryTreeNode<V>> centerRef = 
			new AtomicReference<TernaryTreeNode<V>>();
	
	/**
	 * Constructor
	 */
	public TernaryTreeNode(char ch) {
		super();
		setChar(ch);
	}
	
	public TernaryTreeNode(char ch, V value) {
		super();
		setChar(ch);
		setValue(value);
	}
	
	public char getChar() {
		return this.ch;
	}
	
	public void setChar(char ch) {
		this.ch = ch;
	}
	
	/**
	 * A node is terminated if it has a database entry.
	 * (i.e., if the value is non-<code>null</code>)
	 */
	public boolean isTerminated() {
		return (getValue() != null);
	}

	/**
	 * Get the node's value
	 */
	public V getValue() {
		return value;
	}
	
	/**
	 * Set the node's value
	 */
	public void setValue(V value) {
		this.value = value;
	}

	/**
	 * Get left child
	 */
	public TernaryTreeNode<V> getLeft() {
		return leftRef.get();
	}
	
	/**
	 * Set left child
	 */
	public void setLeft(TernaryTreeNode<V> left) {
		this.leftRef.getAndSet(left);
	}
	
	public TernaryTreeNode<V> getRight() {
		return rightRef.get();
	}

	public void setRight(TernaryTreeNode<V> right) {
		this.rightRef.getAndSet(right);
	}
	
	public TernaryTreeNode<V> getCenter() {
		return centerRef.get();
	}

	public void setCenter(TernaryTreeNode<V> center) {
		this.centerRef.getAndSet(center);
	}
	
	@Override
	public int compareTo(TernaryTreeNode<V> o) {
		return (new Character(ch)).compareTo(o.ch);
	}
	
	/**
	 * Accept a tree node visitor.
	 */
	public void accept(TernaryTreeNodeVisitor<V> visitor) {
		visitor.visit(getLeft());
		visitor.visit(getCenter());
		visitor.visit(getRight());
	}
}
