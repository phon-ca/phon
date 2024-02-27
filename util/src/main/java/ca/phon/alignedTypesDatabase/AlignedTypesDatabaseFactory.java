package ca.phon.alignedTypesDatabase;

/**
 * Create instances of {@link AlignedTypesDatabase}
 */
public final class AlignedTypesDatabaseFactory {

	/**
	 * Create a new database
	 *
	 * @return new database
	 */
	public static AlignedTypesDatabase newDatabase() {
		return new AlignedTypesDatabase(new AlignedTypesDatabaseTSTImpl());
	}

	/**
	 * Create a new database
	 *
	 * @return new database
	 */
	public AlignedTypesDatabase createDatabase() {
		return new AlignedTypesDatabase(new AlignedTypesDatabaseTSTImpl());
	}

}
