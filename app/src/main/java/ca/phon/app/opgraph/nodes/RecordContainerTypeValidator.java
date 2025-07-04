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
package ca.phon.app.opgraph.nodes;

import ca.phon.opgraph.validators.TypeValidator;
import ca.phon.query.db.ResultSet;
import ca.phon.session.*;

import java.util.*;

public class RecordContainerTypeValidator implements TypeValidator {

	@Override
	public boolean isAcceptable(Object arg0) {
		final Class<?> clazz = arg0.getClass();
		
		boolean allowed = false;
		// allow single sessions or result sets
		if(Session.class.isAssignableFrom(clazz) || ResultSet.class.isAssignableFrom(clazz)
				|| Session[].class.isAssignableFrom(clazz) || ResultSet[].class.isAssignableFrom(clazz)
				|| SessionPath.class.isAssignableFrom(clazz) || SessionPath[].class.isAssignableFrom(clazz)) {
			allowed = true;
		} else if(Collection.class.isAssignableFrom(clazz)) {
			Collection<?> collection = (Collection<?>)arg0;
			Iterator<?> itr = collection.iterator();
			// check each object
			allowed = true;
			allowed &= isAcceptable(itr.next());
		}
		
		return allowed;
	}

	@Override
	public boolean isAcceptable(Class<?> clazz) {
		boolean retVal = false;
		if(Session.class.isAssignableFrom(clazz) || ResultSet.class.isAssignableFrom(clazz)
				|| Session[].class.isAssignableFrom(clazz) || ResultSet[].class.isAssignableFrom(clazz)
				|| SessionPath.class.isAssignableFrom(clazz) || SessionPath[].class.isAssignableFrom(clazz)
				|| Collection.class.isAssignableFrom(clazz)) {
			retVal = true;
		}
		return retVal;
	}
	
}
