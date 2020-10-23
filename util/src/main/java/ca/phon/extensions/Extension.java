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
package ca.phon.extensions;

import java.lang.annotation.*;

/**
 * Extension annotation.  Used by {@link ExtensionSupport} objects
 * for automatic extension loading during object construction.
 * 
 * The class defined by {@link #value()} is guarnteed to be the
 * type passed into the {@link ExtensionProvider#installExtension(IExtendable)}
 * method.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Extension {
	
	public Class<? extends IExtendable> value();

}
