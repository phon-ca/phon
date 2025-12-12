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

import ca.hedlund.jpraat.binding.fon.kPitch_unit;
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

public class PitchSettings {
	
	public static final String TIME_STEP_PROP = PitchSettings.class.getName() + ".timStep";
	public static final double DEFAULT_TIME_STEP = 0.0;
	private double timeStep = getDefaultTimeStep();
	
	public static double getDefaultTimeStep() {
		return PrefHelper.getDouble(TIME_STEP_PROP, DEFAULT_TIME_STEP);
	}
	
	public static final String RANGE_START_PROP = PitchSettings.class.getName() + ".rangeStart";
	public static final double DEFAULT_RANGE_START = 75.0;
	private double rangeStart = getDefaultRangeStart();

	public static double getDefaultRangeStart() {
		return PrefHelper.getDouble(RANGE_START_PROP, DEFAULT_RANGE_START);
	}
	
	public static final String RANGE_END_PROP = PitchSettings.class.getName() + ".rangeEnd";
	public static final double DEFAULT_RANGE_END = 500.0;
	private double rangeEnd = getDefaultRangeEnd();
	
	public static double getDefaultRangeEnd() {
		return PrefHelper.getDouble(RANGE_END_PROP, DEFAULT_RANGE_END);
	}
	
	public static final String UNITS_PROP = PitchSettings.class.getName() + ".units";
	public static final kPitch_unit DEFAULT_UNITS = kPitch_unit.HERTZ;
	private kPitch_unit units = getDefaultUnits();
	
	public static kPitch_unit getDefaultUnits() {
		final int pitchUnitOrdinal = PrefHelper.getInt(UNITS_PROP, DEFAULT_UNITS.ordinal());
		return kPitch_unit.values()[pitchUnitOrdinal];
	}
	
	public static final String AUTOCORRELATE_PROP = PitchSettings.class.getName() + ".autoCorrelate";
	public static final boolean DEFAULT_AUTOCORRELATE = true;
	private boolean autoCorrelate = getDefaultAutocorrelate();
	
	public static boolean getDefaultAutocorrelate() {
		return PrefHelper.getBoolean(AUTOCORRELATE_PROP, DEFAULT_AUTOCORRELATE);
	}
	
	public static final String VERY_ACCURATE_PROP = PitchSettings.class.getName() + ".veryAccurate";
	public static final boolean DEFAULT_VERY_ACCURATE = false;
	private boolean veryAccurate = getDefaultVeryAccurate();
	
	public static boolean getDefaultVeryAccurate() {
		return PrefHelper.getBoolean(VERY_ACCURATE_PROP, DEFAULT_VERY_ACCURATE);
	}
	
	public static final String MAX_CANDIDATES_PROP = PitchSettings.class.getName() + ".maxCandidates";
	public static final int DEFAULT_MAX_CANDIDATES = 15;
	private int maxCandidates = getDefaultMaxCandidates();
	
	public static int getDefaultMaxCandidates() {
		return PrefHelper.getInt(MAX_CANDIDATES_PROP, DEFAULT_MAX_CANDIDATES);
	}
	
	public static final String SILENCE_THRESHOLD_PROP = PitchSettings.class.getName() + ".silenceThreshold";
	public static final double DEFAULT_SILENCE_THRESHOLD = 0.03;
	private double silenceThreshold = getDefaultSilenceThreshold();
	
	public static double getDefaultSilenceThreshold() {
		return PrefHelper.getDouble(SILENCE_THRESHOLD_PROP, DEFAULT_SILENCE_THRESHOLD);
	}
	
	public static final String VOICING_THRESHOLD_PROP = PitchSettings.class.getName( )+ ".voicingThreshold";
	public static final double DEFAULT_VOICING_THRESHOLD = 0.45;
	private double voicingThreshold = getDefaultVoicingThreshold();
	
	public static double getDefaultVoicingThreshold() {
		return PrefHelper.getDouble(VOICING_THRESHOLD_PROP, DEFAULT_VOICING_THRESHOLD);
	}
	
	public static final String OCTAVE_COST_PROP = PitchSettings.class.getName() + ".octaveCost";
	public static final double DEFAULT_OCTAVE_COST = 0.01;
	private double octaveCost = getDefaultOctaveCost();
	
	public static double getDefaultOctaveCost() {
		return PrefHelper.getDouble(OCTAVE_COST_PROP, DEFAULT_OCTAVE_COST);
	}
	
	public static final String OCTAVE_JUMP_COST_PROP = PitchSettings.class.getName() + ".octaveJumpCost";
	public static final double DEFAULT_OCTAVE_JUMP_COST = 0.35;
	private double octaveJumpCost = getDefaultOctaveJumpCost();
	
	public static double getDefaultOctaveJumpCost() {
		return PrefHelper.getDouble(OCTAVE_JUMP_COST_PROP, DEFAULT_OCTAVE_JUMP_COST);
	}
	
	public static final String VOICED_UNVOICED_COST_PROP = PitchSettings.class.getName() + "voicedUnvoicedCost";
	public static final double DEFAULT_VOICED_UNVOICED_COST = 0.14;
	private double voicedUnvoicedCost = getDefaultVoicedUnvoicedCost();
	
	public static double getDefaultVoicedUnvoicedCost() {
		return PrefHelper.getDouble(VOICED_UNVOICED_COST_PROP, DEFAULT_VOICED_UNVOICED_COST);
	}
	
	public static final String DOT_SIZE_PROP = PitchSettings.class.getName() + ".dotSize";
	public static final double DEFAULT_DOT_SIZE = 2.0;
	private double dotSize = getDefaultDotSize();
	
	public static double getDefaultDotSize() {
		return PrefHelper.getDouble(DOT_SIZE_PROP, DEFAULT_DOT_SIZE);
	}

	public double getRangeStart() {
		return rangeStart;
	}

	public void setRangeStart(double rangeStart) {
		this.rangeStart = rangeStart;
	}

	public double getRangeEnd() {
		return rangeEnd;
	}

	public void setRangeEnd(double rangeEnd) {
		this.rangeEnd = rangeEnd;
	}

	public kPitch_unit getUnits() {
		return units;
	}

	public void setUnits(kPitch_unit units) {
		this.units = units;
	}

	public boolean isAutoCorrelate() {
		return autoCorrelate;
	}

	public void setAutoCorrelate(boolean autoCorrelate) {
		this.autoCorrelate = autoCorrelate;
	}

	public double getDotSize() {
		return dotSize;
	}

	public void setDotSize(double dotSize) {
		this.dotSize = dotSize;
	}
	
	public double getTimeStep() {
		return this.timeStep;
	}
	
	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}
	
	public boolean isVeryAccurate() {
		return veryAccurate;
	}

	public void setVeryAccurate(boolean veryAccurate) {
		this.veryAccurate = veryAccurate;
	}

	public int getMaxCandidates() {
		return maxCandidates;
	}

	public void setMaxCandidates(int maxCandidates) {
		this.maxCandidates = maxCandidates;
	}
	
	public double getSilenceThreshold() {
		return silenceThreshold;
	}

	public void setSilenceThreshold(double silenceThreshold) {
		this.silenceThreshold = silenceThreshold;
	}
	

	public double getVoicingThreshold() {
		return voicingThreshold;
	}

	public void setVoicingThreshold(double voicingThreshold) {
		this.voicingThreshold = voicingThreshold;
	}

	public double getOctaveCost() {
		return octaveCost;
	}

	public void setOctaveCost(double octaveCost) {
		this.octaveCost = octaveCost;
	}

	public double getOctaveJumpCost() {
		return octaveJumpCost;
	}

	public void setOctaveJumpCost(double octaveJumpCost) {
		this.octaveJumpCost = octaveJumpCost;
	}

	public double getVoicedUnvoicedCost() {
		return voicedUnvoicedCost;
	}

	public void setVoicedUnvoicedCost(double voicedUnvoicedCost) {
		this.voicedUnvoicedCost = voicedUnvoicedCost;
	}

	public void loadDefaults() {
		setTimeStep(getDefaultTimeStep());
		setAutoCorrelate(getDefaultAutocorrelate());
		setDotSize(getDefaultDotSize());
		setRangeEnd(getDefaultRangeEnd());
		setRangeStart(getDefaultRangeStart());
		setVeryAccurate(getDefaultVeryAccurate());
		setMaxCandidates(getDefaultMaxCandidates());
		setSilenceThreshold(getDefaultSilenceThreshold());
		setVoicingThreshold(getDefaultVoicingThreshold());
		setOctaveCost(getDefaultOctaveCost());
		setOctaveJumpCost(getDefaultOctaveJumpCost());
		setVoicedUnvoicedCost(getDefaultVoicedUnvoicedCost());
		setUnits(getDefaultUnits());
	}
	
	public void saveToPreferences(Preferences prefs) {
		prefs.putDouble(TIME_STEP_PROP, getTimeStep());
		prefs.putBoolean(AUTOCORRELATE_PROP, isAutoCorrelate());
		prefs.putDouble(DOT_SIZE_PROP, getDotSize());
		prefs.putDouble(RANGE_END_PROP, getRangeEnd());
		prefs.putDouble(RANGE_START_PROP, getRangeStart());
		prefs.putBoolean(VERY_ACCURATE_PROP, isVeryAccurate());
		prefs.putInt(MAX_CANDIDATES_PROP, getMaxCandidates());
		prefs.putDouble(SILENCE_THRESHOLD_PROP, getSilenceThreshold());
		prefs.putDouble(VOICING_THRESHOLD_PROP, getVoicingThreshold());
		prefs.putDouble(OCTAVE_COST_PROP, getOctaveCost());
		prefs.putDouble(OCTAVE_JUMP_COST_PROP, getOctaveJumpCost());
		prefs.putDouble(VOICED_UNVOICED_COST_PROP, getVoicedUnvoicedCost());
		prefs.putInt(UNITS_PROP, getUnits().ordinal());
	}
	
	public void saveAsDefaults() {
		final Preferences prefs = PrefHelper.getUserPreferences();
		saveToPreferences(prefs);
	}

	public void loadStandards() {
		setTimeStep(DEFAULT_TIME_STEP);
		setAutoCorrelate(DEFAULT_AUTOCORRELATE);
		setDotSize(DEFAULT_DOT_SIZE);
		setRangeEnd(DEFAULT_RANGE_END);
		setRangeStart(DEFAULT_RANGE_START);
		setVeryAccurate(DEFAULT_VERY_ACCURATE);
		setMaxCandidates(DEFAULT_MAX_CANDIDATES);
		setSilenceThreshold(DEFAULT_SILENCE_THRESHOLD);
		setVoicingThreshold(DEFAULT_VOICING_THRESHOLD);
		setOctaveCost(DEFAULT_OCTAVE_COST);
		setOctaveJumpCost(DEFAULT_OCTAVE_JUMP_COST);
		setVoicedUnvoicedCost(DEFAULT_VOICED_UNVOICED_COST);
		setUnits(DEFAULT_UNITS);
	}

	public JSONObject toJSON() {
		final JSONObject retVal = new JSONObject();
		retVal.put("timeStep", getTimeStep());
		retVal.put("rangeStart", getRangeStart());
		retVal.put("rangeEnd", getRangeEnd());
		retVal.put("units", getUnits().ordinal());
		retVal.put("autoCorrelate", isAutoCorrelate());
		retVal.put("veryAccurate", isVeryAccurate());
		retVal.put("maxCandidates", getMaxCandidates());
		retVal.put("silenceThreshold", getSilenceThreshold());
		retVal.put("voicingThreshold", getVoicingThreshold());
		retVal.put("octaveCost", getOctaveCost());
		retVal.put("octaveJumpCost", getOctaveJumpCost());
		retVal.put("voicedUnvoicedCost", getVoicedUnvoicedCost());
		retVal.put("dotSize", getDotSize());
		return retVal;
	}

	public static void saveForSessionParticipant(Project project, Session session, Participant participant, PitchSettings settings) {
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
					participantJSON.put("pitchSettings", settings.toJSON());
					return retVal;
				});
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
	}

	public static PitchSettings getSettingsForSessionParticipant(Project project, Session session, Participant participant) {
		final PitchSettings retVal = new PitchSettings();
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
						if (participantJSON.has("pitchSettings")) {
							final JSONObject pitchSettingsJSON = participantJSON.getJSONObject("pitchSettings");
							retVal.setTimeStep(pitchSettingsJSON.optDouble("timeStep", getDefaultTimeStep()));
							retVal.setRangeStart(pitchSettingsJSON.optDouble("rangeStart", getDefaultRangeStart()));
							retVal.setRangeEnd(pitchSettingsJSON.optDouble("rangeEnd", getDefaultRangeEnd()));
							final int unitsOrdinal = pitchSettingsJSON.optInt("units", getDefaultUnits().ordinal());
							retVal.setUnits(kPitch_unit.values()[unitsOrdinal]);
							retVal.setAutoCorrelate(pitchSettingsJSON.optBoolean("autoCorrelate", getDefaultAutocorrelate()));
							retVal.setVeryAccurate(pitchSettingsJSON.optBoolean("veryAccurate", getDefaultVeryAccurate()));
							retVal.setMaxCandidates(pitchSettingsJSON.optInt("maxCandidates", getDefaultMaxCandidates()));
							retVal.setSilenceThreshold(pitchSettingsJSON.optDouble("silenceThreshold", getDefaultSilenceThreshold()));
							retVal.setVoicingThreshold(pitchSettingsJSON.optDouble("voicingThreshold", getDefaultVoicingThreshold()));
							retVal.setOctaveCost(pitchSettingsJSON.optDouble("octaveCost", getDefaultOctaveCost()));
							retVal.setOctaveJumpCost(pitchSettingsJSON.optDouble("octaveJumpCost", getDefaultOctaveJumpCost()));
							retVal.setVoicedUnvoicedCost(pitchSettingsJSON.optDouble("voicedUnvoicedCost", getDefaultVoicedUnvoicedCost()));
							retVal.setDotSize(pitchSettingsJSON.optDouble("dotSize", getDefaultDotSize()));
						}
					}
				}
			}
		}
		return retVal;
	}

}
