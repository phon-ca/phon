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

import ca.phon.media.TimeComponentUI;
import ca.phon.session.Record;
import ca.phon.session.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class RecordGridUI extends TimeComponentUI {

	public abstract Rectangle2D getSegmentRect(Record record);
	
	public abstract Rectangle2D getSpeakerTierRect(Participant participant);
	
	public abstract Participant getSpeakerAtPoint(Point pt);
	
	public abstract void repaintOverlappingRecords(Record r);
	
	public abstract void repaintOverlappingRecords(Rectangle2D segRect);
	
}
