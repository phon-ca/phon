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
package ca.phon.session.position;

import ca.phon.util.Range;

/**
 * A range within a transcript element. The range is defined by a tier and a range of characters within that tier.
 * If the transcript element is a record, tier will be the name of the specific tier.  If the transcript element is a comment, the
 * tier name will be the type of comment, if the transcript element is a gem, tier will be the type of gem.
 *
 * @param transcriptElementIndex
 * @param tier
 * @param range
 */
public record TranscriptElementRange(int transcriptElementIndex, String tier, Range range) {

	public TranscriptElementLocation start() {
		return new TranscriptElementLocation(transcriptElementIndex(), tier(), range().getStart());
	}
	
	public TranscriptElementLocation end() {
		return new TranscriptElementLocation(transcriptElementIndex(), tier(), range().getEnd());
	}

}