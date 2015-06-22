package ca.phon.app.query.opgraph;

import java.util.Collection;
import java.util.Iterator;

import ca.gedge.opgraph.validators.TypeValidator;
import ca.phon.query.db.ResultSet;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;

public class RecordContainerTypeValidator implements TypeValidator {

	@Override
	public boolean isAcceptable(Object arg0) {
		final Class<?> clazz = arg0.getClass();
		
		boolean allowed = false;
		// allow single sessions or result sets
		if(clazz == Session.class || clazz == ResultSet.class
				|| clazz == Session[].class || clazz == ResultSet[].class
				|| clazz == SessionPath.class || clazz == SessionPath[].class) {
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
		if(clazz == Session.class || clazz == ResultSet.class
				|| clazz == Session[].class || clazz == ResultSet[].class
				|| clazz == SessionPath.class || clazz == SessionPath[].class
				|| Collection.class.isAssignableFrom(clazz)) {
			retVal = true;
		}
		return retVal;
	}
	
}
