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
package ca.phon.app.csv;

import ca.phon.csv.CSVReader;
import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.formatter.PeriodFormatter;
import ca.phon.ipa.Phone;
import ca.phon.project.MutableProject;
import ca.phon.project.Project;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.format.MediaSegmentFormatter;
import ca.phon.session.tierdata.TierData;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Language;

import java.io.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * Class to import CSV files into a Phon project as one or more sessions. All
 * sessions
 * will be imported into the same corpus. If no corpus is specified, a new
 * corpus
 * named "imported" will be created.
 */
public class CSVImporter {

    private final Project project;
    private final String selectedCorpus;
    private final List<CSVImporterListener> listenerList = new ArrayList<>();
    private final SessionFactory sessionFactory = SessionFactory.newFactory();
    private String fileName;

    public CSVImporter(Project project, String selectedCorpus) {
        this.project = project;
        this.selectedCorpus = selectedCorpus;
    }

    public void addListener(CSVImporterListener csvImporterListener) {
        listenerList.add(csvImporterListener);
    }

    private void saveSession(Session session) throws IOException {
        final MutableProject mutableProject = project.getExtension(MutableProject.class);
        if (mutableProject == null) {
            throw new IOException("Project is not mutable");
        }
        var writeLock = mutableProject.getSessionWriteLock(session);
        try {
            mutableProject.saveSession(session, writeLock);
        } finally {
            mutableProject.releaseSessionWriteLock(session, writeLock);
        }
    }

    /**
     * Reformat the tier data from the CSV file. Older versions of Phon used '[ ]'
     * to divide data into
     * phonetic groups. This method removes the brackets and returns the data as a
     * single tier string.
     *
     * @param currentTierData the current tier data
     * @return the reformatted tier data or the original data if it doesn't match
     *         the old format
     */
    private String reformatTierData(String currentTierData) {
        if (currentTierData.matches("\\[.*?\\](\\s\\[.*?\\])*")) {
            return currentTierData.replaceAll("[\\[\\]]", "").trim();
        }
        return currentTierData;
    }

    /**
     * &
     * Import a CSV file into the project. The CSV file must be in the format
     * specified by the {@link CSVImportSettings} object.
     *
     *
     * @param filePath csv file path
     * @param settings csv import settings
     * @throws IOException if an error occurs while reading the file or writing the
     *                     session
     */
    public void importCSV(String filePath, CSVImportSettings settings) throws IOException {
        final MutableProject mutableProject = project.getExtension(MutableProject.class);
        if (mutableProject == null) {
            throw new IOException("Project is not mutable");
        }

        fileName = filePath;

        var inputStreamReader = new InputStreamReader(new FileInputStream(filePath), settings.getEncoding());
        var csvReader = new CSVReader(
                inputStreamReader,
                settings.getSeparators(),
                settings.getQuoteType(),
                settings.getTrimSpaces());

        // Session Path
        var sessionPathTier = settings.getColumnByType(CSVColumnType.SESSION_PATH);

        // Corpus
        var corpusNameTier = settings.getColumnByType(CSVColumnType.CORPUS_NAME);

        // Session
        var sessionNameTier = settings.getColumnByType(CSVColumnType.SESSION_NAME);
        var sessionMediaTier = settings.getColumnByType(CSVColumnType.SESSION_MEDIA);
        var sessionDateTier = settings.getColumnByType(CSVColumnType.SESSION_DATE);

        // Participant
        Map<SessionPath, Map<String, Participant>> participantMap = new HashMap<>();

        // Record Language
        var recordLanguageTier = settings.getColumnByType(CSVColumnType.RECORD_LANGUAGE);

        var importColumnList = settings.getImportColumnList();
        String currentCorpus = this.selectedCorpus == null ? "imported" : this.selectedCorpus;

        if (sessionPathTier.isEmpty() && corpusNameTier.isEmpty()) {
            // If no session path and no corpus name columns, use "." as corpus name
            currentCorpus = ".";
            if (!project.hasCorpus(currentCorpus)) {
                mutableProject.addCorpus(currentCorpus);
            }
        }

        // Default session name from CSV file name (without path and extension)
        String defaultSessionName = "";
        if (sessionPathTier.isEmpty() && sessionNameTier.isEmpty()) {
            // Extract file name from full path
            String csvFileName = new File(fileName).getName();
            // Remove .csv extension if present
            if (csvFileName.toLowerCase().endsWith(".csv")) {
                defaultSessionName = csvFileName.substring(0, csvFileName.length() - 4);
            } else {
                defaultSessionName = csvFileName;
            }
        }

        String currentSessionName = defaultSessionName;
        Optional<Session> currentSession = Optional.empty();
        Map<String, Session> importedSessions = new HashMap<>();

        // Start iterating

        int csvRowIndex = 0;

        String[] row = csvReader.readNext();

        if (settings.isUseFirstRowAsHeader()) {
            csvRowIndex++;
            row = csvReader.readNext();
        }

        while (row != null) {

            boolean corpusChanged = false;
            boolean sessionChanged = false;

            if (isImported(sessionPathTier)) {
                final SessionPath sp = sessionFactory.createSessionPath(row[sessionPathTier.get().getCsvColumnIndex()]);

                if (!currentCorpus.equals(sp.getFolder())) {
                    currentCorpus = sp.getFolder();
                    corpusChanged = true;
                    if (!project.hasCorpus(currentCorpus)) {
                        mutableProject.addCorpus(currentCorpus);
                    }
                }

                if (!currentSessionName.equals(sp.getSessionName())) {
                    currentSessionName = sp.getSessionName();
                    sessionChanged = true;
                }
            } else {
                if (isImported(corpusNameTier)) {
                    String corpusNameFromRow = row[corpusNameTier.get().getCsvColumnIndex()];
                    if (!currentCorpus.equals(corpusNameFromRow)) {
                        currentCorpus = corpusNameFromRow;
                        corpusChanged = true;
                        if (!project.hasCorpus(currentCorpus)) {
                            mutableProject.addCorpus(currentCorpus);
                        }
                    }
                } else if (sessionPathTier.isEmpty()) {
                    // If no session path and no corpus name column, use "." as corpus name
                    if (!currentCorpus.equals(".")) {
                        currentCorpus = ".";
                        corpusChanged = true;
                        if (!project.hasCorpus(currentCorpus)) {
                            mutableProject.addCorpus(currentCorpus);
                        }
                    }
                }

                if (isImported(sessionNameTier)) {
                    String sessionNameFromRow = row[sessionNameTier.get().getCsvColumnIndex()];
                    if (!currentSessionName.equals(sessionNameFromRow)) {
                        currentSessionName = sessionNameFromRow;
                        sessionChanged = true;
                    }
                } else if (sessionPathTier.isEmpty()) {
                    // If no session path and no session name column, keep using the default file
                    // name
                    // (currentSessionName is already set to defaultSessionName above)
                    // Mark as changed on first row to ensure session gets created
                    if (csvRowIndex == 0 || (settings.isUseFirstRowAsHeader() && csvRowIndex == 1)) {
                        sessionChanged = true;
                    }
                }
            }

            if (corpusChanged || sessionChanged) {
                // If this isn't the first session
                if (currentSession.isPresent()) {
                    // Save the session
                    saveSession(currentSession.get());
                }

                // Get the session path for the new session
                String newSessionPath = currentCorpus + "." + currentSessionName;

                // If it has already been imported
                if (importedSessions.containsKey(newSessionPath)) {
                    // Set the current session to the one from the map
                    currentSession = Optional.of(importedSessions.get(newSessionPath));
                }
                // Otherwise
                else {
                    // Create a new session
                    Session newSession = sessionFactory.createSession();
                    newSession.setCorpus(currentCorpus);
                    newSession.setName(currentSessionName);
                    // Set the session media location if there is one
                    if (isImported(sessionMediaTier)) {
                        String sessionMediaString = row[sessionMediaTier.get().getCsvColumnIndex()];
                        if (sessionMediaString.length() > 0) {
                            newSession.setMediaLocation(sessionMediaString);
                        }
                    }
                    // Set the session date if there is one
                    if (isImported(sessionDateTier)) {
                        String sessionDateString = row[sessionDateTier.get().getCsvColumnIndex()];
                        if (sessionDateString.length() > 0) {
                            newSession.setDate(getDateFromTier(
                                    sessionDateTier.get(),
                                    sessionDateString,
                                    fileName,
                                    csvRowIndex,
                                    newSession,
                                    newSession.getRecordCount()));
                        }
                    }
                    // Add the new session to the map
                    importedSessions.put(newSessionPath, newSession);
                    var sessionPathObject = new SessionPath(currentCorpus, currentSessionName);
                    participantMap.put(sessionPathObject, new HashMap<>());
                    // Set the current session to the one just created
                    currentSession = Optional.of(newSession);

                    // Notify the listeners that a new session has been created
                    for (CSVImporterListener listener : this.listenerList) {
                        listener.sessionCreated(fileName, newSession);
                    }
                }
            }

            // Participant
            var participantKey = getParticipantKey(settings, row);

            var sessionPath = new SessionPath(currentCorpus, currentSessionName);

            var participant = Participant.UNKNOWN;
            if (!participantMap.get(sessionPath).containsKey(participantKey)) {
                participant = createParticipant(
                        settings,
                        row,
                        currentSession.get(),
                        csvRowIndex,
                        currentSession.get().getRecordCount());
                currentSession.get().addParticipant(participant);
                participantMap.get(sessionPath).put(participantKey, participant);
            } else {
                participant = participantMap.get(sessionPath).get(participantKey);
            }

            // Segment
            MediaSegment segment = setupSegment(
                    settings,
                    row,
                    currentSession.get(),
                    csvRowIndex,
                    currentSession.get().getRecordCount());

            // Record
            Record record = sessionFactory.createRecord(currentSession.get());
            record.setSpeaker(participant);

            if (segment != null) {
                record.getSegmentTier().setValue(segment);
            }

            boolean firstIPAImported = false;

            for (CSVColumn importColumn : importColumnList) {
                String field = reformatTierData(row[importColumn.getCsvColumnIndex()]);
                switch (importColumn.getColumnType()) {
                    case USER_TIER -> {
                        importUserTier(currentSession.get(), record, importColumn, field);
                    }
                    case ORTHOGRAPHY -> {
                        importOrthographyTier(
                                record,
                                field,
                                importColumn,
                                currentSession.get(),
                                csvRowIndex,
                                currentSession.get().getRecordCount());
                    }
                    case IPA_TARGET -> {
                        firstIPAImported = importIPATargetTier(
                                record,
                                field,
                                firstIPAImported,
                                importColumn,
                                currentSession.get(),
                                csvRowIndex,
                                currentSession.get().getRecordCount());
                    }
                    case IPA_ACTUAL -> {
                        firstIPAImported = importIPAActualTier(
                                record,
                                field,
                                firstIPAImported,
                                importColumn,
                                currentSession.get(),
                                csvRowIndex,
                                currentSession.get().getRecordCount());
                    }
                    case NOTES -> {
                        importNotesTier(
                                record,
                                field,
                                importColumn,
                                currentSession.get(),
                                csvRowIndex,
                                currentSession.get().getRecordCount());
                    }
                    default -> {
                    }
                }
            }

            if (isImported(recordLanguageTier)) {
                String recordLanguageString = row[recordLanguageTier.get().getCsvColumnIndex()];

                if (recordLanguageString.length() > 0) {
                    try {
                        record.setLanguage(Language.parseLanguage(recordLanguageString));
                    } catch (IllegalArgumentException e) {
                        fireParsingError(
                                fileName,
                                csvRowIndex,
                                recordLanguageTier.get().getCsvColumnIndex(),
                                0,
                                recordLanguageTier.get().getColumnType(),
                                currentSession.get(),
                                currentSession.get().getRecordCount(),
                                e);
                    }
                }
            }

            currentSession.get().addRecord(record);
            csvRowIndex++;
            row = csvReader.readNext();
        }

        csvReader.close();

        saveSession(currentSession.get());

        // Notify the listeners that the import is complete
        for (CSVImporterListener listener : this.listenerList) {
            listener.importComplete(fileName);
        }
    }

    private void importUserTier(Session session, Record record, CSVColumn importColumn, String field) {
        if (!importColumn.shouldImportThisColumn())
            return;

        String tierName = importColumn.getOption(CSVImportSettings.USER_TIER_NAME_KEY);

        final UserTierType userTierType = UserTierType.fromPhonTierName(tierName);
        Class<?> tierType = TierData.class;
        if (userTierType != null) {
            tierType = userTierType.getType();
        }

        var optionalTierDescription = StreamSupport
                .stream(session.getUserTiers().spliterator(), false)
                .filter(td -> td.getName().equals(tierName))
                .findFirst();
        TierDescription tierDescription;
        if (optionalTierDescription.isPresent()) {
            tierDescription = optionalTierDescription.get();
        } else {
            tierDescription = sessionFactory.createTierDescription(
                    tierName,
                    tierType,
                    new HashMap<>());
            session.addUserTier(tierDescription);
        }

        var userTier = sessionFactory.createTier(
                tierDescription.getName(),
                tierDescription.getDeclaredType());
        userTier.setText(field);
        record.putTier(userTier);
    }

    private void importOrthographyTier(
            Record record,
            String field,
            CSVColumn column,
            Session session,
            int csvRecordIndex,
            int sessionRecordIndex) {
        var orthographyTier = record.getOrthographyTier();
        orthographyTier.setText(field);
        if (orthographyTier.isUnvalidated()) {
            var e = orthographyTier.getUnvalidatedValue().getParseError();
            fireParsingError(
                    fileName,
                    csvRecordIndex,
                    column.getCsvColumnIndex(),
                    e.getErrorOffset(),
                    column.getColumnType(),
                    session,
                    sessionRecordIndex,
                    e);
        }
    }

    private boolean importIPATargetTier(
            Record record,
            String field,
            boolean firstIPAImported,
            CSVColumn column,
            Session session,
            int csvRecordIndex,
            int sessionRecordIndex) {
        var ipaTargetTier = record.getIPATargetTier();
        ipaTargetTier.setText(field);
        if (ipaTargetTier.isUnvalidated()) {
            var e = ipaTargetTier.getUnvalidatedValue().getParseError();
            fireParsingError(
                    fileName,
                    csvRecordIndex,
                    column.getCsvColumnIndex(),
                    e.getErrorOffset(),
                    column.getColumnType(),
                    session,
                    sessionRecordIndex,
                    e);
            return false;
        }

        var ipaElementStream = StreamSupport.stream(
                ipaTargetTier.getValue().spliterator(),
                false);
        ipaElementStream = ipaElementStream.filter(element -> element instanceof Phone);
        boolean syllabificationRequired = ipaElementStream.allMatch(
                ipaElement -> ipaElement.getScType() == SyllableConstituentType.UNKNOWN);
        if (syllabificationRequired) {
            var syllabifier = column.getOption("syllabifierLanguage") != null
                ? SyllabifierLibrary.getInstance().getSyllabifierForLanguage(column.getOption("syllabifierLanguage"))
                : SyllabifierLibrary.getInstance().defaultSyllabifier();
            syllabifier.syllabify(ipaTargetTier.getValue().toList());
        }

        if (firstIPAImported) {
            // Calculate and set the phone alignment
            record.setPhoneAlignment(PhoneAlignment.fromTiers(
                    record.getIPATargetTier(),
                    record.getIPAActualTier()));
        }

        return true;
    }

    private boolean importIPAActualTier(
            Record record,
            String field,
            boolean firstIPAImported,
            CSVColumn column,
            Session session,
            int csvRecordIndex,
            int sessionRecordIndex) {
        var ipaActualTier = record.getIPAActualTier();
        ipaActualTier.setText(field);
        if (ipaActualTier.isUnvalidated()) {
            var e = ipaActualTier.getUnvalidatedValue().getParseError();
            fireParsingError(
                    fileName,
                    csvRecordIndex,
                    column.getCsvColumnIndex(),
                    e.getErrorOffset(),
                    column.getColumnType(),
                    session,
                    sessionRecordIndex,
                    e);

            return false;
        }
        var ipaElementStream = StreamSupport.stream(
                ipaActualTier.getValue().spliterator(),
                false);
        ipaElementStream = ipaElementStream.filter(element -> element instanceof Phone);
        boolean syllabificationRequired = ipaElementStream.allMatch(
                ipaElement -> ipaElement.getScType() == SyllableConstituentType.UNKNOWN);
        if (syllabificationRequired) {
            var syllabifier = column.getOption("syllabifierLanguage") != null
                ? SyllabifierLibrary.getInstance().getSyllabifierForLanguage(column.getOption("syllabifierLanguage"))
                : SyllabifierLibrary.getInstance().defaultSyllabifier();
            syllabifier.syllabify(ipaActualTier.getValue().toList());
        }

        if (firstIPAImported) {
            // Calculate and set the phone alignment
            record.setPhoneAlignment(PhoneAlignment.fromTiers(
                    record.getIPATargetTier(),
                    record.getIPAActualTier()));
        }

        return true;
    }

    private void importNotesTier(
            Record record,
            String field,
            CSVColumn column,
            Session session,
            int csvRecordIndex,
            int sessionRecordIndex) {
        var notesTier = record.getNotesTier();
        notesTier.setText(field);
        if (notesTier.isUnvalidated()) {
            var e = notesTier.getUnvalidatedValue().getParseError();
            fireParsingError(
                    fileName,
                    csvRecordIndex,
                    column.getCsvColumnIndex(),
                    e.getErrorOffset(),
                    column.getColumnType(),
                    session,
                    sessionRecordIndex,
                    e);
        }
    }

    private boolean isImported(Optional<CSVColumn> column) {
        return column.isPresent() ? column.get().shouldImportThisColumn() : false;
    }

    private void fireParsingError(
            String fileName,
            int csvRecordIndex,
            int fieldIndex,
            int charPosInField,
            CSVColumnType columnType,
            Session session,
            int recordIndexInSession,
            Exception e) {
        for (CSVImporterListener listener : this.listenerList) {
            listener.parsingError(
                    fileName,
                    csvRecordIndex,
                    fieldIndex,
                    charPosInField,
                    columnType,
                    session,
                    recordIndexInSession,
                    e);
        }
    }

    private String getParticipantKey(CSVImportSettings settings, String[] row) {

        var participantRoleTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_ROLE);
        var participantNameTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_NAME);
        var participantSexTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_SEX);
        var participantBirthdayTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_BIRTHDAY);
        var participantAgeTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_AGE);
        var participantLanguageTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_LANGUAGE);
        var participantEducationTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_EDUCATION);
        var participantSESTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_SES);

        StringBuilder stringBuilder = new StringBuilder();

        if (isImported(participantRoleTier)) {
            stringBuilder.append(row[participantRoleTier.get().getCsvColumnIndex()]);
        }
        stringBuilder.append(".");
        if (isImported(participantNameTier)) {
            stringBuilder.append(row[participantNameTier.get().getCsvColumnIndex()]);
        }
        stringBuilder.append(".");
        if (isImported(participantSexTier)) {
            stringBuilder.append(row[participantSexTier.get().getCsvColumnIndex()]);
        }
        stringBuilder.append(".");
        if (isImported(participantBirthdayTier)) {
            stringBuilder.append(row[participantBirthdayTier.get().getCsvColumnIndex()]);
        }
        stringBuilder.append(".");
        if (isImported(participantAgeTier)) {
            stringBuilder.append(row[participantAgeTier.get().getCsvColumnIndex()]);
        }
        stringBuilder.append(".");
        if (isImported(participantLanguageTier)) {
            stringBuilder.append(row[participantLanguageTier.get().getCsvColumnIndex()]);
        }
        stringBuilder.append(".");
        if (isImported(participantEducationTier)) {
            stringBuilder.append(row[participantEducationTier.get().getCsvColumnIndex()]);
        }
        stringBuilder.append(".");
        if (isImported(participantSESTier)) {
            stringBuilder.append(row[participantSESTier.get().getCsvColumnIndex()]);
        }

        return stringBuilder.toString();
    }

    private Participant createParticipant(
            CSVImportSettings settings,
            String[] row,
            Session session,
            int csvRecordIndex,
            int sessionRecordIndex) {

        var participantRoleTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_ROLE);
        var participantNameTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_NAME);
        var participantSexTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_SEX);
        var participantBirthdayTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_BIRTHDAY);
        var participantAgeTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_AGE);
        var participantLanguageTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_LANGUAGE);
        var participantEducationTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_EDUCATION);
        var participantSESTier = settings.getColumnByType(CSVColumnType.PARTICIPANT_SES);

        Participant createdParticipant = sessionFactory.createParticipant();
        boolean allFieldsEmpty = true;

        // Role
        var role = ParticipantRole.PARTICIPANT;
        if (isImported(participantRoleTier)) {
            String roleString = row[participantRoleTier.get().getCsvColumnIndex()];
            if (ParticipantRole.fromString(roleString) == ParticipantRole.UNIDENTIFIED) {
                return Participant.UNKNOWN;
            }
            if (roleString.length() > 0) {
                role = ParticipantRole.fromString(roleString);
                if (role == null) {
                    role = ParticipantRole.PARTICIPANT;
                }

                allFieldsEmpty = false;
            }
        }
        createdParticipant.setRole(role);
        createdParticipant.setId(session.getParticipants().getPreferredParticipantId(role));

        // Name
        if (isImported(participantNameTier)) {
            String nameString = row[participantNameTier.get().getCsvColumnIndex()];

            if (nameString.length() > 0) {
                createdParticipant.setName(nameString);
                allFieldsEmpty = false;
            }
        }

        // Sex
        if (isImported(participantSexTier)) {
            String participantSexString = row[participantSexTier.get().getCsvColumnIndex()];

            if (participantSexString.length() > 0) {
                createdParticipant.setSex(Sex.valueOf(participantSexString));
            } else {
                createdParticipant.setSex(Sex.UNSPECIFIED);
            }
        } else {
            createdParticipant.setSex(Sex.UNSPECIFIED);
        }

        // Age
        if (isImported(participantAgeTier)) {
            String participantAgeString = row[participantAgeTier.get().getCsvColumnIndex()];

            if (participantAgeString.length() > 0) {
                try {
                    createdParticipant.setAge(PeriodFormatter.stringToPeriod(participantAgeString));
                } catch (ParseException e) {
                    fireParsingError(
                            fileName,
                            csvRecordIndex,
                            participantAgeTier.get().getCsvColumnIndex(),
                            e.getErrorOffset(),
                            participantAgeTier.get().getColumnType(),
                            session,
                            sessionRecordIndex,
                            e);
                }
            }
        }

        // Birthday
        if (isImported(participantBirthdayTier)) {
            String participantBirthdayString = row[participantBirthdayTier.get().getCsvColumnIndex()];

            if (participantBirthdayString.length() > 0) {

                createdParticipant.setBirthDate(getDateFromTier(
                        participantBirthdayTier.get(),
                        participantBirthdayString,
                        fileName,
                        csvRecordIndex,
                        session,
                        session.getRecordCount()));
            }
        }

        // Language
        if (isImported(participantLanguageTier)) {
            String participantLanguageString = row[participantLanguageTier.get().getCsvColumnIndex()];

            if (participantLanguageString.length() > 0) {
                createdParticipant.setLanguage(participantLanguageString);
            }
        }

        // Education
        if (isImported(participantEducationTier)) {
            String participantEducationString = row[participantEducationTier.get().getCsvColumnIndex()];

            if (participantEducationString.length() > 0) {
                createdParticipant.setEducation(participantEducationString);
            }
        }

        // SES
        if (isImported(participantSESTier)) {
            String participantSESString = row[participantSESTier.get().getCsvColumnIndex()];

            if (participantSESString.length() > 0) {
                createdParticipant.setSES(participantSESString);
            }
        }

        if (allFieldsEmpty) {
            return Participant.UNKNOWN;
        }
        return createdParticipant;
    }

    private MediaSegment setupSegment(
            CSVImportSettings settings,
            String[] row,
            Session session,
            int csvRecordIndex,
            int sessionRecordIndex) {

        var segment = sessionFactory.createMediaSegment();

        var segmentTier = settings.getColumnByType(CSVColumnType.SEGMENT);
        var segmentStartTier = settings.getColumnByType(CSVColumnType.SEGMENT_START);
        var segmentEndTier = settings.getColumnByType(CSVColumnType.SEGMENT_END);
        var segmentDurationTier = settings.getColumnByType(CSVColumnType.SEGMENT_DURATION);

        boolean validSegment = false;
        if (isImported(segmentTier)) {
            var segmentString = row[segmentTier.get().getCsvColumnIndex()];
            MediaSegmentFormatter mediaSegmentFormatter = new MediaSegmentFormatter();
            try {
                segment.setSegment(mediaSegmentFormatter.parse(segmentString));
                validSegment = true;
            } catch (ParseException e) {
                fireParsingError(
                        fileName,
                        csvRecordIndex,
                        segmentTier.get().getCsvColumnIndex(),
                        e.getErrorOffset(),
                        segmentTier.get().getColumnType(),
                        session,
                        sessionRecordIndex,
                        e);
            }
        } else if (isImported(segmentStartTier)) {
            String segmentStartString = row[segmentStartTier.get().getCsvColumnIndex()];
            try {
                segment.setStartValue(MediaTimeFormatter.parseTimeToMilliseconds(segmentStartString));
            } catch (ParseException e) {
                fireParsingError(
                        fileName,
                        csvRecordIndex,
                        segmentStartTier.get().getCsvColumnIndex(),
                        e.getErrorOffset(),
                        segmentStartTier.get().getColumnType(),
                        session,
                        sessionRecordIndex,
                        e);
            }
            if (isImported(segmentEndTier)) {
                String segmentEndString = row[segmentEndTier.get().getCsvColumnIndex()];
                try {
                    segment.setEndValue(MediaTimeFormatter.parseTimeToMilliseconds(segmentEndString));
                    validSegment = true;
                } catch (ParseException e) {
                    fireParsingError(
                            fileName,
                            csvRecordIndex,
                            segmentEndTier.get().getCsvColumnIndex(),
                            e.getErrorOffset(),
                            segmentEndTier.get().getColumnType(),
                            session,
                            sessionRecordIndex,
                            e);
                }
            } else if (isImported(segmentDurationTier)) {
                String segmentDurationString = row[segmentDurationTier.get().getCsvColumnIndex()];
                try {
                    segment.setEndValue(
                            segment.getStartValue()
                                    + MediaTimeFormatter.parseTimeToMilliseconds(segmentDurationString));
                } catch (ParseException e) {
                    fireParsingError(
                            fileName,
                            csvRecordIndex,
                            segmentDurationTier.get().getCsvColumnIndex(),
                            e.getErrorOffset(),
                            segmentDurationTier.get().getColumnType(),
                            session,
                            sessionRecordIndex,
                            e);
                }
            }
        } else {
            segment.setStartValue(0.0f);
            segment.setEndValue(0.0f);
            validSegment = true;
        }

        return validSegment ? segment : null;
    }

    private LocalDate getDateFromTier(
            CSVColumn dateTier,
            String dateString,
            String fileName,
            int csvRecordIndex,
            Session session,
            int sessionRecordIndex) {
        String formatString = dateTier.getOption("dateFormat");
        String localeString = dateTier.getOption("locale");

        if (formatString == null || formatString.equals("DEFAULT") || formatString.equals("ISO")) {
            try {
                return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                fireParsingError(
                        fileName,
                        csvRecordIndex,
                        dateTier.getCsvColumnIndex(),
                        e.getErrorIndex(),
                        dateTier.getColumnType(),
                        session,
                        sessionRecordIndex,
                        e);
                return null;
            }
        } else {
            var formatStyle = Arrays
                    .stream(FormatStyle.values())
                    .filter(fs -> fs.toString().equals(formatString))
                    .findFirst();

            var locale = Arrays
                    .stream(Locale.getAvailableLocales())
                    .filter(loc -> loc.getDisplayName().equals(localeString))
                    .findFirst();

            var formatter = DateTimeFormatter
                    .ofLocalizedDate(formatStyle.orElse(FormatStyle.SHORT))
                    .withLocale(locale.orElse(Locale.getDefault()));

            try {
                return LocalDate.from(formatter.parse(dateString));
            } catch (DateTimeParseException e) {
                fireParsingError(
                        fileName,
                        csvRecordIndex,
                        dateTier.getCsvColumnIndex(),
                        e.getErrorIndex(),
                        dateTier.getColumnType(),
                        session,
                        sessionRecordIndex,
                        e);
                return null;
            }
        }
    }
}
