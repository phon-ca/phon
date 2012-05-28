/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.ui.action;

import java.awt.event.ActionEvent;

/**
 * Wrapper for ActionEvents.  May also
 * contain extra data given by the registering object.
 */
public class PhonActionEvent {

	/** Wrapped action event */
	private ActionEvent ae;

	/** Extra data */
	private Object data;

	public PhonActionEvent(ActionEvent ae) {
		this(ae, null);
	}

	public PhonActionEvent(ActionEvent ae, Object data) {
		this.ae = ae;
		this.data = data;
	}

	public ActionEvent getActionEvent() {
		return ae;
	}

	public void setActionEvent(ActionEvent ae) {
		this.ae = ae;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
