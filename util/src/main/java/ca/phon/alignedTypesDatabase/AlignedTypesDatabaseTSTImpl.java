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
 *
 */
package ca.phon.alignedTypesDatabase;

import ca.hedlund.tst.*;
import ca.phon.util.Tuple;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link AlignedTypesDatabase} implementation using TernaryTree as a backend.
 *
 */
public final class AlignedTypesDatabaseTSTImpl implements Serializable, AlignedTypesDatabaseImpl {

	private static final long serialVersionUID = -4436233595101310518L;

	private transient ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private TernaryTree<TierInfo> tierDescriptionTree;

	private TernaryTree<Collection<TypeEntry>> tree;

	AlignedTypesDatabaseTSTImpl() {
		super();

		tierDescriptionTree = new TernaryTree<>();
		tree = new TernaryTree<>();
	}

	/**
	 * Adds a user tier to the list of tiers in the database
	 * Tier data type is assumed to be TierString
	 *
	 * @param tierName
	 */
	@Override
	public void addUserTier(String tierName) {
		ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

		try {
			writeLock.lock();

			if (!tierDescriptionTree.containsKey(tierName)) {
				final TierInfo userTierInfo = new TierInfo(tierName);
				userTierInfo.setOrder(tierDescriptionTree.size());
				tierDescriptionTree.put(tierName, userTierInfo);
			}
		} finally {
			writeLock.unlock();
		}

		fireDatabaseEvent(new AlignedTypesDatabaseEvent(AlignedTypesDatabaseEvent.EventType.TierAdded, tierName));
	}

	/**
	 * Add type for specified tier
	 *
	 *
	 *
	 * @throws IllegalStateException if unable to add tier name or type to database
	 */
	public TernaryTreeNode<Collection<TypeEntry>> addTypeForTier(String tierName, String type) {
		try {
			readWriteLock.writeLock().lock();

			// ensure tier exists
			Optional<TernaryTreeNode<TierInfo>> tierNameRefOpt = tierDescriptionTree.findNode(tierName);
			if (tierNameRefOpt.isEmpty()) {
				try {
					addUserTier(tierName);
				} catch (Exception e) {
					// do nothing, tier exists
				}
				tierNameRefOpt = tierDescriptionTree.findNode(tierName);
			}
			if (tierNameRefOpt.isEmpty())
				throw new IllegalStateException("Unable to add tier name to database");
			final TernaryTreeNode<TierInfo> tierNameRef = tierNameRefOpt.get();

			Optional<TernaryTreeNode<Collection<TypeEntry>>> typeNodeOpt = tree.findNode(type, true, true);
			if (typeNodeOpt.isPresent()) {
				final TernaryTreeNode<Collection<TypeEntry>> typeNode = typeNodeOpt.get();
				if (!typeNode.isTerminated()) {
					List<TypeEntry> entryList = new ArrayList<>();
					typeNode.setValue(entryList);
				}
				Optional<TypeEntry> entryOpt =
						typeNode.getValue().stream().filter((e) -> e.getTierName(tierDescriptionTree).equals(tierName)).findAny();
				if (entryOpt.isEmpty()) {
					TypeEntry entry = new TypeEntry(tierNameRef);
					typeNode.getValue().add(entry);

					fireDatabaseEvent(new AlignedTypesDatabaseEvent(AlignedTypesDatabaseEvent.EventType.TypeInserted,
							new Tuple<String, String>(tierName, type)));
				}
				return typeNode;
			} else {
				throw new IllegalStateException("Unable to add type to database");
			}
		} catch (IllegalStateException e) {
			throw e;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	/**
	 * Add aligned types to the database.  This method will add each type as a key in the database
	 * and setup tier links as necessary.
	 *
	 * @param alignedTypes a map of tierName -> types which will be added to the database
	 */
	@Override
	public void addAlignedTypes(Map<String, String> alignedTypes) {
		final Tuple<String[], String[]> alignedArrays = AlignedTypesUtil.alignedTypesToArrays(alignedTypes);
		final String[] tierNames = alignedArrays.getObj1();
		final String[] types = alignedArrays.getObj2();
		// don't include cycle which already exists
		if(hasAlignedTypes(tierNames, types)) {
			return;
		}

		addAlignedTypes(tierNames, types);
	}

	@Override
	public void addAlignedTypes(String[] tierNames, String[] types) {
		for(int i = 0; i < tierNames.length; i++) {
			final String tierName = tierNames[i];
			final String type = types[i];

			for(int j = 0; j < tierNames.length; j++) {
				final String alignedTierName = tierNames[j];
				final String alignedType = types[j];

				addAlignment(tierName, type, alignedTierName, alignedType);
			}
		}
	}

	@Override
	public void addAlignment(String tierName, String type, String alignedTierName, String alignedType) {
		try {
			readWriteLock.writeLock().lock();

			final TernaryTreeNode<Collection<TypeEntry>> typeNode = addTypeForTier(tierName, type);
			final TernaryTreeNode<Collection<TypeEntry>> alignedTypeNode = addTypeForTier(alignedTierName, alignedType);
			final TernaryTreeNode<TierInfo> alignedTierNameNode = tierDescriptionTree.findNode(alignedTierName).get();

			Optional<TypeEntry> typeEntryOpt =
					typeNode.getValue()
							.stream()
							.filter((e) -> e.getTierName(tierDescriptionTree).equals(tierName)).findAny();
			if (typeEntryOpt.isPresent()) {
				TypeEntry typeEntryForTier = typeEntryOpt.get();

				Optional<TypeLinkedEntry> linkedEntryOpt =
						typeEntryForTier.getLinkedEntries()
								.stream()
								.filter((e) -> e.getTierName(tierDescriptionTree).equals(alignedTierName)).findAny();
				if (linkedEntryOpt.isEmpty()) {
					TypeLinkedEntry linkedEntry = new TypeLinkedEntry(alignedTierNameNode);
					typeEntryForTier.addLinkedEntry(linkedEntry);
					linkedEntry.addLinkedTier(tree, alignedTypeNode);

					fireDatabaseEvent(new AlignedTypesDatabaseEvent(AlignedTypesDatabaseEvent.EventType.AlignmentAdded,
							new Tuple<Tuple<String, String>, Tuple<String, String>>(new Tuple<>(tierName, type), new Tuple<>(alignedTierName, alignedType))));
				} else {
					linkedEntryOpt.get().incrementLinkedTier(tree, alignedTypeNode);

					fireDatabaseEvent(new AlignedTypesDatabaseEvent(AlignedTypesDatabaseEvent.EventType.AlignmentIncremented,
							new Tuple<Tuple<String, String>, Tuple<String, String>>(new Tuple<>(tierName, type), new Tuple<>(alignedTierName, alignedType))));
				}
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public boolean removeAlignedTypes(Map<String, String> alignedTypes) {
		try {
			readWriteLock.writeLock().lock();

			final Tuple<String[], String[]> alignedArrays = AlignedTypesUtil.alignedTypesToArrays(alignedTypes);
			final String[] tierNames = alignedArrays.getObj1();
			final String[] types = alignedArrays.getObj2();
			if (!hasAlignedTypes(tierNames, types)) {
				return false;
			}

			return removeAlignedTypes(tierNames, types);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public boolean removeAlignedTypes(String[] tierNames, String[] types) {
		for(int i = 0; i < tierNames.length; i++) {
			final String tierName = tierNames[i];
			final String type = types[i];

			for(int j = 0; j < tierNames.length; j++) {
				if(i == j) continue;
				final String alignedTier = tierNames[j];
				final String alignedType = types[j];

				removeAlignment(tierName, type, alignedTier, alignedType);
			}
		}

		return true;
	}

	/**
	 * Return a set of aligned types given a tier name and type
	 * that exists for that tier.
	 *
	 * @param tierName
	 * @param type
	 *
	 * @return a map of aligned tier values for the given tier and type
	 */
	@Override
	public Map<String, String[]> alignedTypesForTier(String tierName, String type) {
		return alignedTypesForTier(tierName, type, List.of());
	}

	/**
	 * Return a set of aligned types given a tier name and type
	 * that exists for that tier.
	 *
	 * @param tierName
	 * @param type
	 * @param tierList
	 *
	 * @return a map of aligned tier values for the given tier and type
	 */
	@Override
	public Map<String, String[]> alignedTypesForTier(String tierName, String type, List<String> tierList) {
		Map<String, String[]> retVal = new LinkedHashMap<>();
		if (tierName == null || type == null) return retVal;
		try {
			readWriteLock.readLock().lock();

			Optional<TernaryTreeNode<Collection<TypeEntry>>> typeNodeRefOpt = tree.findNode(type);
			if (typeNodeRefOpt.isPresent()) {
				final TernaryTreeNode<Collection<TypeEntry>> typeNodeRef = typeNodeRefOpt.get();
				if (typeNodeRef.getValue() == null) {
					return retVal;
				}
				retVal.put(tierName, new String[]{type});
				Optional<TypeEntry> entryOpt =
						typeNodeRef.getValue().stream().filter((e) -> e.getTierName(tierDescriptionTree).equals(tierName)).findAny();
				if (entryOpt.isPresent()) {
					TypeEntry entry = entryOpt.get();
					retVal = alignedTypesForEntry(entry, tierList);
				}
			}
		} finally {
			readWriteLock.readLock().unlock();
		}

		return retVal;
	}

	private Map<String, String[]> alignedTypesForEntry(TypeEntry entry) {
		return alignedTypesForEntry(entry, List.of());
	}

	private Map<String, String[]> alignedTypesForEntry(TypeEntry entry, List<String> tierList) {
		Map<String, String[]> retVal = new LinkedHashMap<>();

		try {
			readWriteLock.readLock().lock();

			for (TypeLinkedEntry linkedEntry : entry.getLinkedEntries()) {
				String alignedTierName = linkedEntry.getTierName(tierDescriptionTree);
				boolean includeTier = tierList.size() > 0 ? tierList.contains(alignedTierName) : true;
				if (!includeTier) continue;
				var linkedTierCounts = linkedEntry.getLinkedTierCounts(tree)
						.entrySet()
						.stream()
						.filter(e -> e.getValue() != null && e.getValue() > 0)
						.collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
				String[] alignedTierVals = new String[linkedTierCounts.size()];
				int i = 0;
				for (Map.Entry<TernaryTreeNode<Collection<TypeEntry>>, Integer> alignedEntry : linkedTierCounts.entrySet()) {
					alignedTierVals[i++] = alignedEntry.getKey().getPrefix();
				}
				retVal.put(alignedTierName, alignedTierVals);
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
		return retVal;
	}

	@Override
	public Collection<String> tierNames() {
		try {
			readWriteLock.readLock().lock();

			return tierDescriptionTree.values()
					.stream().sorted(Comparator.comparingInt(TierInfo::getOrder))
					.map(TierInfo::getTierName)
					.collect(Collectors.toList());
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public Collection<TierInfo> getTierInfo() {
		try {
			readWriteLock.readLock().lock();

			return tierDescriptionTree.values()
					.stream().sorted(Comparator.comparingInt(TierInfo::getOrder))
					.collect(Collectors.toList());
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	private TypeEntry typeEntryForTier(String key, String tierName) {
		try {
			readWriteLock.readLock().lock();

			final Collection<TypeEntry> entries = typeEntries(key);
			final Optional<TypeEntry> typeTaggerEntry =
					entries.stream().filter((v) -> v.getTierName(tierDescriptionTree).equals(tierName)).findAny();
			if (typeTaggerEntry.isPresent())
				return typeTaggerEntry.get();
			else
				return null;
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	/**
	 * Return all types for given keyTier
	 *
	 * @param keyTier
	 * @return all types which appear for the given keyTier
	 */
	@Override
	public Collection<String> typesForTier(String keyTier) {
		final List<String> retVal = new ArrayList<>();
		Set<Map.Entry<String, Collection<TypeEntry>>> entrySet = tree.entrySet();
		for (Map.Entry<String, Collection<TypeEntry>> entry : entrySet) {
			Optional<TypeEntry> entryForKeyTier = entry.getValue()
					.stream()
					.filter((e) -> e.getTierName(tierDescriptionTree).equals(keyTier))
					.findAny();
			if (entryForKeyTier.isPresent()) {
				retVal.add(entry.getKey());
			}
		}
		return retVal;
	}

	private Collection<TypeEntry> typeEntries(String key) {
		try {
			readWriteLock.readLock().lock();

			final Optional<TernaryTreeNode<Collection<TypeEntry>>> node = tree.findNode(key);
			if (node.isEmpty()) return new ArrayList<>();

			return node.get().getValue();
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public boolean typeExists(String type) {
		return this.tree.containsKey(type);
	}

	@Override
	public boolean typeExistsInTier(String type, String tier) {
		try {
			readWriteLock.readLock().lock();

			Optional<TernaryTreeNode<Collection<TypeEntry>>> treeNodeOpt = this.tree.findNode(type);
			if (treeNodeOpt.isPresent()) {
				TernaryTreeNode<Collection<TypeEntry>> treeNode = treeNodeOpt.get();
				if (treeNode.getValue() != null) {
					Optional<TypeEntry> typeEntryForTier = treeNode.getValue().stream()
							.filter(e -> e.getTierName(this.tierDescriptionTree).equals(tier))
							.findAny();
					return typeEntryForTier.isPresent();
				} else {
					return false;
				}
			} else {
				return false;
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	/**
	 * Is there a link between the two tier values
	 *
	 * @param tierName
	 * @param tierVal
	 * @param linkedTier
	 * @param linkedVal
	 */
	@Override
	public boolean alignmentExists(String tierName, String tierVal, String linkedTier, String linkedVal) {
		final Optional<TernaryTreeNode<Collection<TypeEntry>>> nodeOpt = tree.findNode(tierVal);
		if(nodeOpt.isEmpty()) return false;

		final var node = nodeOpt.get();
		return alignmentExists(node, tierName, linkedTier, linkedVal);
	}

	/**
	 * Remove the link between two tier values.  If all links for the tier are removed, the
	 * type is also removed for that tier
	 *
	 * @param tierName
	 * @param type
	 * @param alignedTierName
	 * @param alignedType
	 *
	 * @return true if link was removed
	 */
	@Override
	public boolean removeAlignment(String tierName, String type, String alignedTierName, String alignedType) {
		try {
			readWriteLock.writeLock().lock();

			final Optional<TernaryTreeNode<Collection<TypeEntry>>> nodeOpt = tree.findNode(type);
			if (nodeOpt.isEmpty()) return false;

			final var node = nodeOpt.get();
			if (node.getValue() == null) return false;

			final Optional<TypeEntry> entryForTier = node.getValue()
					.stream()
					.filter((e) -> e.getTierName(tierDescriptionTree).equals(tierName))
					.findAny();
			if (entryForTier.isEmpty()) return false;

			final TypeEntry taggerEntry = entryForTier.get();
			final Optional<TypeLinkedEntry> linkedEntryOpt = taggerEntry.getLinkedEntries()
					.stream()
					.filter((e) -> e.getTierName(tierDescriptionTree).equals(alignedTierName))
					.findAny();
			if (linkedEntryOpt.isEmpty()) return false;

			final TypeLinkedEntry linkedEntry = linkedEntryOpt.get();
			final Optional<TernaryTreeNode<Collection<TypeEntry>>> linkedValOpt = linkedEntry.getLinkedTierRefs(tree)
					.stream()
					.filter((r) -> r.getPrefix().equals(alignedType))
					.findAny();
			if (linkedValOpt.isPresent()) {
				int linkCnt = linkedEntry.getLinkedTierCount(tree, linkedValOpt.get());
				if (linkCnt > 0) {
					linkCnt = linkedEntry.decrementLinkedTier(tree, linkedValOpt.get());
					if (linkCnt == 0) {
						if (linkedEntry.getLinkedTierRefs(tree).size() == 0) {
							taggerEntry.getLinkedEntries().remove(linkedEntry);

							if (taggerEntry.getLinkedEntries().size() == 0) {
								node.getValue().remove(taggerEntry);
							}

							fireDatabaseEvent(new AlignedTypesDatabaseEvent(AlignedTypesDatabaseEvent.EventType.AlignmentRemoved,
									new Tuple<Tuple<String, String>, Tuple<String, String>>(new Tuple<>(tierName, type), new Tuple<>(alignedTierName, alignedType))));
						}
					} else {
						fireDatabaseEvent(new AlignedTypesDatabaseEvent(AlignedTypesDatabaseEvent.EventType.AlignmentDecremented,
								new Tuple<Tuple<String, String>, Tuple<String, String>>(new Tuple<>(tierName, type), new Tuple<>(alignedTierName, alignedType))));
					}
					return true;
				}
			}
			return false;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	private boolean alignmentExists(TernaryTreeNode<Collection<TypeEntry>> node, String tierName, String linkedTier, String linkedVal) {
		try {
			readWriteLock.readLock().lock();

			if (node.getValue() == null) return false;
			final Optional<TypeEntry> entryForTier = node.getValue()
					.stream()
					.filter((e) -> e.getTierName(tierDescriptionTree).equals(tierName))
					.findAny();
			if (entryForTier.isEmpty()) return false;

			final TypeEntry taggerEntry = entryForTier.get();
			final Optional<TypeLinkedEntry> linkedEntryOpt = taggerEntry.getLinkedEntries()
					.stream()
					.filter((e) -> e.getTierName(tierDescriptionTree).equals(linkedTier))
					.findAny();
			if (linkedEntryOpt.isEmpty()) return false;

			final TypeLinkedEntry linkedEntry = linkedEntryOpt.get();
			final Optional<TernaryTreeNode<Collection<TypeEntry>>> linkedValOpt
					= linkedEntry.getLinkedTierRefs(tree)
					.stream()
					.filter((r) -> r.getPrefix().equals(linkedVal))
					.findAny();
			if (linkedValOpt.isPresent()) {
				final int linkCnt = linkedEntry.getLinkedTierCount(tree, linkedValOpt.get());
				return linkCnt > 0;
			} else {
				return false;
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public boolean hasAlignedTypes(Map<String, String> alignedTypes) {
		final Tuple<String[], String[]> alignedArrays = AlignedTypesUtil.alignedTypesToArrays(alignedTypes);
		final String[] tierNames = alignedArrays.getObj1();
		final String[] types = alignedArrays.getObj2();

		return hasAlignedTypes(tierNames, types);
	}

	@Override
	public boolean hasAlignedTypes(String tierNames[], String[] rowVals) {
		if(rowVals.length != tierNames.length) return false;

		try {
			readWriteLock.readLock().lock();

			boolean retVal = true;
			// only include row if all values have links between them
			for(int i = 0; i < rowVals.length-1; i++) {
				String v1 = rowVals[i];
				if(v1 == null) continue; // ignore empty tier values
				String t1 = tierNames[i];

				for(int j = i + 1; j < rowVals.length; j++) {
					String v2= rowVals[j];
					if(v2 == null) continue; // ignore empty tier values
					String t2 = tierNames[j];

					retVal &= alignmentExists(t1, v1, t2, v2);
				}
			}

			return retVal;
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	/**
	 * Import all entries from given database into this database.
	 *
	 * @param importDb
	 */
	public void importDatabase(AlignedTypesDatabase importDb) {
		// TODO rewrite after adding alignment counts
//		// add all tiers
//		for(TierInfo ti: importDb.getTierInfo()) {
//			if(!tierDescriptionTree.containsKey(ti.getTierName())) {
//				TierInfo cloneInfo = ti.clone();
//				cloneInfo.setOrder(tierDescriptionTree.size());
//				tierDescriptionTree.put(cloneInfo.getTierName(), cloneInfo);
//			}
//		}
//
//		// walk tree and add all entries
//		Set<Map.Entry<String, Collection<TypeEntry>>> entrySet = tree.entrySet();
//
//		// first add all types to tree
//		// we will need to add all keys first to ensure we have nodes for linked types
//		for(Map.Entry<String, Collection<TypeEntry>> entry:entrySet) {
//			String type = entry.getKey();
//			for(TypeEntry taggerEntry:entry.getValue()) {
//				addTypeForTier(taggerEntry.getTierName(importDb.tierDescriptionTree), type);
//			}
//		}
//
//		// now create links for aligned types
//		for(Map.Entry<String, Collection<TypeEntry>> entry:entrySet) {
//			String type = entry.getKey();
//
//			Optional<TernaryTreeNode<Collection<TypeEntry>>> keyNodeOpt = tree.findNode(type);
//			// shouldn't happen because we added it above
//			if(!keyNodeOpt.isPresent()) continue;
//
//			TernaryTreeNode<Collection<TypeEntry>> keyNode = keyNodeOpt.get();
//			for(TypeEntry importTaggerEntry:entry.getValue()) {
//				// create tagger entry for tier or use existing
//				final String tierName = importTaggerEntry.getTierName(importDb.tierDescriptionTree);
//				Optional<TypeEntry> existingEntry =
//						keyNode.getValue().stream().filter(e -> e.getTierName(tierDescriptionTree).equals(tierName)).findAny();
//				final TypeEntry taggerEntry = existingEntry.isPresent() ? existingEntry.get() :
//						new TypeEntry(tierDescriptionTree.findNode(tierName).get());
//				if(existingEntry.isEmpty())
//					keyNode.getValue().add(taggerEntry);
//
//				for(TypeLinkedEntry importTaggerLinkedEntry:importTaggerEntry.getLinkedEntries()) {
//					// create tagger linked entry for linked tier or use existing
//					String linkedTierName = importTaggerLinkedEntry.getTierName(importDb.tierDescriptionTree);
//					Optional<TypeLinkedEntry> existingLinkedEntry = taggerEntry.getLinkedEntries().stream()
//							.filter((e) -> e.getTierName(tierDescriptionTree).equals(linkedTierName))
//							.findAny();
//					TypeLinkedEntry taggerLinkedEntry = (existingLinkedEntry.isPresent()
//						? existingLinkedEntry.get()
//						: new TypeLinkedEntry(
//							tierDescriptionTree.findNode(importTaggerEntry.getTierName(importDb.tierDescriptionTree)).get()));
//					if(existingLinkedEntry.isEmpty())
//						taggerEntry.getLinkedEntries().add(taggerLinkedEntry);
//
//					for(Map.Entry<TernaryTreeNode<Collection<TypeEntry>>, Integer> importTierNodeRef:importTaggerLinkedEntry.getLinkedTierCounts(importDb.tree).entrySet()) {
//						// find the tree node for the linked type
//						Optional<TernaryTreeNode<Collection<TypeEntry>>> tierNodeRef =
//								tree.findNode(importTierNodeRef.getKey().getPrefix());
//						if(tierNodeRef.isPresent()) {
//							taggerLinkedEntry.getLinkedTierCounts(tree).put(tierNodeRef.get(), importTierNodeRef.getValue());
//						}
//					}
//				}
//			}
//		}
	}

	// events
	private transient List<AlignedTypesDatabaseListener> listenerList = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void addDatabaseListener(AlignedTypesDatabaseListener listener) {
		if (!listenerList.contains(listener))
			listenerList.add(listener);

	}

	@Override
	public void removeDatabaseListener(AlignedTypesDatabaseListener listener) {
		listenerList.remove(listener);
	}

	private void fireDatabaseEvent(AlignedTypesDatabaseEvent evt) {
		for(AlignedTypesDatabaseListener listener:listenerList) {
			listener.databaseEvent(evt);
		}
	}

	@Serial
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();

		this.readWriteLock = new ReentrantReadWriteLock();
		this.listenerList = Collections.synchronizedList(new ArrayList<>());
	}

	@Override
	public Iterator<String> typesWithPrefix(String prefix, Function<String, Boolean> filter) {
		final Optional<TernaryTreeNode<Collection<TypeEntry>>> prefixNodeOpt = tree.findNode(prefix);
		if(prefixNodeOpt.isPresent()) {
			final Function<TernaryTreeNode<Collection<TypeEntry>>, Boolean> itrFilter = (node) -> filter.apply(node.getPrefix());
			return new TypeIterator(new TerminatedNodeIterator<>(tree, prefixNodeOpt.get(), itrFilter));
		} else {
			return new Iterator<String>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public String next() {
					return null;
				}
			};
		}
	}

	@Override
	public Iterator<String> typesContaining(String infix, Function<String, Boolean> filter) {
		final Function<TernaryTreeNode<Collection<TypeEntry>>, Boolean> itrFilter = (node) -> {
			return node.getPrefix().contains(infix) && filter.apply(node.getPrefix());
		};
		return new TypeIterator(new TerminatedNodeIterator<>(tree, filter));
	}

	@Override
	public Iterator<String> typesWithSuffix(String suffix, Function<String, Boolean> filter) {
		final Function<TernaryTreeNode<Collection<TypeEntry>>, Boolean> itrFilter = (node) -> {
			return node.getPrefix().endsWith(suffix) && filter.apply(node.getPrefix());
		};
		return new TypeIterator(new TerminatedNodeIterator<>(tree, filter));
	}

	@Override
	public Iterator<String> typeIterator(Function<String, Boolean> filter) {
		final Function<TernaryTreeNode<Collection<TypeEntry>>, Boolean> itrFilter = (node) -> filter.apply(node.getPrefix());
		return new TypeIterator(new TerminatedNodeIterator<>(tree, itrFilter));
	}

	private class TypeIterator implements Iterator<String> {

		private final TerminatedNodeIterator<Collection<TypeEntry>> itr;

		public TypeIterator(TerminatedNodeIterator<Collection<TypeEntry>> itr) {
			this.itr = itr;
		}

		@Override
		public boolean hasNext() {
			return itr.hasNext();
		}

		@Override
		public String next() {
			return itr.next().getPrefix();
		}

	}

}
