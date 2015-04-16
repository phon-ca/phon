/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.ipa;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class DiacriticFilter extends VisitorAdapter<IPAElement> {

	final IPATranscriptBuilder builder = new IPATranscriptBuilder();
	
	public IPATranscript getIPATranscript() {
		return builder.toIPATranscript();
	}
	
	@Override
	public void fallbackVisit(IPAElement obj) {
		builder.append(obj);
	}
	
	@Visits
	public void visitPhone(Phone phone) {
		builder.append(phone.getBase());
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone phone) {
		visit(phone.getFirstPhone());
		visit(phone.getSecondPhone());
		builder.makeCompoundPhone(phone.getLigature());
	}
	
}
