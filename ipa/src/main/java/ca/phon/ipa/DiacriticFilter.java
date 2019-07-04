/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.ipa;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Visit each element in a {@link IPATranscript} and create a new 
 * transcript with {@link Diacritic}s removed.
 *
 */
public class DiacriticFilter extends VisitorAdapter<IPAElement> {

	final private IPATranscriptBuilder builder = new IPATranscriptBuilder();
	
	final private IPAElementFactory factory = new IPAElementFactory();
	
	final private Predicate<Diacritic> diacriticFilter;
	
	public DiacriticFilter() {
		this( (d) -> false );
	}
	
	public DiacriticFilter(Predicate<Diacritic> filter) {
		super();
		this.diacriticFilter = filter;
	}
	
	public Predicate<Diacritic> getDiacriticFilter() { 
		return this.diacriticFilter;
	}
	
	public IPATranscript getIPATranscript() {
		return builder.toIPATranscript();
	}
	
	@Override
	public void fallbackVisit(IPAElement obj) {
		builder.append(factory.cloneElement(obj));
	}
	
	private boolean keepDiacritic(Diacritic d) {
		if(this.diacriticFilter != null)
			return this.diacriticFilter.test(d);
		return false;
	}
	
	@Visits
	public void visitPhone(Phone phone) {
		// retain tone diacritics
		Character base = phone.getBasePhone();
		Diacritic[] prefix = Arrays.stream(phone.getPrefixDiacritics())
				.filter( this::keepDiacritic )
				.collect(Collectors.toList())
				.toArray(new Diacritic[0]);
		Diacritic[] combining = Arrays.stream(phone.getCombiningDiacritics())
				.filter( this::keepDiacritic )
				.collect(Collectors.toList())
				.toArray(new Diacritic[0]);
		Diacritic[] suffix = Arrays.stream(phone.getSuffixDiacritics())
				.filter(this::keepDiacritic)
				.collect(Collectors.toList())
				.toArray(new Diacritic[0]);
		
		Phone p = factory.createPhone(prefix,  base, combining, suffix);
		factory.copySyllabification(phone, p);
		builder.append(p);
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone phone) {
		visit(phone.getFirstPhone());
		visit(phone.getSecondPhone());
		builder.makeCompoundPhone(phone.getLigature());
		factory.copySyllabification(phone, builder.last());
	}
	
}
