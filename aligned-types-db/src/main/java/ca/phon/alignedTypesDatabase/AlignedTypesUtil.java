package ca.phon.alignedTypesDatabase;

import ca.phon.util.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class AlignedTypesUtil {

	public static Tuple<String[], String[]> alignedTypesToArrays(Map<String, String> alignedTypes) {
		final List<Tuple<String, String>> alignedInfo =
				alignedTypes.entrySet().stream()
						.map(e -> new Tuple<String, String>(e.getKey(), e.getValue()))
						.collect(Collectors.toList());
		final String[] tierNames = alignedInfo.stream().map(Tuple::getObj1).collect(Collectors.toList()).toArray(new String[0]);
		final String[] types = alignedInfo.stream().map(Tuple::getObj2).collect(Collectors.toList()).toArray(new String[0]);
		return new Tuple<>(tierNames, types);
	}

}
