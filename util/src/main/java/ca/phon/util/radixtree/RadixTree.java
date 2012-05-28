/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.phon.util.radixtree;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A radix tree. Radix trees are String -> Object mappings which allow quick
 * lookups on the strings. Radix trees also make it easy to grab the objects
 * with a common prefix.
 * 
 * @see <a>http://en.wikipedia.org/wiki/Radix_tree</a>
 * 
 * @param <V>  the type of values stored in the tree
 */
public class RadixTree<V> implements Map<String, V>, Serializable {
	/** The root nodes in this tree */
	private RadixTreeNode<V> root;
	
	/**
	 * Default constructor.
	 */
	public RadixTree() {
		this.root = new RadixTreeNode<V>("");
	}
	
	/**
	 * Traverses this radix tree using the given visitor. Note that the tree
	 * will be traversed in lexicographical order.
	 * 
	 * @param visitor  the visitor
	 */
	public void visit(RadixTreeVisitor<V, ?> visitor) {
		visit(root, "", "", visitor);
	}
	
	/**
	 * Traverses this radix tree using the given visitor. Only values with
	 * the given prefix will be visited. Note that the tree will be traversed
	 * in lexicographical order.
	 * 
	 * @param visitor  the visitor
	 * @param prefix  the prefix used to restrict visitation
	 */
	public void visit(RadixTreeVisitor<V, ?> visitor, String prefix) {
		visit(root, prefix, "", visitor);
	}
	
	/**
	 * Visits the given node of this tree with the given prefix and visitor. Also,
	 * recursively visits the left/right subtrees of this node.
	 * 
	 * @param node  the node
	 * @param prefix  the prefix
	 * @param visitor  the visitor
	 */
	private void visit(RadixTreeNode<V> node, String prefixAllowed, String prefix, RadixTreeVisitor<V, ?> visitor) {
		if(node.hasValue() && prefix.startsWith(prefixAllowed))
			visitor.visit(prefix, node.getValue());
		
		for(RadixTreeNode<V> child : node) {
			int prefixLen = prefix.length();
			String newPrefix = prefix + child.getPrefix();
			if(prefixAllowed.length() <= prefixLen
			   || newPrefix.length() <= prefixLen
			   || newPrefix.charAt(prefixLen) == prefixAllowed.charAt(prefixLen))
			{
				visit(child, prefixAllowed, newPrefix, visitor);
			}
		}
	}
	
	@Override
	public void clear() {
		root.getChildren().clear();
	}

	@Override
	public boolean containsKey(final Object keyToCheck) {
		if(keyToCheck == null)
			throw new NullPointerException("key cannot be null");
		
		if(!(keyToCheck instanceof String))
			throw new ClassCastException("keys must be String instances");
		
		RadixTreeVisitor<V, Boolean> visitor = new RadixTreeVisitor<V, Boolean>() {
			boolean found = false;
			
			@Override
			public void visit(String key, V value) {
				if(key.equals(keyToCheck))
					found = true;
			}

			@Override
			public Boolean getResult() {
				return found;
			}
		};
		visit(visitor, (String)keyToCheck);
		return visitor.getResult();
	}

	@Override
	public boolean containsValue(final Object val) {
		RadixTreeVisitor<V, Boolean> visitor = new RadixTreeVisitor<V, Boolean>() {
			boolean found = false;
			
			@Override
			public void visit(String key, V value) {
				if(value != null && value.equals(val))
					found = true;
			}

			@Override
			public Boolean getResult() {
				return found;
			}
		};
		visit(visitor);
		return visitor.getResult();
	}

	@Override
	public Set<Map.Entry<String, V>> entrySet() {
		RadixTreeVisitor<V, Set<Map.Entry<String, V>>> visitor = new RadixTreeVisitor<V, Set<Map.Entry<String, V>>>() {
			Set<Map.Entry<String, V>> result = new HashSet<Map.Entry<String, V>>();
			
			@Override
			public void visit(String key, V value) {
				result.add(new AbstractMap.SimpleEntry<String, V>(key, value));
			}

			@Override
			public Set<Map.Entry<String, V>> getResult() {
				return result;
			}
		};
		visit(visitor);
		return visitor.getResult();
	}

	@Override
	public V get(final Object keyToCheck) {
		if(keyToCheck == null)
			throw new NullPointerException("key cannot be null");
		
		if(!(keyToCheck instanceof String))
			throw new ClassCastException("keys must be String instances");

		RadixTreeVisitor<V, V> visitor = new RadixTreeVisitor<V, V>() {
			V result = null;
			
			@Override
			public void visit(String key, V value) {
				if(key.equals(keyToCheck))
					result = value;
			}

			@Override
			public V getResult() {
				return result;
			}
		};
		visit(visitor, (String)keyToCheck);
		return visitor.getResult();
	}
	
	/**
	 * Gets a list of entries whose associated keys have the given prefix.
	 * 
	 * @param prefix  the prefix to look for
	 * 
	 * @return the list of values
	 * 
	 * @throws NullPointerException if prefix is <code>null</code>
	 */
	public List<Map.Entry<String, V>> getEntriesWithPrefix(String prefix) {
		RadixTreeVisitor<V, List<Map.Entry<String, V>>> visitor = new RadixTreeVisitor<V, List<Map.Entry<String, V>>>() {
			List<Map.Entry<String, V>> result = new ArrayList<Map.Entry<String, V>>();
			
			@Override
			public void visit(String key, V value) {
				result.add(new AbstractMap.SimpleEntry<String, V>(key, value));
			}

			@Override
			public List<Map.Entry<String, V>> getResult() {
				return result;
			}
		};
		visit(visitor, prefix);
		return visitor.getResult();
	}
	
	/**
	 * Gets a list of values whose associated keys have the given prefix.
	 * 
	 * @param prefix  the prefix to look for
	 * 
	 * @return the list of values
	 * 
	 * @throws NullPointerException if prefix is <code>null</code>
	 */
	public List<V> getValuesWithPrefix(String prefix) {
		if(prefix == null)
			throw new NullPointerException("prefix cannot be null");
		
		RadixTreeVisitor<V, List<V>> visitor = new RadixTreeVisitor<V, List<V>>() {
			List<V> result = new ArrayList<V>();
			
			@Override
			public void visit(String key, V value) {
				result.add(value);
			}

			@Override
			public List<V> getResult() {
				return result;
			}
		};
		visit(visitor, prefix);
		return visitor.getResult();
	}
	
	/**
	 * Gets a list of keys with the given prefix.
	 * 
	 * @param prefix  the prefix to look for
	 * 
	 * @return the list of prefixes
	 * 
	 * @throws NullPointerException if prefix is <code>null</code>
	 */
	public List<String> getKeysWithPrefix(String prefix) {
		if(prefix == null)
			throw new NullPointerException("prefix cannot be null");
		
		RadixTreeVisitor<V, List<String>> visitor = new RadixTreeVisitor<V, List<String>>() {
			List<String> result = new ArrayList<String>();
			
			@Override
			public void visit(String key, V value) {
				result.add(key);
			}

			@Override
			public List<String> getResult() {
				return result;
			}
		};
		visit(visitor, prefix);
		return visitor.getResult();
	}

	@Override
	public boolean isEmpty() {
		return root.getChildren().isEmpty();
	}

	@Override
	public Set<String> keySet() {
		RadixTreeVisitor<V, Set<String>> visitor = new RadixTreeVisitor<V, Set<String>>() {
			Set<String> result = new TreeSet<String>();
			
			@Override
			public void visit(String key, V value) {
				result.add(key);
			}

			@Override
			public Set<String> getResult() {
				return result;
			}
		};
		visit(visitor);
		return visitor.getResult();
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> map) {
		for(Map.Entry<? extends String, ? extends V> entry : map.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public int size() {
		RadixTreeVisitor<V, Integer> visitor = new RadixTreeVisitor<V, Integer>() {
			int count = 0;
			
			@Override
			public void visit(String key, V value) {
				++count;
			}

			@Override
			public Integer getResult() {
				return count;
			}
		};
		visit(visitor);
		return visitor.getResult();
	}

	@Override
	public Collection<V> values() {
		RadixTreeVisitor<V, Collection<V>> visitor = new RadixTreeVisitor<V, Collection<V>>() {
			Collection<V> result = new ArrayList<V>();
			
			@Override
			public void visit(String key, V value) {
				result.add(value);
			}

			@Override
			public Collection<V> getResult() {
				return result;
			}
		};
		visit(visitor);
		return visitor.getResult();
	}

	@Override
	public V put(String key, V value) {
		if(key == null)
			throw new NullPointerException("key cannot be null");
		
		return put(key, value, root);
	}
	
	/**
	 * Remove the value with the given key from the subtree rooted at the
	 * given node.
	 * 
	 * @param key  the key
	 * @param node  the node to start searching from
	 * 
	 * @return the old value associated with the given key, or <code>null</code>
	 *         if there was no mapping for <code>key</code> 
	 */
	private V put(String key, V value, RadixTreeNode<V> node) {
		V ret = null;
		
		int largestPrefix = matchingPrefixCharacters(key, node.getPrefix());
		if(largestPrefix == node.getPrefix().length() && largestPrefix == key.length()) {
			// found a node with an exact match
			ret = node.getValue();
			node.setValue(value);
			node.setHasValue(true);
		} else if(largestPrefix == 0
				  || (largestPrefix < key.length() && largestPrefix >= node.getPrefix().length()))
		{
			// key is bigger than the prefix located at this node, so we need to see if
			// there's a child that can possibly share a prefix, and if not, we just add
			// a new node to this node
			boolean found = false;
			String newKey = key.substring(largestPrefix);
			for(RadixTreeNode<V> child : node) {
				if(child.getPrefix().charAt(0) == newKey.charAt(0)) {
					found = true;
					ret = put(newKey, value, child);
					break;
				}
			}
			
			if(!found) {
				RadixTreeNode<V> n = new RadixTreeNode<V>(newKey, value);
				node.getChildren().add(n);
			}
		} else if(largestPrefix < node.getPrefix().length()) {
			// key and node.getPrefix() share a prefix, so split node
			RadixTreeNode<V> n = new RadixTreeNode<V>(node.getPrefix().substring(largestPrefix), node.getValue());
			n.setHasValue(node.hasValue());
			n.getChildren().addAll(node.getChildren());
			
			node.setPrefix(node.getPrefix().substring(0, largestPrefix));
			node.getChildren().clear();
			node.getChildren().add(n);
			
			if(largestPrefix == key.length()) {
				ret = node.getValue();
				node.setValue(value);
				node.setHasValue(true);
			} else {
				RadixTreeNode<V> keyNode = new RadixTreeNode<V>(key.substring(largestPrefix), value);
				node.getChildren().add(keyNode);
				node.setHasValue(false);
			}
		} else {
			// node.getPrefix() is a prefix of key, so add as child
			RadixTreeNode<V> n = new RadixTreeNode<V>(key.substring(largestPrefix), value);
			node.getChildren().add(n);
		}
		
		return ret;
	}

	@Override
	public V remove(Object key) {
		if(key == null)
			throw new NullPointerException("key cannot be null");
		
		if(!(key instanceof String))
			throw new ClassCastException("keys must be String instances");

		// Special case for removing empty string (root node)
		if(key.equals("")) {
			V value = root.getValue();
			root.setHasValue(false);
			return value;
		}
		
		return remove(key.toString(), root);
	}
	
	/**
	 * Remove the value with the given key from the subtree rooted at the
	 * given node.
	 * 
	 * @param key  the key
	 * @param node  the node to start searching from
	 * 
	 * @return the value associated with the given key, or <code>null</code>
	 *         if there was no mapping for <code>key</code> 
	 */
	private V remove(String key, RadixTreeNode<V> node) {
		V ret = null;
		Iterator<RadixTreeNode<V>> iter = node.getChildren().iterator();
		while(iter.hasNext()) {
			RadixTreeNode<V> child = iter.next();

			int largestPrefix = matchingPrefixCharacters(key, child.getPrefix());
			if(largestPrefix == child.getPrefix().length()
			   && largestPrefix == key.length())
			{
				// Found our match, remove the value from this node
				if(child.getChildren().size() == 0) {
					// leaf node, simply remove from parent
					ret = child.getValue();
					iter.remove();
					break;
				} else if(child.hasValue()) {
					// internal node
					ret = child.getValue();
					child.setHasValue(false);
					
					if(child.getChildren().size() == 1) {
						// merge is necessary
						RadixTreeNode<V> subchild = child.getChildren().iterator().next();
						child.setValue(subchild.getValue());
						child.setHasValue(subchild.hasValue());
						child.setPrefix(child.getPrefix() + subchild.getPrefix());
						child.getChildren().clear();
					}
					
					break;
				}
			} else if(largestPrefix > 0 && largestPrefix < key.length()) {
				// Continue down subtree of child
				ret = remove(key.substring(largestPrefix), child);
				break;
			}
		}
		
		return ret;
	}
	
	/**
	 * Finds the length of the largest prefix for two given strings.
	 *  
	 * @return the length of largest string <code>X</code> such that both
	 *         <code>a.startsWith(X) == true</code> and
	 *         <code>b.startsWith(X) == true</code>
	 */
	private int matchingPrefixCharacters(String a, String b) {
		int len = 0;
		for(int i = 0; i < Math.min(a.length(), b.length()); ++i) {
			if(a.charAt(i) != b.charAt(i))
				break;
			++len;
		}
		return len;
	}
	
	/**
	 * Print the tree to System.out.
	 */
	public void dumpTree() {
		dumpTree(root, "");
	}
	
	/**
	 * Print the subtree to System.out.
	 * 
	 * @param node  the subtree
	 * @param outputPrefix  prefix to be attached to output
	 */
	public void dumpTree(RadixTreeNode<V> node, String outputPrefix) {
		if(node.hasValue())
			System.out.format("%s{%s : %s}%n", outputPrefix, node.getPrefix(), node.getValue());
		else
			System.out.format("%s{%s}%n", outputPrefix, node.getPrefix(), node.getValue());
		
		for(RadixTreeNode<V> child : node)
			dumpTree(child, outputPrefix + "\t");
	}
}
