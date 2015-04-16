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
package ca.phon.orthography;

import java.text.ParseException;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;
import ca.phon.orthography.parser.OrthoTokenSource;
import ca.phon.orthography.parser.OrthographyParser;

/**
 * Orthography formatter
 *
 */
@FormatterType(Orthography.class)
public class OrthographyFormatter implements Formatter<Orthography> {

	@Override
	public String format(Orthography obj) {
		return obj.toString();
	}

	@Override
	public Orthography parse(String text) throws ParseException {
		Orthography retVal = new Orthography();
		final OrthoTokenSource tokenSource = new OrthoTokenSource(text);
		final TokenStream tokenStream = new CommonTokenStream(tokenSource);
		final OrthographyParser parser = new OrthographyParser(tokenStream);
		try {
			retVal = parser.orthography().ortho;
		} catch (RecognitionException e) {
			throw new ParseException(text, e.charPositionInLine);
		}
		return retVal;
	}

}
