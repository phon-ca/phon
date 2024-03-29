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
package ca.phon.app.session.editor.view.timeline;

import java.awt.event.MouseEvent;

/**
 * Interface for handling various mouse events for records in the {@link RecordGrid}
 */
public interface RecordGridMouseListener {

	public void recordClicked(int recordIndex, MouseEvent me);
	
	public void recordPressed(int recordIndex, MouseEvent me);
	
	public void recordReleased(int recordIndex, MouseEvent me);
	
	public void recordEntered(int recordIndex, MouseEvent me);
	
	public void recordExited(int recordIndex, MouseEvent me);
	
	public void recordDragged(int recordIndex, MouseEvent me);
	
}
