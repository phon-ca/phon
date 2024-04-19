package ca.phon.alignedTypesDatabase;

import java.util.*;
import java.util.function.*;

public interface AlignedTypesDatabaseImpl {

	/**
	 * Adds a user tier to the list of tiers in the database
	 * Tier data type is assumed to be TierString
	 *
	 * @param tierName
	 */
	void addUserTier(String tierName);

	/**
	 * Add aligned types to the database.  This method will add each type as a key in the database
	 * and setup tier links as necessary.
	 *
	 * @param alignedTypes a map of tierName -> types which will be added to the database
	 */
	void addAlignedTypes(Map<String, String> alignedTypes);

	void addAlignedTypes(String[] tierNames, String[] types);

	void addAlignment(String tierName, String type, String alignedTierName, String alignedType);

	boolean removeAlignedTypes(Map<String, String> alignedTypes);

	boolean removeAlignedTypes(String[] tierNames, String[] types);

	/**
	 * Return a set of aligned types given a tier name and type
	 * that exists for that tier.
	 *
	 * @param tierName
	 * @param type
	 * @return a map of aligned tier values for the given tier and type
	 */
	Map<String, String[]> alignedTypesForTier(String tierName, String type);

	/**
	 * Return a set of aligned types given a tier name and type
	 * that exists for that tier.
	 *
	 * @param tierName
	 * @param type
	 * @param tierList
	 * @return a map of aligned tier values for the given tier and type
	 */
	Map<String, String[]> alignedTypesForTier(String tierName, String type, List<String> tierList);

	Collection<String> tierNames();

	Collection<TierInfo> getTierInfo();

	/**
	 * Return all types for given keyTier
	 *
	 * @param keyTier
	 * @return all types which appear for the given keyTier
	 */
	Collection<String> typesForTier(String keyTier);

	boolean typeExists(String type);

	boolean removeType(String type);

	boolean typeExistsInTier(String type, String tier);

	/**
	 * Is there a link between the two tier values
	 *
	 * @param tierName
	 * @param tierVal
	 * @param linkedTier
	 * @param linkedVal
	 */
	boolean alignmentExists(String tierName, String tierVal, String linkedTier, String linkedVal);

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
	boolean removeAlignment(String tierName, String type, String alignedTierName, String alignedType);

	boolean hasAlignedTypes(Map<String, String> alignedTypes);

	boolean hasAlignedTypes(String tierNames[], String[] rowVals);

	void addDatabaseListener(AlignedTypesDatabaseListener listener);

	void removeDatabaseListener(AlignedTypesDatabaseListener listener);

	TypeIterator typesWithPrefix(String prefix, boolean caseSensitive, Predicate<String> filter);

	TypeIterator typesContaining(String infix, boolean caseSensitive, Predicate<String> filter);

	TypeIterator typesWithSuffix(String suffix, boolean caseSensitive, Predicate<String> filter);

	TypeIterator typeIterator(Predicate<String> filter);

}
