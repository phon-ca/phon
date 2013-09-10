package ca.hedlund.tst;


import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Node for ternary trees.
 * 
 * @param <V>
 */
public class TernaryTreeNode<V> {
	
	public static enum Position {
		LOW,
		EQUAL,
		HIGH;
	}
	
	/**
	 * Node char
	 */
	private volatile char ch;
	
	/**
	 * Node value, a node is 'terminated' if it's
	 * value is non-<code>null</code>.
	 */
	private final AtomicReference<V> valueRef = 
			new AtomicReference<V>();
	
	/**
	 * parent reference
	 */
	private final AtomicReference<TernaryTreeNode<V>> parentRef = 
			new AtomicReference<TernaryTreeNode<V>>();
	
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
	public TernaryTreeNode(TernaryTreeNode<V> parent, char ch) {
		this(parent, ch, null);
	}
	
	public TernaryTreeNode(TernaryTreeNode<V> parent, char ch, V value) {
		super();
		setParent(parent);
		setChar(ch);
		setValue(value);
	}
	
	public TernaryTreeNode<V> getParent() {
		return parentRef.get();
	}
	
	public void setParent(TernaryTreeNode<V> parent) {
		this.parentRef.getAndSet(parent);
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
	
	public boolean isRoot() {
		return getParent() == null;
	}

	/**
	 * Get the node's value
	 */
	public V getValue() {
		return valueRef.get();
	}
	
	/**
	 * Set the node's value
	 */
	public V setValue(V value) {
		return valueRef.getAndSet(value);
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
	
	public TernaryTreeNode<V> getChild(Position pos) {
		TernaryTreeNode<V> retVal = null;
		switch(pos) {
		case LOW:
			retVal = getLeft();
			break;
			
		case EQUAL:
			retVal = getCenter();
			break;
			
		case HIGH:
			retVal = getRight();
			break;
		}
		return retVal;
	}
	
	public void setChild(TernaryTreeNode<V> child, Position pos) {
		switch(pos) {
		case LOW:
			setLeft(child);
			break;
			
		case EQUAL:
			setCenter(child);
			break;
			
		case HIGH:
			setRight(child);
			break;
		}
	}
	
	/**
	 * Returns the full string key for this node
	 * @return
	 */
	public String getPrefix() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(getChar());
		TernaryTreeNode<V> child = this;
		TernaryTreeNode<V> parent = getParent();
		
		while(parent != null) {
			if(parent.getCenter() == child)
				buffer.append(parent.getChar());
			child = parent;
			parent = parent.getParent();
		}
		
		return buffer.reverse().toString();
	}
	
	/**
	 * Accept a tree node visitor.
	 */
	public void acceptVisitLast(TernaryTreeNodeVisitor<V> visitor) {
		if(getLeft() != null)
			getLeft().acceptVisitLast(visitor);
		if(getCenter() != null)
			getCenter().acceptVisitLast(visitor);
		if(getRight() != null)
			getRight().acceptVisitLast(visitor);
		visitor.visit(this);
	}
	
	public void acceptVisitFirst(TernaryTreeNodeVisitor<V> visitor) {
		if(!visitor.visit(this)) {
			if(getLeft() != null)
				getLeft().acceptVisitFirst(visitor);
			if(getCenter() != null)
				getCenter().acceptVisitFirst(visitor);
			if(getRight() != null)
				getRight().acceptVisitFirst(visitor);
		}
	}
	
}
