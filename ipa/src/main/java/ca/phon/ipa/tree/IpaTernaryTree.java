/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ipa.tree;

import ca.phon.ipa.*;
import ca.phon.ipa.features.IPAElementComparator;
import ca.phon.ipa.tree.IpaTernaryTreeNode.Position;

import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Ternary tree implementation for IPA transcripts
 * based on features
 *
 */
public class IpaTernaryTree<V> implements Map<IPATranscript, V> {
	
	/**
	 * Root
	 */
	private IpaTernaryTreeNode<V> root = null;
	
	/**
	 * re-entrant lock
	 */
	private final Lock lock = new ReentrantLock();
	
	private Comparator<IPAElement> comparator;
	
	public IpaTernaryTree() {
		this(new IPAElementComparator());
	}
	
	public IpaTernaryTree(Comparator<IPAElement> comparator) {
		super();
		this.comparator = comparator;
	}
	
	public IpaTernaryTreeNode<V> getRoot() {
		return root;
	}

	@Override
	public int size() {
		return keySet().size();
	}

	@Override
	public boolean isEmpty() {
		return values().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		if(!(key instanceof IPATranscript))
			throw new IllegalArgumentException("key must be of type " + IPATranscript.class.getName());
		final IpaTernaryTreeNode<V> node = findNode((IPATranscript)key, false);
		return (node != null);
	}

	@Override
	public boolean containsValue(Object value) {
		return values().contains(value);
	}

	@Override
	public V get(Object key) {
		if(!(key instanceof IPATranscript))
			throw new IllegalArgumentException("key must be of type " + IPATranscript.class.getName());
		final IpaTernaryTreeNode<V> node = findNode((IPATranscript)key, false);
		return (node != null && node.isTerminated() ? node.getValue() : null);
	}

	@Override
	public V put(IPATranscript key, V value) {
		final IpaTernaryTreeNode<V> node = findNode(key, true);
		if(node == null) return null;
		return node.setValue(value);
	}

	@Override
	public V remove(Object key) {
		if(!(key instanceof IPATranscript))
			throw new IllegalArgumentException("key must be of type " + IPATranscript.class.getName());
		final IpaTernaryTreeNode<V> node = findNode((IPATranscript)key, false);
		return (node != null ? node.setValue(null) : null);
	}

	@Override
	public void putAll(Map<? extends IPATranscript, ? extends V> m) {
		for(IPATranscript key:m.keySet()) {
			put(key, m.get(key));
		}
	}

	@Override
	public void clear() {
		root = null;
	}

	@Override
	public Set<IPATranscript> keySet() {
		final KeyVisitor visitor = new KeyVisitor();
		lock.lock();
		final IpaTernaryTreeNode<V> root = getRoot();
		if(root != null)
			root.acceptVisitMiddle(visitor);
		lock.unlock();
		return visitor.keySet;
	}

	@Override
	public Collection<V> values() {
		final ValuesVisitor visitor = new ValuesVisitor();
		lock.lock();
		final IpaTernaryTreeNode<V> root = getRoot();
		if(root != null)
			root.acceptVisitMiddle(visitor);
		lock.unlock();
		return visitor.values;
	}

	@Override
	public Set<java.util.Map.Entry<IPATranscript, V>> entrySet() {
		final EntryVisitor visitor = new EntryVisitor();
		lock.lock();
		final IpaTernaryTreeNode<V> root = getRoot();
		if(root != null)
			root.acceptVisitMiddle(visitor);
		lock.unlock();
		return visitor.values;
	}
	
	public Set<IPATranscript> keysWithPrefix(IPATranscript prefix) {
		final KeyVisitor visitor = new KeyVisitor();
		final IpaTernaryTreeNode<V> node = findNode(prefix, false);
		lock.lock();
		if(node != null) {
			if(node.isTerminated())
				visitor.keySet.add(node.getPrefix());
			if(node.getCenter() != null)
				node.getCenter().acceptVisitMiddle(visitor);
		}
		lock.unlock();
		return visitor.keySet;
	}
	
	public Collection<V> valuesWithPrefix(IPATranscript prefix) {
		final ValuesVisitor visitor = new ValuesVisitor();
		final IpaTernaryTreeNode<V> node = findNode(prefix, false);
		lock.lock();
		if(node != null) {
			if(node.isTerminated())
				visitor.values.add(node.getValue());
			if(node.getCenter() != null)
				node.getCenter().acceptVisitMiddle(visitor);
		}
		lock.unlock();
		return visitor.values;
	}
	
	public Set<java.util.Map.Entry<IPATranscript, V>> entriesWithPrefix(IPATranscript prefix) {
		final EntryVisitor visitor = new EntryVisitor();
		final IpaTernaryTreeNode<V> node = findNode(prefix, false);
		lock.lock();
		if(node != null) {
			if(node.isTerminated())
				visitor.values.add(new Entry(node.getPrefix(), node.getValue()));
			if(node.getCenter() != null) {
				node.getCenter().acceptVisitMiddle(visitor);
			}
		}
		lock.unlock();
		return visitor.values;
	}
	
	IpaTernaryTreeNode<V> findNode(IPATranscript key, boolean create) {
		if(key.length() == 0) return null;
		IpaTernaryTreeNode<V> retVal = null;
		
		lock.lock();
		
		IpaTernaryTreeNode<V> prevNode = null;
		IpaTernaryTreeNode<V> currentNode = getRoot();
		int eleIndex = 0;
		Position lastPos = Position.EQUAL;
		while(true) {
			IPAElement keyEle = key.elementAt(eleIndex);
			if(currentNode == null) {
				if(create) {
					final IpaTernaryTreeNode<V> newNode = new IpaTernaryTreeNode<V>(prevNode, keyEle);
					if(prevNode == null) {
						root = newNode;
					} else {
						prevNode.setChild(newNode, lastPos);
					}
					currentNode = newNode;
				} else {
					break;
				}
			}
			prevNode = currentNode;
			
			IPAElement splitEle = currentNode.getEle();
			int cmp = comparator.compare(keyEle, splitEle);
			
			if(cmp == 0) {
				eleIndex++;
				if(eleIndex == key.length()) {
					retVal = currentNode;
					break;
				}
				currentNode = currentNode.getCenter();
				lastPos = Position.EQUAL;
			} else if(cmp < 0) {
				currentNode = currentNode.getLeft();
				lastPos = Position.LOW;
			} else if(cmp > 0) {
				currentNode = currentNode.getRight();
				lastPos = Position.HIGH;
			}
		}
		
		lock.unlock();
		
		return retVal;
	}
	
	/*
	 * Base class for visitors
	 */
	private abstract class ContainsVisitor<T> implements IpaTernaryTreeNodeVisitor<V> {
		
		private final IPATranscript ipa;
		
		public ContainsVisitor(IPATranscript ipa) {
			super();
			this.ipa = ipa;
		}
		
		@Override
		public boolean visit(IpaTernaryTreeNode<V> node) {
			if(ipa.length() == 0) return false;
			
			final IPAElement ele = ipa.elementAt(ipa.length()-1);
			final IPAElement nodeEle = node.getEle();
			final int cmp = comparator.compare(ele, nodeEle);
			
			if(cmp == 0) {
				final IPATranscript prefix = node.getPrefix();
				// using toString here should be accurate enough
				if(prefix.toString().endsWith(ipa.toString())) {
					accept(node);
					return true;
				}
			}
			return false;
		}
		
		public abstract T getResult();
		
		public abstract void accept(IpaTernaryTreeNode<V> node);
		
	}
	
	private class KeyContainsVisitor extends ContainsVisitor<Set<IPATranscript>> {
		
		final Set<IPATranscript> keySet = new LinkedHashSet<IPATranscript>();
		
		public KeyContainsVisitor(IPATranscript ipa) {
			super(ipa);
		}
		
		@Override
		public Set<IPATranscript> getResult() {
			return keySet;
		}
		
		@Override
		public void accept(IpaTernaryTreeNode<V> node) {
			final KeyVisitor visitor = new KeyVisitor();
			node.acceptVisitLast(visitor);
			keySet.addAll(visitor.keySet);
		}
		
	}
	
	private class ValuesForKeyContainsVisitor extends ContainsVisitor<Collection<V>> {
		
		private final Collection<V> values = new ArrayList<V>();
		
		public ValuesForKeyContainsVisitor(IPATranscript ipa) {
			super(ipa);
		}
		
		@Override
		public Collection<V> getResult() {
			return values;
		}
		
		@Override
		public void accept(IpaTernaryTreeNode<V> node) {
			final ValuesVisitor visitor = new ValuesVisitor();
			node.acceptVisitFirst(visitor);
			values.addAll(visitor.values);
		}
		
	}
	
	private class EntriesForKeyContainsVisitor extends ContainsVisitor<Set<Map.Entry<IPATranscript, V>>> {
		
		private Set<Map.Entry<IPATranscript, V>> entrySet  =
				new LinkedHashSet<Map.Entry<IPATranscript,V>>();
		
		public EntriesForKeyContainsVisitor(IPATranscript ipa) {
			super(ipa);
		}
		
		@Override
		public Set<Map.Entry<IPATranscript, V>> getResult() {
			return entrySet;
		}
		
		@Override
		public void accept(IpaTernaryTreeNode<V> node) {
			final EntryVisitor visitor = new EntryVisitor();
			node.acceptVisitLast(visitor);
			entrySet.addAll(visitor.values);
		}
		
	}
	
	private class KeyVisitor implements IpaTernaryTreeNodeVisitor<V> {
		final Set<IPATranscript> keySet = new LinkedHashSet<IPATranscript>();
		
		@Override
		public boolean visit(IpaTernaryTreeNode<V> node) {
			if(node.isTerminated()) {
				keySet.add(node.getPrefix());
			}
			return false;
		}
		
	}
	
	private class ValuesVisitor implements IpaTernaryTreeNodeVisitor<V> {
		final Collection<V> values = new ArrayList<V>();
		
		@Override
		public boolean visit(IpaTernaryTreeNode<V> node) {
			if(node.isTerminated()) {
				values.add(node.getValue());
			}
			return false;
		}
	}
	
	private class EntryVisitor implements IpaTernaryTreeNodeVisitor<V> {
		final Set<Map.Entry<IPATranscript, V>> values = new LinkedHashSet<Map.Entry<IPATranscript,V>>();
	
		@Override
		public boolean visit(IpaTernaryTreeNode<V> node) {
			if(node.isTerminated()) {
				final Entry entry = new Entry(node.getPrefix(), node.getValue());
				values.add(entry);
			}
			return false;
		}
	}
	
	private class Entry implements Map.Entry<IPATranscript, V> {
		
		private final IPATranscript key;
		
		private V value;
		
		public Entry(IPATranscript key, V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public IPATranscript getKey() {
			return this.key;
		}
		
		@Override
		public V getValue() {
			return this.value;
		}
		
		@Override
		public V setValue(V object) {
			final V oldVal = this.value;
			this.value = object;
			return oldVal;
		}
		
	}
	
	

}
