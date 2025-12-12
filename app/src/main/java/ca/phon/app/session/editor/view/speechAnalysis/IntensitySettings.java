/*
 * Copyright (C) 2012-2018 Gregory Hedlund
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
package ca.phon.app.session.editor.view.speechAnalysis;

import ca.hedlund.jpraat.binding.fon.Intensity;
import ca.phon.app.log.LogUtil;
import ca.phon.project.Project;
import ca.phon.project.ProjectProperties;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.util.PrefHelper;
import org.json.JSONObject;

import java.io.IOException;
import java.util.prefs.Preferences;

public class IntensitySettings {

	public final static double DEFAULT_VIEW_RANGE_MIN = 50.0;
	public final static String VIEW_RANGE_MIN_PROP = IntensitySettings.class.getName() + ".viewRangeMin";
	private double viewRangeMin = getDefaultViewRangeMin();
	
	public static double getDefaultViewRangeMin() {
		return PrefHelper.getDouble(VIEW_RANGE_MIN_PROP, DEFAULT_VIEW_RANGE_MIN);
	}
	
	public double getViewRangeMin() {
		return this.viewRangeMin;
	}
	
	public void setViewRangeMin(double viewRangeMin) {
		this.viewRangeMin = viewRangeMin;
	}
	
	public final static double DEFAULT_VIEW_RANGE_MAX = 100.0;
	public final static String VIEW_RANGE_MAX_PROP = IntensitySettings.class.getName() + ".viewRangeMax";
	private double viewRangeMax = getDefaultViewRangeMax();
	
	public static double getDefaultViewRangeMax() {
		return PrefHelper.getDouble(VIEW_RANGE_MAX_PROP, DEFAULT_VIEW_RANGE_MAX);
	}
	
	public double getViewRangeMax() {
		return this.viewRangeMax;
	}
	
	public void setViewRangeMax(double viewRangeMax) {
		this.viewRangeMax = viewRangeMax;
	}
	
	public final static int DEFAULT_AVERAGING = Intensity.AVERAGING_ENERGY;
	public final static String AVERAGING_PROP = IntensitySettings.class.getName() + ".averaging";
	private int averaging = getDefaultAveraging();
	
	public static int getDefaultAveraging() {
		return PrefHelper.getInt(AVERAGING_PROP, DEFAULT_AVERAGING);
	}
	
	public void setAveraging(int averaging) {
		this.averaging = averaging;
	}
	
	public int getAveraging() {
		return this.averaging;
	}
	
	public final static boolean DEFAULT_SUBTRACT_MEAN = true;
	public final static String SUBTRACT_MEAN_PROP = IntensitySettings.class.getName() + ".subtractMean";
	private boolean subtractMean = getDefaultSubtractMean();
	
	public static boolean getDefaultSubtractMean() {
		return PrefHelper.getBoolean(SUBTRACT_MEAN_PROP, DEFAULT_SUBTRACT_MEAN);
	}
	
	public void setSubtractMean(boolean subtractMean) {
		this.subtractMean = subtractMean;
	}
	
	public boolean getSubtractMean() {
		return this.subtractMean;
	}
	
	public void saveToPreferences(Preferences prefs) {
		prefs.putDouble(VIEW_RANGE_MIN_PROP, getViewRangeMin());
		prefs.putDouble(VIEW_RANGE_MAX_PROP, getViewRangeMax());
		prefs.putInt(AVERAGING_PROP, getAveraging());
		prefs.putBoolean(SUBTRACT_MEAN_PROP, getSubtractMean());
	}
	
	public void saveAsDefaults() {
		final Preferences prefs = PrefHelper.getUserPreferences();
		saveToPreferences(prefs);
	}

	public void loadDefaults() {
		setViewRangeMin(getDefaultViewRangeMin());
		setViewRangeMax(getDefaultViewRangeMax());
		setAveraging(getDefaultAveraging());
		setSubtractMean(getDefaultSubtractMean());
	}
	
	public void loadStandards() {
		setViewRangeMin(DEFAULT_VIEW_RANGE_MIN);
		setViewRangeMax(DEFAULT_VIEW_RANGE_MAX);
		setAveraging(DEFAULT_AVERAGING);
		setSubtractMean(DEFAULT_SUBTRACT_MEAN);
	}

	public JSONObject toJSON() {
		final JSONObject retVal = new JSONObject();
		retVal.put("viewRangeMin", getViewRangeMin());
		retVal.put("viewRangeMax", getViewRangeMax());
		retVal.put("averaging", getAveraging());
		retVal.put("subtractMean", getSubtractMean());
		return retVal;
	}

	public static void saveForSessionParticipant(Project project, Session session, Participant participant, IntensitySettings settings) {
		final ProjectProperties projProps = project.getExtension(ProjectProperties.class);
		if(projProps != null) {
			try {
				projProps.modifyProjectJson(json -> {
					final JSONObject retVal = new JSONObject(json.toString());
					final SessionPath sessionPath = session.getSessionPath();
					JSONObject corpusJSON = null;
					if (retVal.has(sessionPath.getFolder())) {
						corpusJSON = retVal.getJSONObject(sessionPath.getFolder());
					} else {
						corpusJSON = new JSONObject();
						retVal.put(sessionPath.getFolder(), corpusJSON);
					}

					JSONObject sessionJSON = null;
					if (corpusJSON.has(sessionPath.getSessionName())) {
						sessionJSON = corpusJSON.getJSONObject(sessionPath.getSessionName());
					} else {
						sessionJSON = new JSONObject();
						corpusJSON.put(sessionPath.getSessionName(), sessionJSON);
					}

					JSONObject participantJSON = null;
					if (sessionJSON.has(participant.getId())) {
						participantJSON = sessionJSON.getJSONObject(participant.getId());
					} else {
						participantJSON = new JSONObject();
						sessionJSON.put(participant.getId(), participantJSON);
					}
					participantJSON.put("intensitySettings", settings.toJSON());
					return retVal;
				});
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
	}

	public static IntensitySettings getSettingsForSessionParticipant(Project project, Session session, Participant participant) {
		final IntensitySettings retVal = new IntensitySettings();
		retVal.loadDefaults();
		final ProjectProperties projProps = project.getExtension(ProjectProperties.class);
		if(projProps != null) {
			final JSONObject projectJson = projProps.getProjectJson();
			final SessionPath sessionPath = session.getSessionPath();
			if (projectJson.has(sessionPath.getFolder())) {
				final JSONObject corpusJSON = projectJson.getJSONObject(sessionPath.getFolder());
				if (corpusJSON.has(sessionPath.getSessionName())) {
					final JSONObject sessionJSON = corpusJSON.getJSONObject(sessionPath.getSessionName());
					if (sessionJSON.has(participant.getId())) {
						final JSONObject participantJSON = sessionJSON.getJSONObject(participant.getId());
						if (participantJSON.has("intensitySettings")) {
							final JSONObject intensitySettingsJSON = participantJSON.getJSONObject("intensitySettings");
							retVal.setViewRangeMin(intensitySettingsJSON.optDouble("viewRangeMin", getDefaultViewRangeMin()));
							retVal.setViewRangeMax(intensitySettingsJSON.optDouble("viewRangeMax", getDefaultViewRangeMax()));
							retVal.setAveraging(intensitySettingsJSON.optInt("averaging", getDefaultAveraging()));
							retVal.setSubtractMean(intensitySettingsJSON.optBoolean("subtractMean", getDefaultSubtractMean()));
						}
					}
				}
			}
		}
		return retVal;
	}

}
