/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.query.script;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.util.Range;
import ca.phon.util.StringUtils;

/**
 * A range as returned by the *Searchable implementing classes.
 * To make scripting as easy as possible, standard <CODE>Range<CODE>s
 * are encapsulated as SRanges so that tier and group refs are 
 * self-contained.
 */
public class SRange implements ResultValue, PlainTextSearchable, RegexSearchable {
	
	/** The utterance */
	protected Record record;
	
	/** The range */
	protected Range range;
	
	/** The record index */
	protected int recordIndex;
	
	/** The tier name */
	protected String tierName;
	
	/** The group index (-1 if not in a word-aligned tier) */
	protected int gIndex;
	
	/** 
	 * Constructor - group-aligned range
	 * 
	 * @param record
	 * @param range
	 * @param recordIndex
	 * @param tier
	 * @param gIndex
	 */
	public SRange(Record record, Range r, int recordIndex, String tier, int gIndex) {
		super();
		
		this.record = record;
		this.range = r;
		this.recordIndex = recordIndex;
		this.tierName = tier;
		this.gIndex = gIndex;
	}

	/** 
	 * Constructor - flat-tier range
	 * 
	 * @param r
	 * @param uttIndex
	 * @param tier
	 * @param gIndex
	 */
	public SRange(Record record, Range r, int recordIndex, String tier) {
		super();
		
		this.record = record;
		this.range = r;
		this.recordIndex = recordIndex;
		this.tierName = tier;
		this.gIndex = -1;
	}
	
	/* (non-Javadoc)
	 * @see ca.phon.modules.mosearch.script.Searchable#getDataRange()
	 */
	@Override
	public Range getDataRange() {
		return range;
	}

	/* (non-Javadoc)
	 * @see ca.phon.modules.mosearch.script.Searchable#getGroupIndex()
	 */
	@Override
	public int getGroupIndex() {
		return gIndex;
	}

	/* (non-Javadoc)
	 * @see ca.phon.modules.mosearch.script.Searchable#getRecordIndex()
	 */
	@Override
	public int getRecordIndex() {
		return recordIndex;
	}

	/* (non-Javadoc)
	 * @see ca.phon.modules.mosearch.script.Searchable#getTier()
	 */
	@Override
	public String getTier() {
		return tierName;
	}
	
	public String getData() {
		String retVal  = null;
		
		if(gIndex < 0) {
			// regular tier
//			retVal = TranscriptUtils.getTierValue(utt, tierName);
			final Tier<String> depTier = record.getTier(getTier(), String.class);
			retVal = depTier.toString();
		} else {
			// group aligned tier
			final Group group = record.getGroup(gIndex);
			retVal = group.getTier(tierName, String.class);
		}
		
		return (retVal != null ? retVal : "");
	}

	@Override
	public boolean containsPlain(String txt) {
		return containsPlain(txt, true);
	}

	@Override
	public boolean containsPlain(String txt, boolean caseSensitive) {
		String data = (caseSensitive ? toString() : toString().toLowerCase());
		String query = (caseSensitive ? txt : txt.toLowerCase());
		return data.contains(query);
	}

	@Override
	public SRange[] findPlain(String txt) {
		return findPlain(txt, true);
	}

	@Override
	public SRange[] findPlain(String txt, boolean caseSensitive) {
		List<SRange> retVal = new ArrayList<SRange>();
		
		String data = (caseSensitive ? toString() : toString().toLowerCase());
		String query = (caseSensitive ? txt : txt.toLowerCase());
		
		int sIndex = 0;
		while((sIndex = data.indexOf(query, sIndex)) != -1) {
			int eIndex = sIndex + txt.length();
			Range r = new Range(sIndex+range.getStart(), eIndex+range.getStart(), true);
			sIndex = eIndex;
			
			retVal.add(new SRange(record, r, recordIndex, tierName, gIndex));
		}
		
		return retVal.toArray(new SRange[0]);
	}

	@Override
	public boolean matchesPlain(String txt) {
		return matchesPlain(txt, true);
	}

	@Override
	public boolean matchesPlain(String txt, boolean caseSensitive) {
		String data = (caseSensitive ? toString() : toString().toLowerCase());
		String query = (caseSensitive ? txt : txt.toLowerCase());
		return data.matches(query);
	}

	@Override
	public boolean containsRegex(String txt) {
		return containsRegex(txt, true);
	}

	@Override
	public boolean containsRegex(String txt, boolean caseSensitive) {
		int flags = (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		Pattern p = Pattern.compile(txt, flags);
		Matcher m = p.matcher(toString());
		
		return m.find();
	}

	@Override
	public SRange[] findRegex(String txt) {
		return findRegex(txt, true);
	}

	@Override
	public SRange[] findRegex(String txt, boolean caseSensitive) {
		List<SRange> retVal = new ArrayList<SRange>();
		
		int flags = (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		Pattern p = Pattern.compile(txt, flags);
		Matcher m = p.matcher(toString());
		
		for(int sIndex = 0; sIndex < toString().length(); sIndex++) {
			if(m.find(sIndex)) {
				int si = m.start() + range.getStart();
				int ei = m.end() + range.getStart();
				
				sIndex = ei;
				
				Range r = new Range(si, ei, true);
				retVal.add(new SRange(record, r, recordIndex, tierName, gIndex));
			}
		}
		
		return retVal.toArray(new SRange[0]);
	}
	
	public int getNumberOfCharacters() {
		int retVal = 0;
		
		String data = getData();
		if(data != null)
			retVal = data.length();
		
		return retVal;
	}
	
	public SRange getCharacter(int pos) {
		// create a range of just the character
		Range newRange = new Range(
				range.getStart()+pos, range.getStart()+pos+1, true);
		return new SRange(record, newRange, recordIndex, tierName, gIndex);
	}
	
	public SRange getCharacterRange(int start, int end) {
		// create a new range
		int rS = range.getStart() + start;
		int rE = range.getStart() + end;
		Range newRange = new Range(rS, rE);
		return new SRange(record, newRange, recordIndex, tierName, gIndex);
	}

	@Override
	public boolean matchesRegex(String txt) {
		return matchesRegex(txt, true);
	}

	@Override
	public boolean matchesRegex(String txt, boolean caseSensitive) {
		int flags = (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		Pattern p = Pattern.compile(txt, flags);
		Matcher m = p.matcher(getData());
		
		return m.matches();
	}
	
	@Override
	public String toString() {
		return StringUtils.substringFromRange(getData(), range);
	}
}
