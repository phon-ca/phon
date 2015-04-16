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
package ca.phon.ipa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Visitor for filtering a list of phones into a list of 
 * audible phones.
 * 
 */
public class AudiblePhoneVisitor extends VisitorAdapter<IPAElement> {

	private final List<IPAElement> phones = new ArrayList<IPAElement>();
	
	@Override
	public void fallbackVisit(IPAElement obj) {	
	}

	@Visits
	public void visitPhone(Phone p) {
		phones.add(p);
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone cp) {
		phones.add(cp);
	}
	
	public List<IPAElement> getPhones() {
		return Collections.unmodifiableList(this.phones);
	}
	
	public void reset() {
		this.phones.clear();
	}
	
}
