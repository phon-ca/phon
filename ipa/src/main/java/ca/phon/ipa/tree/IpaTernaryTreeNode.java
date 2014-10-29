package ca.phon.ipa.tree;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;

public class IpaTernaryTreeNode<V> {

	public static enum Position {
		LOW,
		EQUAL,
		HIGH;
	}
	
	private IPAElement ele;
	
	private V value;
	
	private IpaTernaryTreeNode<V> parent;
	
	private IpaTernaryTreeNode<V> left;
	
	private IpaTernaryTreeNode<V> right;
	
	private IpaTernaryTreeNode<V> center;
	
	public IpaTernaryTreeNode(IpaTernaryTreeNode<V> parent, IPAElement ele) {
		this(parent, ele, null);
	}
	
	public IpaTernaryTreeNode(IpaTernaryTreeNode<V> parent, IPAElement ele, V value) {
		super();
		setParent(parent);
		setEle(ele);
		setValue(value);
	}
	
	/**
	 * A node is terminated if it has a database entry.
	 * (i.e., if the value is non-<code>null</code>)
	 */
	public boolean isTerminated() {
		return (getValue() != null);
	}
	
	public boolean isRoot() {
		return (getParent() == null);
	}

	public IPAElement getEle() {
		return ele;
	}

	public void setEle(IPAElement ele) {
		this.ele = ele;
	}

	public V getValue() {
		return value;
	}

	public V setValue(V value) {
		final V oldVal = this.value;
		this.value = value;
		return oldVal;
	}

	public IpaTernaryTreeNode<V> getParent() {
		return parent;
	}

	public void setParent(IpaTernaryTreeNode<V> parent) {
		this.parent = parent;
	}

	public IpaTernaryTreeNode<V> getLeft() {
		return left;
	}

	public void setLeft(IpaTernaryTreeNode<V> left) {
		this.left = left;
	}

	public IpaTernaryTreeNode<V> getRight() {
		return right;
	}

	public void setRight(IpaTernaryTreeNode<V> right) {
		this.right = right;
	}

	public IpaTernaryTreeNode<V> getCenter() {
		return center;
	}

	public void setCenter(IpaTernaryTreeNode<V> center) {
		this.center = center;
	}
	
	public IpaTernaryTreeNode<V> getChild(Position position) {
		IpaTernaryTreeNode<V> retVal = null;
		
		switch(position) {
		case LOW:
			retVal = getLeft();
			break;
			
		case EQUAL:
			retVal = getCenter();
			break;
			
		case HIGH:
			retVal = getRight();
			break;
			
		default:
			break;
		}
		return retVal;
	}
	
	public void setChild(IpaTernaryTreeNode<V> child, Position position) {
		switch(position) {
		case LOW:
			setLeft(child);
			break;
			
		case EQUAL:
			setCenter(child);
			break;
			
		case HIGH:
			setRight(child);
			break;
			
		default:
			break;
		}
	}
	
	public IPATranscript getPrefix() {
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		builder.append(getEle());
		IpaTernaryTreeNode<V> child = this;
		IpaTernaryTreeNode<V> parent = getParent();
		
		while(parent != null) {
			if(parent.getCenter() == child)
				builder.append(parent.getEle());
			child = parent;
			parent = parent.getParent();
		}
		
		return builder.reverse().toIPATranscript();
	}
	
	public void acceptVisitLast(IpaTernaryTreeNodeVisitor<V> visitor) {
		if(getLeft() != null) 
			getLeft().acceptVisitLast(visitor);
		if(getCenter() != null)
			getCenter().acceptVisitLast(visitor);
		if(getRight() != null)
			getRight().acceptVisitLast(visitor);
		visitor.visit(this);
	}
	
	public void acceptVisitFirst(IpaTernaryTreeNodeVisitor<V> visitor) {
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
