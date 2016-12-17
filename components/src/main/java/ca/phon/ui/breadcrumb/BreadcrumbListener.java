/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * This file is part of the OpGraph project.
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

/**
 * An interface for classes that want to listen to state changes in
 * a {@link Breadcrumb}.
 * 
 * @param <S>  the type of state
 * @param <V>  the type of value associated with a state
 */
public interface BreadcrumbListener<S, V> {
	/**
	 * Called when the state of a breadcrumb changes.
	 * 
	 * @param oldState  the old state
	 * @param newState  the new state
	 */
	public void stateChanged(S oldState, S newState);

	/**
	 * Called when a state is added to a breadcrumb.
	 * 
	 * @param state  the state
	 * @param value  the value associated with the given state
	 */
	public void stateAdded(S state, V value);
}
