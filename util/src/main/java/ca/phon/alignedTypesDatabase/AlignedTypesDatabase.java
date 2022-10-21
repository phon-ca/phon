package ca.phon.alignedTypesDatabase;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 * A database of types (unique strings) along with the tiers in which they appear
 * and a list of other types to which they are linked.  This database also includes
 * a list of tiers along with ordering and visibility of those tiers. The visibility
 * parameter is used by the editor view and will not affect the return values for
 * the tierNames() or tierInfo() methods.
 *
 */
public final class AlignedTypesDatabase implements Serializable {

	private static final long serialVersionUID = -7813357866801444427L;

	private final AlignedTypesDatabaseImpl impl;

	AlignedTypesDatabase() {
		this(new AlignedTypesDatabaseTSTImpl());
	}

	AlignedTypesDatabase(AlignedTypesDatabaseImpl impl) {
		super();

		this.impl = impl;
	}

	/**
	 * Adds a user tier to the list of tiers in the database
	 * Tier data type is assumed to be TierString
	 *
	 * @param tierName
	 */
	public void addUserTier(String tierName) {
		impl.addUserTier(tierName);
	}

	/**
	 * Add aligned types to the database.  This method will add each type as a key in the database
	 * and setup tier links as necessary.
	 *
	 * @param alignedTypes a map of tierName -> types which will be added to the database
	 */
	public void addAlignedTypes(Map<String, String> alignedTypes) {
		impl.addAlignedTypes(alignedTypes);
	}

	public void addAlignedTypes(String[] tierNames, String[] types) {
		impl.addAlignedTypes(tierNames, types);
	}

	public void addAlignment(String tierName, String type, String alignedTierName, String alignedType) {
		impl.addAlignment(tierName, type, alignedTierName, alignedType);
	}

	public boolean removeAlignedTypes(Map<String, String> alignedTypes) {
		return impl.removeAlignedTypes(alignedTypes);
	}

	public boolean removeAlignedTypes(String[] tierNames, String[] types) {
		return impl.removeAlignedTypes(tierNames, types);
	}

	/**
	 * Return a set of aligned types given a tier name and type
	 * that exists for that tier.
	 *
	 * @param tierName
	 * @param type
	 * @return a map of aligned tier values for the given tier and type
	 */
	public Map<String, String[]> alignedTypesForTier(String tierName, String type) {
		return impl.alignedTypesForTier(tierName, type);
	}

	/**
	 * Return a set of aligned types given a tier name and type
	 * that exists for that tier.
	 *
	 * @param tierName
	 * @param type
	 * @param tierList
	 * @return a map of aligned tier values for the given tier and type
	 */
	public Map<String, String[]> alignedTypesForTier(String tierName, String type, List<String> tierList) {
		return impl.alignedTypesForTier(tierName, type, tierList);
	}

	public Collection<String> tierNames() {
		return impl.tierNames();
	}

	public Collection<TierInfo> getTierInfo() {
		return impl.getTierInfo();
	}

	/**
	 * Return all types for given keyTier
	 *
	 * @param keyTier
	 * @return all types which appear for the given keyTier
	 */
	public Collection<String> typesForTier(String keyTier) {
		return impl.typesForTier(keyTier);
	}

	public boolean typeExists(String type) {
		return impl.typeExists(type);
	}

	public boolean typeExistsInTier(String type, String tier) {
		return impl.typeExistsInTier(type, tier);
	}

	/**
	 * Is there a link between the two tier values
	 *
	 * @param tierName
	 * @param tierVal
	 * @param linkedTier
	 * @param linkedVal
	 */
	public boolean alignmentExists(String tierName, String tierVal, String linkedTier, String linkedVal) {
		return impl.alignmentExists(tierName, tierVal, linkedTier, linkedVal);
	}

	/**
	 * Remove the link between two tier values.  If all links for the tier are removed, the
	 * type is also removed for that tier
	 *
	 * @param tierName
	 * @param type
	 * @param alignedTierName
	 * @param alignedType
	 * @return true if link was removed
	 */
	public boolean removeAlignment(String tierName, String type, String alignedTierName, String alignedType) {
		return impl.removeAlignment(tierName, type, alignedTierName, alignedType);
	}

	public boolean hasAlignedTypes(Map<String, String> alignedTypes) {
		return impl.hasAlignedTypes(alignedTypes);
	}

	public boolean hasAlignedTypes(String tierNames[], String[] rowVals) {
		return impl.hasAlignedTypes(tierNames, rowVals);
	}

	public void addDatabaseListener(AlignedTypesDatabaseListener listener) {
		impl.addDatabaseListener(listener);
	}

	public void removeDatabaseListener(AlignedTypesDatabaseListener listener) {
		impl.removeDatabaseListener(listener);
	}

	/**
	 * Return an iterator for types in the database
	 *
	 * @return type iterator
	 */
	public Iterator<String> typeIterator() {
		return typeIterator((type) -> true);
	}

	/**
	 * Return an iterator for types in the database using given filter
	 *
	 * @param filter
	 * @return type iterator for types which pass filter
	 */
	public Iterator<String> typeIterator(Function<String, Boolean> filter) {
		return impl.typeIterator(filter);
	}

}
