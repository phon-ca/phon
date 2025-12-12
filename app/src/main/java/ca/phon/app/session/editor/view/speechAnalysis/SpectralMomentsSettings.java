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

import ca.hedlund.jpraat.binding.fon.kSound_windowShape;
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

public class SpectralMomentsSettings {
	
	private final kSound_windowShape DEFAULT_WINDOW_SHAPE = kSound_windowShape.KAISER_2;
	private final static String WINDOW_SHAPE_PROP = SpectralMomentsSettings.class.getName() + ".windowShape";
	private kSound_windowShape windowShape = getDefaultWindowShape();
	
	public kSound_windowShape getDefaultWindowShape() {
		return kSound_windowShape.valueOf(
				PrefHelper.get(WINDOW_SHAPE_PROP, DEFAULT_WINDOW_SHAPE.toString()));
	}
	
	public kSound_windowShape getWindowShape() {
		return this.windowShape;
	}
	
	public void setWindowShape(kSound_windowShape windowShape) {
		this.windowShape = windowShape;
	}
	
	private final static double DEFAULT_FILTER_START = 500;
	private final static String FILTER_START_PROP = SpectralMomentsSettings.class.getName() + ".filterStart";
	private double filterStart = getDefaultFilterStart();
	
	public double getDefaultFilterStart() {
		return PrefHelper.getDouble(FILTER_START_PROP, DEFAULT_FILTER_START);
	}
	
	public double getFilterStart() {
		return this.filterStart;
	}
	
	public void setFilterStart(double filterStart) {
		this.filterStart = filterStart;
	}
	
	private final static double DEFAULT_FILTER_END = 15000;
	private final static String FILTER_END_PROP = SpectralMomentsSettings.class.getName() + ".filterEnd";
	private double filterEnd = getDefaultFilterEnd();
	
	public double getDefaultFilterEnd() {
		return PrefHelper.getDouble(FILTER_END_PROP, DEFAULT_FILTER_END);
	}
	
	public double getFilterEnd() {
		return this.filterEnd;
	}
	
	public void setFilterEnd(double filterEnd) {
		this.filterEnd = filterEnd;
	}
	
	private final static double DEFAULT_FILTER_SMOOTHING = 100;
	private final static String FILTER_SMOOTHING_PROP = SpectralMomentsSettings.class.getName() + ".filterSmoothing";
	private double filterSmoothing = getDefaultFilterSmoothing();
	
	public double getDefaultFilterSmoothing() {
		return PrefHelper.getDouble(FILTER_SMOOTHING_PROP, DEFAULT_FILTER_SMOOTHING);
	}
	
	public double getFilterSmoothing() {
		return this.filterSmoothing;
	}
	
	public void setFilterSmoothing(double filterSmoothing) {
		this.filterSmoothing = filterSmoothing;
	}
	
	private final boolean DEFAULT_USE_PREEMPHASIS = true;
	private final static String USE_PREEMPHASIS_PROP = SpectralMomentsSettings.class.getName() + ".usePreemphasis";
	private boolean usePreemphasis = getDefaultUsePreemphasis();
	
	public boolean getDefaultUsePreemphasis() {
		return PrefHelper.getBoolean(USE_PREEMPHASIS_PROP, DEFAULT_USE_PREEMPHASIS);
	}
	
	public boolean isUsePreemphasis() {
		return this.usePreemphasis;
	}
	
	public void setUsePreemphasis(boolean usePreemphasis) {
		this.usePreemphasis = usePreemphasis;
	}
	
	private final static double DEFAULT_PREEMP_FROM = 2000;
	private final static String PREEMP_FROM_PROP = SpectralMomentsSettings.class.getName() + ".preempFrom";
	private double preempFrom = getDefaultPreempFrom();
	
	public double getDefaultPreempFrom() {
		return PrefHelper.getDouble(PREEMP_FROM_PROP, DEFAULT_PREEMP_FROM);
	}
	
	public double getPreempFrom() {
		return this.preempFrom;
	}
	
	public void setPreempFrom(double preempFrom) {
		this.preempFrom = preempFrom;
	}
	
	public void saveToPreferences(Preferences prefs) {
		prefs.put(WINDOW_SHAPE_PROP, getWindowShape().toString());
		prefs.putDouble(FILTER_START_PROP, getFilterStart());
		prefs.putDouble(FILTER_END_PROP, getFilterEnd());
		prefs.putDouble(FILTER_SMOOTHING_PROP, getFilterSmoothing());
		prefs.putBoolean(USE_PREEMPHASIS_PROP, isUsePreemphasis());
		prefs.putDouble(PREEMP_FROM_PROP, getPreempFrom());
	}
	
	public void saveAsDefaults() {
		final Preferences prefs = PrefHelper.getUserPreferences();
		saveToPreferences(prefs);
	}

	public void loadDefaults() {
		setWindowShape(getDefaultWindowShape());
		setFilterStart(getDefaultFilterStart());
		setFilterEnd(getDefaultFilterEnd());
		setFilterSmoothing(getDefaultFilterSmoothing());
		setUsePreemphasis(getDefaultUsePreemphasis());
		setPreempFrom(getDefaultPreempFrom());
	}
	
	public void loadStandards() {
		setWindowShape(DEFAULT_WINDOW_SHAPE);
		setFilterStart(DEFAULT_FILTER_START);
		setFilterEnd(DEFAULT_FILTER_END);
		setFilterSmoothing(DEFAULT_FILTER_SMOOTHING);
		setUsePreemphasis(DEFAULT_USE_PREEMPHASIS);
		setPreempFrom(DEFAULT_PREEMP_FROM);
	}

	public JSONObject toJSON() {
		final JSONObject retVal = new JSONObject();
		retVal.put("windowShape", getWindowShape().toString());
		retVal.put("filterStart", getFilterStart());
		retVal.put("filterEnd", getFilterEnd());
		retVal.put("filterSmoothing", getFilterSmoothing());
		retVal.put("usePreemphasis", isUsePreemphasis());
		retVal.put("preempFrom", getPreempFrom());
		return retVal;
	}

	public static void saveForSessionParticipant(Project project, Session session, Participant participant, SpectralMomentsSettings settings) {
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
					participantJSON.put("spectralMomentsSettings", settings.toJSON());
					return retVal;
				});
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
	}

	public static SpectralMomentsSettings getSettingsForSessionParticipant(Project project, Session session, Participant participant) {
		final SpectralMomentsSettings retVal = new SpectralMomentsSettings();
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
						if (participantJSON.has("spectralMomentsSettings")) {
							final JSONObject spectralMomentsSettingsJSON = participantJSON.getJSONObject("spectralMomentsSettings");
							retVal.setWindowShape(kSound_windowShape.valueOf(spectralMomentsSettingsJSON.optString("windowShape", retVal.getDefaultWindowShape().toString())));
							retVal.setFilterStart(spectralMomentsSettingsJSON.optDouble("filterStart", retVal.getDefaultFilterStart()));
							retVal.setFilterEnd(spectralMomentsSettingsJSON.optDouble("filterEnd", retVal.getDefaultFilterEnd()));
							retVal.setFilterSmoothing(spectralMomentsSettingsJSON.optDouble("filterSmoothing", retVal.getDefaultFilterSmoothing()));
							retVal.setUsePreemphasis(spectralMomentsSettingsJSON.optBoolean("usePreemphasis", retVal.getDefaultUsePreemphasis()));
							retVal.setPreempFrom(spectralMomentsSettingsJSON.optDouble("preempFrom", retVal.getDefaultPreempFrom()));
						}
					}
				}
			}
		}
		return retVal;
	}

}
