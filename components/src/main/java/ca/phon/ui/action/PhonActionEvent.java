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
package ca.phon.ui.action;

import java.awt.event.ActionEvent;

/**
 * Wrapper for ActionEvents.  May also
 * contain extra data given by the registering object.
 */
public class PhonActionEvent<T> {

	/** Wrapped action event */
	private ActionEvent ae;

	/** Extra data */
	private T data;

	public PhonActionEvent(ActionEvent ae) {
		this(ae, null);
	}

	public PhonActionEvent(ActionEvent ae, T data) {
		this.ae = ae;
		this.data = data;
	}

	public ActionEvent getActionEvent() {
		return ae;
	}

	public void setActionEvent(ActionEvent ae) {
		this.ae = ae;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
