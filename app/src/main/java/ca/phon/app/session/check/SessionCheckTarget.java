package ca.phon.app.session.check;

import java.lang.annotation.*;

import ca.phon.session.check.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionCheckTarget {
	
	public Class<? extends SessionCheck> value();

}
