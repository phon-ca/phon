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
package ca.phon.app.log;

import java.beans.*;
import java.io.*;

/**
 * Fires a property change event when a new line is printed to
 * the stream.
 * 
 */
public class LinePrintStream extends PrintStream {
	
	private final PropertyChangeSupport propertySupport = 
			new PropertyChangeSupport(this);
	
	public final static String LINE_APPENDED_PROP = 
			LinePrintStream.class.getName() + ".numLines";
	
	int numLines = 0;

	public LinePrintStream(OutputStream out) {
		super(out);
	}

	@Override
	public void write(int b) {
		super.write(b);
		if(b == '\n') {
			int prevLines = numLines;
			propertySupport.firePropertyChange(LINE_APPENDED_PROP, prevLines, ++numLines);
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}
	
	public int getNumLines() {
		return this.numLines;
	}
	
}
