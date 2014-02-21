package ca.phon.ipa;



/**
 * A factory for creating various types of {@link IPAElement}
 * objects.
 * 
 */
public class IPAElementFactory {

	/* Basic phones */
	/**
	 * New basic phone
	 */
	public Phone createPhone() {
		return new Phone();
	}
	
	/**
	 * Create a new basic phone 
	 * 
	 * @param basePhone
	 * @return the created {@link IPAElement} object
	 */
	public Phone createPhone(Character basePhone) {
		return new Phone(basePhone);
	}
	
	/**
	 * Create a new basic phone
	 * 
	 * @param prefix
	 * @param basePhone
	 */
	public Phone createPhone(Character prefix, Character basePhone) {
		return new Phone(prefix, basePhone, new Character[0], 0, null, new Character[0]);
	}
	
	/**
	 * Create a new basic phone
	 * 
	 * @param prefix
	 * @param basePhone
	 * @param suffix
	 */
	public Phone createPhone(Character prefix, Character basePhone, Character suffix) {
		return new Phone(prefix, basePhone, new Character[0], 0, suffix, new Character[0]);
	}
	
	/**
	 * Create a new basic phone
	 * 
	 * @param prefix
	 * @param basePhone
	 * @param combining
	 * @param suffix
	 */
	public Phone createPhone(Character prefix, Character basePhone, Character[] combining, Character suffix) {
		return new Phone(prefix, basePhone, combining, 0, suffix, new Character[0]);
	}
	
	/**
	 * Create a new basic phone
	 * 
	 * @param prefix
	 * @param basePhone
	 * @param combining
	 * @param length
	 * @param suffix
	 */
	public Phone createPhone(Character prefix, Character basePhone, Character[] combining, float length, Character suffix) {
		return new Phone(prefix, basePhone, combining, length, suffix, new Character[0]);
	}
	
	/**
	 * Create a new basic phone
	 * 
	 * @param basePhone
	 * @param combining
	 * @param length
	 */
	public Phone createPhone(Character basePhone, Character[] combining, float length) {
		Phone retVal = new Phone(basePhone);
		retVal.setCombiningDiacritics(combining);
		retVal.setLength(length);
		return retVal;
	}
	
	/**
	 * Create an empty compound phone
	 */
	public CompoundPhone createCompoundPhone() {
		return new CompoundPhone();
	}
	
	/**
	 * Create a new compound phone
	 * 
	 * @param phone1
	 * @param phone2
	 * @param ligature
	 */
	public CompoundPhone createCompoundPhone(Phone phone1, Phone phone2, Character ligature) {
		return new CompoundPhone(phone1, phone2, ligature);
	}
	
	/**
	 * Create a 'hard' syllable boundary. I.e., a '.'
	 * 
	 * @return a syllable boundary marker
	 */
	public SyllableBoundary createSyllableBoundary() {
		return  new SyllableBoundary();
	}
	
	/**
	 * Create a new stress marker
	 * 
	 * @param type
	 * @return a new stress marker of the given
	 *  type
	 */
	public StressMarker createStress(StressType type) {
		return new StressMarker(type);
	}
	
	/**
	 * Create a primary stress marker
	 * 
	 * @return a new primary stress marker
	 */
	public StressMarker createPrimaryStress() {
		return new StressMarker(StressType.PRIMARY);
	}
	
	/**
	 * Create a secondary stress marker
	 * 
	 * @return a new secondary stress marker
	 */
	public StressMarker createSecondaryStress() {
		return new StressMarker(StressType.SECONDARY);
	}
	
	/**
	 * Create a new intonation group
	 * 
	 * @param type
	 * @return a new intonation group marker
	 */
	public IntonationGroup createIntonationGroup(IntonationGroupType type) {
		return new IntonationGroup(type);
	}
	
	/**
	 * Create a major intonation group marker
	 * 
	 * @return a new major intonation group marker
	 */
	public IntonationGroup createMajorIntonationGroup() {
		return new IntonationGroup(IntonationGroupType.MAJOR);
	}
	
	/**
	 * Create a minor intonation group marker
	 * 
	 * @return a new minor intonation group marker
	 */
	public IntonationGroup createMinorIntonationGroup() {
		return new IntonationGroup(IntonationGroupType.MINOR);
	}
	
	/**
	 * Create a new word boundary. I.e., a ' '
	 * 
	 * @return a new word boundary marker
	 */
	public WordBoundary createWordBoundary() {
		return new WordBoundary();
	}
	
	/**
	 * Create a pause
	 * 
	 * @param length
	 * @return a new pause of the given length
	 */
	public Pause createPause(PauseLength length) {
		return new Pause(length);
	}
	
	/**
	 * Create a new diacritic
	 * 
	 * @param diacritic
	 */
	public Diacritic createDiacritic(Character diacritic) {
		return new Diacritic(diacritic);
	}
	
	/**
	 * Create a compound word marker.
	 * 
	 * @return compound word marker
	 */
	public CompoundWordMarker createCompoundWordMarker() {
		return new CompoundWordMarker();
	}
	
	/**
	 * Create a linker
	 * 
	 * @return linker (sandhi)
	 */
	public Linker createLinker() {
		return new Linker();
	}
	
	/**
	 * Create a contraction
	 * 
	 * @return contraction (sandhi)
	 */
	public Contraction createContraction() {
		return new Contraction();
	}
}
