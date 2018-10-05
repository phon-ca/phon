/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.session.io;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation providing runtime support of Session IO
 * version information.
 *
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionIO {
	
	/**
	 * Group name (e.g., 'ca.phon')
	 * @return
	 */
	public String group() default "";
	
	/**
	 * id
	 * @return 
	 */
	public String id();
	
	/**
	 * Version
	 * @return
	 */
	public String version();
	
	/**
	 * name
	 */
	public String name() default "";
	
	/**
	 * mimetype
	 * 
	 */
	public String mimetype();
	
	/**
	 * extension
	 */
	public String extension();
}
