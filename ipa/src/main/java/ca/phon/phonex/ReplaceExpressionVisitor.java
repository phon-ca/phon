/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.phonex;

import java.util.logging.Logger;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.PhonexMatcherReference;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class ReplaceExpressionVisitor extends VisitorAdapter<IPAElement> {
	
	private static final Logger LOGGER = Logger
			.getLogger(ReplaceExpressionVisitor.class.getName());

	private final PhonexMatcher matcher;
	
	private final IPATranscriptBuilder builder;
	
	public ReplaceExpressionVisitor(PhonexMatcher matcher) {
		super();
		this.matcher = matcher;
		this.builder = new IPATranscriptBuilder();
	}
	
	public IPATranscript getTranscript() {
		return builder.toIPATranscript();
	}

	@Override
	public void fallbackVisit(IPAElement obj) {
		builder.append(obj);
	}
	
	@Visits
	public void visitPhonexMatcherReference(PhonexMatcherReference pmr) {
		int groupIndex = pmr.getGroupIndex();
		if(groupIndex < 0) {
			final String groupName = pmr.getGroupName();
			if(groupName == null) {
				LOGGER.severe("Unknown phonex matcher reference " + pmr.getText());
				return;
			}
			groupIndex = matcher.pattern().groupIndex(groupName);
		}
		builder.append(matcher.group(groupIndex));
	}
	
}
