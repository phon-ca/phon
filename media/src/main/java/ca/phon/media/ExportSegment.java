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
package ca.phon.media;

import java.beans.*;
import java.io.*;

import ca.phon.audio.*;
import ca.phon.extensions.*;

/**
 * Extension interface for LongSound objects.
 * 
 * <p>E.g.,
 * <pre>
 * float startTime, endTime = ... // setup segment times
 * ExportSegment exportSeg = longSound.getExtension(ExportSegment.class);
 * if(exportSeg != null) {
 *     exportSeg.exportSegment(file, startTime, endTime);
 * }
 * </pre>
 * </p>
 * 
 */
@Extension(LongSound.class)
public abstract class ExportSegment {
	
	private File file;
	
	private boolean exporting = false;
	
	private float position = -1.0f;
	
	private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	public ExportSegment() {
		super();
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		var oldFile = this.file;
		this.file = file;
		propSupport.firePropertyChange("file", oldFile, file);
	}

	public boolean isExporting() {
		return exporting;
	}

	public void setExporting(boolean exporting) {
		var oldVal = this.exporting;
		this.exporting = exporting;
		propSupport.firePropertyChange("exporting", oldVal, exporting);
	}

	public float getPosition() {
		return position;
	}

	public void setPosition(float position) {
		var oldVal = this.position;
		this.position = position;
		propSupport.firePropertyChange("position", oldVal, position);
	}

	/**
	 * Export segment to given file.
	 * 
	 * @param file
	 * @param startTime
	 * @param endTime
	 * 
	 * @throws IOException
	 */
	public abstract void exportSegment(File file, float startTime, float endTime) throws IOException;
	
	public abstract AudioFileType getFileType();
	
	public abstract AudioFileEncoding getEncoding();
		
}
