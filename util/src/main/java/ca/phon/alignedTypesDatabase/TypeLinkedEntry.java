/*
 * Copyright (C) 2005-2022 Gregory Hedlund & Yvan Rose
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

package ca.phon.alignedTypesDatabase;

import ca.hedlund.tst.*;

import java.io.*;
import java.util.*;

final class TypeLinkedEntry implements Serializable {

	private static final long serialVersionUID = -627133273216532011L;

	private transient TernaryTreeNode<TierInfo> tierNameRef;

	private transient TernaryTreeNodePath tierNamePath;

	private transient Map<TernaryTreeNode<Collection<TypeEntry>>, Integer> linkedTierCounts;

	private transient Map<TernaryTreeNodePath, Integer> linkedNodePaths;

	public TypeLinkedEntry(TernaryTreeNode<TierInfo> tierNameRef) {
		this(tierNameRef, new LinkedHashMap<>());
	}

	public TypeLinkedEntry(TernaryTreeNode<TierInfo> tierNameRef,
	                       Map<TernaryTreeNode<Collection<TypeEntry>>, Integer> linkedTierCounts) {
		super();

		this.tierNameRef = tierNameRef;
		this.linkedTierCounts = linkedTierCounts;
	}

	public String getTierName(TernaryTree<TierInfo> tierDescriptionTree) {
		if(this.tierNameRef == null) {
			// lazy-load after serialization
			if(this.tierNamePath != null) {
				Optional<TernaryTreeNode<TierInfo>> tierInfoOpt =
						tierDescriptionTree.findNode(this.tierNamePath);
				if(tierInfoOpt.isEmpty())
					throw new IllegalStateException("Invalid tier node path");
				this.tierNamePath = null;
				this.tierNameRef = tierInfoOpt.get();
			} else {
				throw new IllegalStateException("No tier node path");
			}
		}
		return this.tierNameRef.getPrefix();
	}

	public Map<TernaryTreeNode<Collection<TypeEntry>>, Integer> getLinkedTierCounts(TernaryTree<Collection<TypeEntry>> tree) {
		if(this.linkedTierCounts == null) {
			// lazy-load after serialization
			if(this.linkedNodePaths != null) {
				this.linkedTierCounts = new LinkedHashMap<>();
				for(var path:this.linkedNodePaths.keySet()) {
					final Optional<TernaryTreeNode<Collection<TypeEntry>>> tierNodeOpt =
							tree.findNode(path);
					if(tierNodeOpt.isEmpty())
						throw new IllegalStateException("Invalid value path");
					this.linkedTierCounts.put(tierNodeOpt.get(), this.linkedNodePaths.get(path));
				}
				this.linkedNodePaths = null;
			} else {
				throw new IllegalStateException("No linked values");
			}
		}
		return this.linkedTierCounts;
	}

	public Set<TernaryTreeNode<Collection<TypeEntry>>> getLinkedTierRefs(TernaryTree<Collection<TypeEntry>> tree) {
		return getLinkedTierCounts(tree).keySet();
	}

	public int getLinkedTierCount(TernaryTree<Collection<TypeEntry>> tree,
	                              TernaryTreeNode<Collection<TypeEntry>> linkedNode) {
		var linkedTierCounts= getLinkedTierCounts(tree);
		var storedVal = linkedTierCounts.get(linkedNode);
		if(storedVal != null)
			return storedVal;
		else
			return 0;
	}

	public void addLinkedTier(TernaryTree<Collection<TypeEntry>> tree,
	                          TernaryTreeNode<Collection<TypeEntry>> linkedNode) {
		var linkedTierCounts = getLinkedTierCounts(tree);
		if(!linkedTierCounts.containsKey(linkedNode)) {
			linkedTierCounts.put(linkedNode, 1);
		}
	}

	/**
	 * Increment number for linked tier node, this will add the linked node
	 * to the set if necessary
	 *
	 * @param tree
	 * @param linkedNode
	 * @return new value of link count
	 */
	public int incrementLinkedTier(TernaryTree<Collection<TypeEntry>> tree,
	                           TernaryTreeNode<Collection<TypeEntry>> linkedNode) {
		var linkedTierCounts = getLinkedTierCounts(tree);
		int newCnt = getLinkedTierCount(tree, linkedNode) + 1;
		linkedTierCounts.put(linkedNode, newCnt);
		return Math.max(newCnt, 0);
	}

	/**
	 * Decrement number for linked tier node, removed linked node if value hits zero
	 *
	 * @param tree
	 * @param linkedNode
	 * @return new value of link count
	 */
	public int decrementLinkedTier(TernaryTree<Collection<TypeEntry>> tree,
			TernaryTreeNode<Collection<TypeEntry>> linkedNode) {
		var linkedTierCounts = getLinkedTierCounts(tree);
		int newCnt = getLinkedTierCount(tree, linkedNode) - 1;
		if(newCnt > 0)
			linkedTierCounts.put(linkedNode, newCnt);
		else
			linkedTierCounts.remove(linkedNode);
		return Math.max(newCnt, 0);
	}

	private void readObject(ObjectInputStream oin) throws IOException, ClassNotFoundException {
		this.tierNameRef = null;
		this.linkedTierCounts = null;

		this.tierNamePath = (TernaryTreeNodePath) oin.readObject();
		final int numLinks = oin.readInt();
		this.linkedNodePaths = new LinkedHashMap<>();
		for(int i = 0; i < numLinks; i++) {
			TernaryTreeNodePath linkedPath = (TernaryTreeNodePath) oin.readObject();
			int cnt = (int) oin.readInt();
			this.linkedNodePaths.put(linkedPath, cnt);
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		if(this.tierNameRef != null) {
			out.writeObject(this.tierNameRef.getPath());
		} else if(this.tierNamePath != null) {
			out.writeObject(this.tierNamePath);
		} else {
			throw new IOException("No path to tier name");
		}

		if(this.linkedTierCounts != null) {
			out.writeInt(this.linkedTierCounts.size());
			for(var linkedNode:this.linkedTierCounts.keySet()) {
				out.writeObject(linkedNode.getPath());
				out.writeInt(linkedTierCounts.get(linkedNode));
			}
		} else if(this.linkedNodePaths != null) {
			out.writeInt(this.linkedNodePaths.size());
			for(var linkedPath:linkedNodePaths.keySet()) {
				out.writeObject(linkedPath);
				out.writeInt(linkedNodePaths.get(linkedPath));
			}
		} else {
			out.writeInt(0);
		}
	}

}
