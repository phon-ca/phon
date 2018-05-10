package ca.phon.ipa.relations;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import ca.phon.ipa.PhoneDimension;

public class SegmentalRelationComparator implements Comparator<SegmentalRelation> {

	private List<BiFunction<SegmentalRelation, SegmentalRelation, Integer>> comparators = new ArrayList<>();
	
	public SegmentalRelationComparator() {
		super();
		
		setupComparators();
	}
	
	private void setupComparators() {
		comparators.add(this::compareDimensions);
		comparators.add(this::compareRelation);
		comparators.add(this::compareDistance);
		comparators.add(this::comparePosition1);
		comparators.add(this::comparePosition2);
	}
	
	@Override
	public int compare(SegmentalRelation o1, SegmentalRelation o2) {
		final Optional<Integer> comprasion = comparators.stream()
				.map( (c) -> c.apply(o1, o2) )
				.filter( (v) -> v != 0 )
				.findFirst();
		return (comprasion.isPresent() ? comprasion.get() : 0);
	}
	
	public int compareDistance(SegmentalRelation o1, SegmentalRelation o2) {
		return new Integer(o1.getDistance()).compareTo(o2.getDistance());
	}
	
	public int compareDimensions(SegmentalRelation o1, SegmentalRelation o2) {
		final int d1 = o1.getDimensions().stream()
				.collect(Collectors.summingInt(PhoneDimension::getWeight));
		final int d2 = o2.getDimensions().stream()
				.collect(Collectors.summingInt(PhoneDimension::getWeight));
		
		return new Integer(d2).compareTo(d1);
	}
	
	public int compareRelation(SegmentalRelation o1, SegmentalRelation o2) {
		return new Integer(o1.getRelation().ordinal()).compareTo(o2.getRelation().ordinal());
	}
	
	public int comparePosition1(SegmentalRelation o1, SegmentalRelation o2) {
		return new Integer(o1.getPosition1()).compareTo(o2.getPosition1());
	}
	
	public int comparePosition2(SegmentalRelation o1, SegmentalRelation o2) {
		return new Integer(o1.getPosition2()).compareTo(o2.getPosition2());
	}

}
