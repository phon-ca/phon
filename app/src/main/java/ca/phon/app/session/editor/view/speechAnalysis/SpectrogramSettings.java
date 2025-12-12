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

import ca.hedlund.jpraat.binding.fon.kSound_to_Spectrogram_windowShape;
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

/**
 * Settings used for generating the Spectrogram.
 *
 */
public class SpectrogramSettings {

	public final static double DEFAULT_WINDOW_LENGTH = 0.005;
	public final static String WINDOW_LENGTH_PROP = SpectrogramSettings.class.getName() + ".windowLength";
	private double windowLength = getDefaultWindowLength();
	
	public final static double DEFAULT_MAX_FREQUENCY = 5000.0;
	public final static String MAX_FREQUENCY_PROP = SpectrogramSettings.class.getName() + ".maxFreq";
	private double maxFrequency = getDefaultMaxFrequency();
			
	
	public final static double DEFAULT_TIME_STEP = 0.002;
	public final static String TIME_STEP_PROP = SpectrogramSettings.class.getName() + ".timeStep";
	private double timeStep = getDefaultTimeStep();
			
	
	public final static double DEFAULT_FREQUENCY_STEP = 20.0;
	public final static String FREQUENCY_STEP_PROP = SpectrogramSettings.class.getName() + ".freqStep";
	private double frequencyStep = getDefaultFrequencyStep();
			
	
	public final static kSound_to_Spectrogram_windowShape DEFAULT_WINDOW_SHAPE = kSound_to_Spectrogram_windowShape.GAUSSIAN;
	public final static String WINDOW_SHAPE_PROP = SpectrogramSettings.class.getName() + ".windowShape";
	private kSound_to_Spectrogram_windowShape windowShape = getDefaultWindowShape();
			
	
	public final static double DEFAULT_PREEMPHASIS = 6.0;
	public final static String PREEMPHASIS_PROP = SpectrogramSettings.class.getName() + ".preEmphasis";
	private double preEmphasis = getDefaultPreEmphasis();
			
	
	public final static double DEFAULT_DYNAMIC_RANGE = 70.0;
	public final static String DYNAMIC_RANGE_PROP = SpectrogramSettings.class.getName() + ".dynamicRange";
	private double dynamicRange = getDefaultDynamicRange();
	
	public final static double DEFAULT_DYNAMIC_COMPRESSION = 0.0;
	public final static String DYNAMIC_COMPRESSION_PROP = SpectrogramSettings.class.getName() + ".dynamicCompression";
	private double dynamicCompression = getDefaultDynamicCompression();
	
	public static double getDefaultDynamicCompression() {
		return PrefHelper.getDouble(DYNAMIC_COMPRESSION_PROP, DEFAULT_DYNAMIC_COMPRESSION);
	}
	public double getDynamicCompression() {
		return dynamicCompression;
	}
	public void setDynamicCompression(double dynamicCompression) {
		this.dynamicCompression = dynamicCompression;
	}
	
	public static double getDefaultWindowLength() {
		return PrefHelper.getDouble(WINDOW_LENGTH_PROP, DEFAULT_WINDOW_LENGTH);
	}
	public double getWindowLength() {
		return windowLength;
	}
	public void setWindowLength(double windowLength) {
		this.windowLength = windowLength;
	}
	public double getMaxFrequency() {
		return maxFrequency;
	}
	
	public static double getDefaultMaxFrequency() {
		return PrefHelper.getDouble(MAX_FREQUENCY_PROP, DEFAULT_MAX_FREQUENCY);
	}
	public void setMaxFrequency(double maxFrequency) {
		this.maxFrequency = maxFrequency;
	}
	
	public static double getDefaultTimeStep() {
		return PrefHelper.getDouble(TIME_STEP_PROP, DEFAULT_TIME_STEP);
	}
	public double getTimeStep() {
		return timeStep;
	}
	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}
	
	public static double getDefaultFrequencyStep() {
		return PrefHelper.getDouble(FREQUENCY_STEP_PROP, DEFAULT_FREQUENCY_STEP);
	}
	public double getFrequencyStep() {
		return frequencyStep;
	}
	public void setFrequencyStep(double frequencyStep) {
		this.frequencyStep = frequencyStep;
	}
	
	public static kSound_to_Spectrogram_windowShape getDefaultWindowShape() {
		return kSound_to_Spectrogram_windowShape.values()[PrefHelper.getInt(WINDOW_SHAPE_PROP, DEFAULT_WINDOW_SHAPE.ordinal())];
	}
	public kSound_to_Spectrogram_windowShape getWindowShape() {
		return windowShape;
	}
	public void setWindowShape(kSound_to_Spectrogram_windowShape windowShape) {
		this.windowShape = windowShape;
	}
	
	public static double getDefaultPreEmphasis() {
		return PrefHelper.getDouble(PREEMPHASIS_PROP, DEFAULT_PREEMPHASIS);
	}
	public double getPreEmphasis() {
		return preEmphasis;
	}
	public void setPreEmphasis(double preEmphasis) {
		this.preEmphasis = preEmphasis;
	}
	
	public static double getDefaultDynamicRange() {
		return PrefHelper.getDouble(DYNAMIC_RANGE_PROP, DEFAULT_DYNAMIC_RANGE);
	}
	public double getDynamicRange() {
		return dynamicRange;
	}
	public void setDynamicRange(double dynamicRange) {
		this.dynamicRange = dynamicRange;
	}
	
	public void saveToPreferences(Preferences prefs) {
		prefs.putDouble(DYNAMIC_COMPRESSION_PROP, getDynamicCompression());
		prefs.putDouble(DYNAMIC_RANGE_PROP, getDynamicRange());
		prefs.putDouble(FREQUENCY_STEP_PROP, getFrequencyStep());
		prefs.putDouble(MAX_FREQUENCY_PROP, getMaxFrequency());
		prefs.putDouble(PREEMPHASIS_PROP, getPreEmphasis());
		prefs.putDouble(TIME_STEP_PROP, getTimeStep());
		prefs.putDouble(WINDOW_LENGTH_PROP, getWindowLength());
		prefs.putInt(WINDOW_SHAPE_PROP, getWindowShape().ordinal());
	}
	
	/**
	 * Save these settings as custom defaults.
	 *
	 *
	 */
	public void saveAsDefaults() {
		final Preferences prefs = PrefHelper.getUserPreferences();
		saveToPreferences(prefs);
	}

	public void loadStandards() {
		setDynamicCompression(DEFAULT_DYNAMIC_COMPRESSION);
		setDynamicRange(DEFAULT_DYNAMIC_RANGE);
		setFrequencyStep(DEFAULT_FREQUENCY_STEP);
		setMaxFrequency(DEFAULT_MAX_FREQUENCY);
		setPreEmphasis(DEFAULT_PREEMPHASIS);
		setTimeStep(DEFAULT_TIME_STEP);
		setWindowLength(DEFAULT_WINDOW_LENGTH);
		setWindowShape(DEFAULT_WINDOW_SHAPE);
	}
	
	public void loadDefaults() {
		setDynamicCompression(getDefaultDynamicCompression());
		setDynamicRange(getDefaultDynamicRange());
		setFrequencyStep(getDefaultFrequencyStep());
		setMaxFrequency(getDefaultMaxFrequency());
		setPreEmphasis(getDefaultPreEmphasis());
		setTimeStep(getDefaultTimeStep());
		setWindowLength(getDefaultWindowLength());
		setWindowShape(getDefaultWindowShape());
	}

	public JSONObject toJSON() {
		final JSONObject retVal = new JSONObject();
		retVal.put("windowLength", getWindowLength());
		retVal.put("maxFrequency", getMaxFrequency());
		retVal.put("timeStep", getTimeStep());
		retVal.put("frequencyStep", getFrequencyStep());
		retVal.put("windowShape", getWindowShape().ordinal());
		retVal.put("preEmphasis", getPreEmphasis());
		retVal.put("dynamicRange", getDynamicRange());
		retVal.put("dynamicCompression", getDynamicCompression());
		return retVal;
	}

	public static void saveForSessionParticipant(Project project, Session session, Participant participant, SpectrogramSettings settings) {
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
					participantJSON.put("spectrogramSettings", settings.toJSON());
					return retVal;
				});
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
	}

	public static SpectrogramSettings getSettingsForSessionParticipant(Project project, Session session, Participant participant) {
		final SpectrogramSettings retVal = new SpectrogramSettings();
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
						if (participantJSON.has("spectrogramSettings")) {
							final JSONObject spectrogramSettingsJSON = participantJSON.getJSONObject("spectrogramSettings");
							retVal.setWindowLength(spectrogramSettingsJSON.optDouble("windowLength", getDefaultWindowLength()));
							retVal.setMaxFrequency(spectrogramSettingsJSON.optDouble("maxFrequency", getDefaultMaxFrequency()));
							retVal.setTimeStep(spectrogramSettingsJSON.optDouble("timeStep", getDefaultTimeStep()));
							retVal.setFrequencyStep(spectrogramSettingsJSON.optDouble("frequencyStep", getDefaultFrequencyStep()));
							final int windowShapeOrdinal = spectrogramSettingsJSON.optInt("windowShape", getDefaultWindowShape().ordinal());
							retVal.setWindowShape(kSound_to_Spectrogram_windowShape.values()[windowShapeOrdinal]);
							retVal.setPreEmphasis(spectrogramSettingsJSON.optDouble("preEmphasis", getDefaultPreEmphasis()));
							retVal.setDynamicRange(spectrogramSettingsJSON.optDouble("dynamicRange", getDefaultDynamicRange()));
							retVal.setDynamicCompression(spectrogramSettingsJSON.optDouble("dynamicCompression", getDefaultDynamicCompression()));
						}
					}
				}
			}
		}
		return retVal;
	}

}
