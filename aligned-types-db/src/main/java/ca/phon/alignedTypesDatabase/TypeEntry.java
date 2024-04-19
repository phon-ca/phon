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

final class TypeEntry implements Serializable {

	private static final long serialVersionUID = -8095511445561636192L;

	// reference to tier name node, tier names are stored frequently
	// and storing a reference to the tree node reduces memory footprint
	private transient TernaryTreeNode<TierInfo> tierNameRef;

	// used for serialization to lazy-load linked values after tree structure is fully loaded into memory
	private transient TernaryTreeNodePath tierNameNodePath;

	// map of links to aligned tier data
	private transient List<TypeLinkedEntry> alignedTierLinkedEntries;

	public TypeEntry(TernaryTreeNode<TierInfo> tierNameRef) {
		this(tierNameRef, new ArrayList<>());
	}

	public TypeEntry(TernaryTreeNode<TierInfo> tierNameRef,
	                 List<TypeLinkedEntry> alignedTierLinkedEntries) {
		super();

		this.tierNameRef = tierNameRef;
		this.alignedTierLinkedEntries = alignedTierLinkedEntries;
	}

	public String getTierName(TernaryTree<TierInfo> tierDescriptionTree) {
		if(this.tierNameRef == null) {
			if(this.tierNameNodePath != null) {
				Optional<TernaryTreeNode<TierInfo>> tierNodeOpt =
						tierDescriptionTree.findNode(this.tierNameNodePath);
				if(tierNodeOpt.isEmpty()) {
					throw new IllegalStateException("Invalid tier name path");
				}
				this.tierNameNodePath = null;
				this.tierNameRef = tierNodeOpt.get();
			} else {
				throw new IllegalStateException("No path to tier name");
			}
		}
		return this.tierNameRef.getPrefix();
	}

	public List<TypeLinkedEntry> getLinkedEntries() {
		return this.alignedTierLinkedEntries;
	}

	public void addLinkedEntry(TypeLinkedEntry entry) {
		this.alignedTierLinkedEntries.add(entry);
	}

	@Serial
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		this.tierNameRef = null;

		this.tierNameNodePath = (TernaryTreeNodePath) ois.readObject();

		this.alignedTierLinkedEntries = new ArrayList<>();
		final int numEntries = ois.readInt();
		for(int i = 0; i < numEntries; i++) {
			this.alignedTierLinkedEntries.add((TypeLinkedEntry) ois.readObject());
		}
	}

	@Serial
	private void writeObject(ObjectOutputStream out) throws IOException {
		if(tierNameRef != null) {
			out.writeObject(tierNameRef.getPath());
		} else if(tierNameNodePath != null) {
			out.writeObject(tierNameNodePath);
		} else {
			throw new IOException("No tree path to tier name");
		}

		if(this.alignedTierLinkedEntries != null) {
			out.writeInt(this.alignedTierLinkedEntries.size());
			for(var linkedEntry:this.alignedTierLinkedEntries) {
				out.writeObject(linkedEntry);
			}
		}
	}

}
