package ca.phon.alignedTypesDatabase;

/**
 * Create instances of {@link AlignedTypesDatabase}
 */
public final class AlignedTypesDatabaseFactory {

	public AlignedTypesDatabase createDatabase() {
		return new AlignedTypesDatabase(new AlignedTypesDatabaseTSTImpl());
	}

}
