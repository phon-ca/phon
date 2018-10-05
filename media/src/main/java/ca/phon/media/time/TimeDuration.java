/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.media.time;

public class TimeDuration {
	
	/** The time */
	private int time;
	/** The duration */
	private int duration;
	
	public TimeDuration(int time, int duration) {
		super();
		
		this.time = time;
		this.duration = duration;
	}

	/** Constructor */
	public TimeDuration() {
		super();
		
		this.time = 0;
		this.duration = 0;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		return "T: " + time + " D: " + duration;
	}

}
