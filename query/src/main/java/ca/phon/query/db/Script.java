/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

package ca.phon.query.db;

import java.util.Map;

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
