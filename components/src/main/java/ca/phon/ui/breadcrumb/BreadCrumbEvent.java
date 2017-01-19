/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui.breadcrumb;

public class BreadCrumbEvent<S, V> {
	
	public static enum BreadcrumbEventType {
		STATE_ADDED,
		GOTO_STATE
	};

	private BreadCrumb<S, V> breadCrumb;
	
	private S state;
	
	private V value;
	
	private int stateIndex;
	
	private BreadcrumbEventType eventType = BreadcrumbEventType.GOTO_STATE;
	
	public BreadCrumbEvent() {
		super();
	}

	public BreadCrumbEvent(BreadCrumb<S, V> breadcrumb, S state, V value, int stateIndex,
			BreadcrumbEventType eventType) {
		super();
		this.breadCrumb = breadcrumb;
		this.state = state;
		this.value = value;
		this.stateIndex = stateIndex;
		this.eventType = eventType;
	}

	public BreadCrumb<S, V> getBreadcrumb() {
		return breadCrumb;
	}

	public void setBreadcrumb(BreadCrumb<S, V> breadcrumb) {
		this.breadCrumb = breadcrumb;
	}

	public S getState() {
		return state;
	}

	public void setState(S state) {
		this.state = state;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public int getStateIndex() {
		return stateIndex;
	}

	public void setStateIndex(int stateIndex) {
		this.stateIndex = stateIndex;
	}

	public BreadcrumbEventType getEventType() {
		return eventType;
	}

	public void setEventType(BreadcrumbEventType eventType) {
		this.eventType = eventType;
	}
	
}
