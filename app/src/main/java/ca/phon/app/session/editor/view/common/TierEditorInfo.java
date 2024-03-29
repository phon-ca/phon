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
package ca.phon.app.session.editor.view.common;

import ca.phon.plugin.IPluginExtensionPoint;

import java.lang.annotation.*;

/**
 * Annotation requried for {@link TierEditor} {@link IPluginExtensionPoint}s.
 * 
 * This annotation should exist on the extension point implementation.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TierEditorInfo {

	/**
	 * Tier type
	 */
	public Class<?> type() default String.class;
	
	/**
	 * Tier name (optional)
	 * If specified, the editor will only be used
	 * for the specified tier name.
	 */
	public String tierName() default "";
	
}
