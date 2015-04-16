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

import java.text.ParseException;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;

/**
 * IPA transcript formatter
 */
@FormatterType(IPATranscript.class)
public class IPATranscriptFormatter implements Formatter<IPATranscript> {

	@Override
	public String format(IPATranscript obj) {
		return obj.toString();
	}

	@Override
	public IPATranscript parse(String text) throws ParseException {
		return IPATranscript.parseIPATranscript(text);
	}
	
}
