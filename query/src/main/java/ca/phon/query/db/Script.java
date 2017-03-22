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

package ca.phon.query.db;

import java.util.Map;

import ca.phon.script.PhonScript;

/**
 * An interface that defines a Phon script. A script has source code and
 * a set of parameters.
 */
public interface Script {
	/**
	 * Gets the source code for this script.
	 *
	 * @return the source code
	 */
	public abstract String getSource();

	/**
	 * Sets the source code for this script.
	 *
	 * @param source  the source code
	 */
	public abstract void setSource(String source);

	/**
	 * Set url for script.  This may be used as an alternative to setSource
	 *
	 * @param url
	 * @param library may be <code>null</code>
	 */
	public abstract void setUrl(ScriptURL scriptUrl);

	/**
	 * Get url for script.  May be <code>null</code>
	 *
	 * @return url
	 */
	public abstract ScriptURL getUrl();

	/**
	 * Gets the parameters used in this script.
	 *
	 * @return the map containing parameters (key = param name, value = param value)
	 */
	public abstract Map<String, String> getParameters();

	/**
	 * Sets the parameters used in this script.
	 *
	 * @param params  the map containing parameters (key = param name, value = param value)
	 */
	public abstract void setParameters(Map<String, String> params);

	/**
	 * Gets the MIME-type for this script.
	 *
	 * @return the MIME-type
	 */
	public abstract String getMimeType();

	/**
	 * Sets the MIME-type for this script.
	 *
	 * @param source  the MIME-type
	 */
	public abstract void setMimeType(String mimeType);

}
