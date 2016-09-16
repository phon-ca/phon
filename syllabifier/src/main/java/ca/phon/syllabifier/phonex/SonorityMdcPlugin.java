/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.syllabifier.phonex;

import java.util.List;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

/**
 * Provides the phonex 'mdc' (minimum distance constraint)
 * plug-in for syllabification.  Format of input should be
 * 
 *  INT(,[true|false])?
 *  
 * Where INT is the minimum distance from the previous phone
 * and the optional boolean indicates if flat sonority is
 * allowed.
 */
@PhonexPlugin(name = "mdc")
public class SonorityMdcPlugin
implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		if(args.size() != 2)
			throw new IllegalArgumentException("Invalid number of arguments, should be 2, is " + args.size());
		int dist = 0;
		boolean allowFlat = false;
		try {
			dist = Integer.parseInt(args.get(0));
			allowFlat = Boolean.parseBoolean(args.get(1));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
		
		return new SonorityDistancePhoneMatcher(dist, allowFlat);
	}

}
