package ca.phon.app.csv;

import ca.phon.app.log.LogUtil;
import ca.phon.csv.CSVWriter;
import ca.phon.formatter.MediaTimeFormatStyle;
import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.formatter.PeriodFormatStyle;
import ca.phon.formatter.PeriodFormatter;
import ca.phon.orthography.OrthoWordExtractor;
import ca.phon.orthography.Orthography;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.format.MediaSegmentFormatter;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class CSVExporter {
    private JLabel currentlyExportingLabel = null;
    private final List<CSVExporterListener> listenerList = new ArrayList<>();

    public CSVExporter() {}

    public void addListener(CSVExporterListener csvExporterListener) {
        listenerList.add(csvExporterListener);
    }

    public void exportCSV(Session[] sessions, CSVExportSettings settings, String filePath) {

        FileWriter writer = null;
        try {
            writer = new FileWriter(filePath);
        }
        catch (IOException e) {
            fireWritingError(e);
            LogUtil.warning(e);
        }

        if (writer == null) {
            return;
        }

        CSVWriter csvWriter = new CSVWriter(writer);

        var exportColumnList = settings.getExportColumnList();

        if (settings.isUseFirstRowAsHeader()) {
            List<String> headerList = new ArrayList<>();

            for (CSVColumn csvColumn : exportColumnList) {
                headerList.add(csvColumn.getOption("name"));
            }

            try {
                csvWriter.writeNext(headerList.toArray(String[]::new));
            }
            catch (IOException e) {
                fireWritingError(e);
                LogUtil.warning(e);
            }
        }

        for (Session session : sessions) {
            if (currentlyExportingLabel != null) currentlyExportingLabel.setText(session.getName());
            for (Record record : session.getRecords()) {
                List<String> rowList = new ArrayList<>();

                Participant participant = record.getSpeaker();

                for (CSVColumn column : exportColumnList) {
                    String field = "";

                    switch (column.columnType) {
                        case NOTES -> field = getNotesTierValue(record);
                        case ORTHOGRAPHY -> field = getOrthographyTierValue(record, column);
                        case IPA_TARGET -> field = getIPATargetTierValue(record, column);
                        case IPA_ACTUAL -> field = getIPAActualTierValue(record, column);
                        case PHONE_ALIGNMENT -> field = getPhoneAlignment(record);
                        case SESSION_PATH -> field = getSessionPath(session);
                        case CORPUS_NAME -> field = getCorpus(session);
                        case SESSION_NAME -> field = getSessionName(session);
                        case SESSION_DATE -> field = getSessionDate(session, column);
                        case SESSION_MEDIA -> field = getSessionMedia(session);
                        case PARTICIPANT_ID -> field = getParticipantID(participant);
                        case PARTICIPANT_NAME -> field = getParticipantName(participant);
                        case PARTICIPANT_ROLE -> field = getParticipantRole(participant);
                        case PARTICIPANT_LANGUAGE -> field = getParticipantLanguage(participant);
                        case PARTICIPANT_EDUCATION -> field = getParticipantEducation(participant);
                        case PARTICIPANT_SEX -> field = getParticipantSex(participant);
                        case PARTICIPANT_SES -> field = getParticipantSES(participant);
                        case PARTICIPANT_AGE -> field = getParticipantAge(participant, column);
                        case PARTICIPANT_BIRTHDAY -> field = getParticipantBirthday(participant, column);
                        case RECORD_ID -> field = getRecordID(record);
                        case RECORD_NUMBER -> field = getRecordNumber(session, record);
                        case RECORD_LANGUAGE -> field = getRecordLanguage(record);
                        case SEGMENT -> field = getSegmentTierValue(record, column);
                        case SEGMENT_START -> field = getSegmentStart(record, column);
                        case SEGMENT_END -> field = getSegmentEnd(record, column);
                        case SEGMENT_DURATION -> field = getSegmentDuration(record, column);
                        case USER_TIER -> field = getUserTierValue(record, column);
                        default -> System.out.println("Something went wrong");
                    }

                    rowList.add(field);
                }

                try {
                    csvWriter.writeNext(rowList.toArray(String[]::new));
                }
                catch (IOException e) {
                    fireWritingError(e);
                    LogUtil.warning(e);
                }
            }
        }

        try {
            csvWriter.close();
        }
        catch (IOException e) {
            fireWritingError(e);
            LogUtil.warning(e);
        }
    }

    private String getNotesTierValue(Record record) {
        var notesTier = record.getNotesTier();
        if (notesTier.isUnvalidated()) {
            return notesTier.getUnvalidatedValue().toString();
        }
        else if (notesTier.hasValue()) {
            return notesTier.getValue().toString();
        }
        return "";
    }

    private String getOrthographyTierValue(Record record, CSVColumn column) {
        var orthographyTier = record.getOrthographyTier();
        if (orthographyTier.isUnvalidated()) {
            return orthographyTier.getUnvalidatedValue().toString();
        }
        else if (orthographyTier.hasValue()) {
            boolean wordsOnly = column.getOption("wordsOnly").equals("true");
            if (wordsOnly) {
                var orthoWordExtractor = new OrthoWordExtractor();
                orthographyTier.getValue().accept(orthoWordExtractor);
                var wordList = orthoWordExtractor.getWordList();
                Orthography orthography = new Orthography(wordList);
                return orthography.toString();
            }
            return orthographyTier.getValue().toString();
        }
        return "";
    }

    private String getSessionPath(Session session) {
        return session.getSessionPath().toString();
    }

    private String getCorpus(Session session) {
        return session.getCorpus();
    }

    private String getSessionName(Session session) {
        return session.getName();
    }

    private String getSessionDate(Session session, CSVColumn column) {
        return getDateString(session.getDate(), column);
    }

    private String getSessionMedia(Session session) {
        return session.getMediaLocation();
    }

    private String getParticipantID(Participant participant) {
        return participant.getId();
    }

    private String getParticipantName(Participant participant) {
        return participant.getName();
    }

    private String getParticipantRole(Participant participant) {
        return participant.getRole().toString();
    }

    private String getParticipantLanguage(Participant participant) {
        return participant.getLanguage();
    }

    private String getParticipantBirthday(Participant participant, CSVColumn column) {
        return getDateString(participant.getBirthDate(), column);
    }

    private String getParticipantAge(Participant participant, CSVColumn column) {
        String ageFormatString = column.getOption("ageFormat");
        boolean isoFormat = ageFormatString != null && ageFormatString.equals("ISO");

        return PeriodFormatter.periodToString(
            participant.getAgeTo(),
            isoFormat ? PeriodFormatStyle.ISO : PeriodFormatStyle.PHON
        );
    }

    private String getParticipantEducation(Participant participant) {
        return participant.getEducation();
    }

    private String getParticipantSex(Participant participant) {
        return participant.getSex().toString();
    }

    private String getParticipantSES(Participant participant) {
        return participant.getSES();
    }

    private String getRecordID(Record record) {
        return record.getUuid().toString();
    }

    private String getRecordNumber(Session session, Record record) {
        return String.valueOf(session.getRecordPosition(record) + 1);
    }

    private String getRecordLanguage(Record record) {
        return record.getLanguage().toString();
    }

    private String getIPATargetTierValue(Record record, CSVColumn column) {
        var ipaTargetTier = record.getIPATargetTier();
        if (ipaTargetTier.isUnvalidated()) {
            return ipaTargetTier.getUnvalidatedValue().toString();
        }
        else if (ipaTargetTier.hasValue()) {
            String includeSyllabificationString = column.getOption("includeSyllabification");
            boolean includeSyllabification = includeSyllabificationString.equals("true");
            String stripDiacriticsString = column.getOption("stripDiacritics");
            boolean stripDiacritics = stripDiacriticsString.equals("true");
            var ipaTranscript = stripDiacritics ? ipaTargetTier.getValue().stripDiacritics() : ipaTargetTier.getValue();
            return ipaTranscript.toString(includeSyllabification);
        }
        return "";
    }

    private String getIPAActualTierValue(Record record, CSVColumn column) {
        var ipaActualTier = record.getIPAActualTier();
        if (ipaActualTier.isUnvalidated()) {
            return ipaActualTier.getUnvalidatedValue().toString();
        }
        else if (ipaActualTier.hasValue()) {
            String includeSyllabificationString = column.getOption("includeSyllabification");
            boolean includeSyllabification = includeSyllabificationString.equals("true");
            String stripDiacriticsString = column.getOption("stripDiacritics");
            boolean stripDiacritics = stripDiacriticsString.equals("true");
            var ipaTranscript = stripDiacritics ? ipaActualTier.getValue().stripDiacritics() : ipaActualTier.getValue();
            return ipaTranscript.toString(includeSyllabification);
        }
        return "";
    }

    private String getPhoneAlignment(Record record) {
        return record.getPhoneAlignment().toString();
    }

    private String getSegmentTierValue(Record record, CSVColumn column) {
        var segmentTier = record.getSegmentTier();
        if (segmentTier.hasValue()) {

            String formatString = column.getOption("segmentFormat");

            var formatStyle = Arrays
                .stream(MediaTimeFormatStyle.values())
                .filter(format -> format.toString().equals(formatString))
                .findFirst();

            MediaSegmentFormatter mediaSegmentFormatter = new MediaSegmentFormatter(
                formatStyle.orElse(MediaTimeFormatStyle.MINUTES_AND_SECONDS)
            );

            return mediaSegmentFormatter.format(segmentTier.getValue());
        }
        return "";
    }

    private String getSegmentStart(Record record, CSVColumn column) {
        var segmentTier = record.getSegmentTier();
        if (segmentTier.hasValue()) {

            String formatString = column.getOption("segmentFormat");

            var formatStyle = Arrays
                .stream(MediaTimeFormatStyle.values())
                .filter(format -> format.toString().equals(formatString))
                .findFirst();
            
            MediaTimeFormatter mediaTimeFormatter = new MediaTimeFormatter(
                formatStyle.orElse(MediaTimeFormatStyle.MINUTES_AND_SECONDS)
            );

            return mediaTimeFormatter.format(segmentTier.getValue().getStartValue());
        }
        return "";
    }

    private String getSegmentEnd(Record record, CSVColumn column) {
        var segmentTier = record.getSegmentTier();
        if (segmentTier.hasValue()) {

            String formatString = column.getOption("segmentFormat");

            var formatStyle = Arrays
                .stream(MediaTimeFormatStyle.values())
                .filter(format -> format.toString().equals(formatString))
                .findFirst();

            MediaTimeFormatter mediaTimeFormatter = new MediaTimeFormatter(
                formatStyle.orElse(MediaTimeFormatStyle.MINUTES_AND_SECONDS)
            );

            return mediaTimeFormatter.format(segmentTier.getValue().getEndValue());
        }
        return "";
    }

    private String getSegmentDuration(Record record, CSVColumn column) {
        var segmentTier = record.getSegmentTier();
        if (segmentTier.hasValue()) {

            String formatString = column.getOption("segmentFormat");

            var formatStyle = Arrays
                .stream(MediaTimeFormatStyle.values())
                .filter(format -> format.toString().equals(formatString))
                .findFirst();
            
            MediaTimeFormatter mediaTimeFormatter = new MediaTimeFormatter(
                formatStyle.orElse(MediaTimeFormatStyle.MINUTES_AND_SECONDS)
            );

            float duration = segmentTier.getValue().getEndValue() - segmentTier.getValue().getStartValue();
            return mediaTimeFormatter.format(duration);
        }
        return "";
    }

    private String getUserTierValue(Record record, CSVColumn column) {
//        String tierName = column.getOption(CSVExportSettings.USER_TIER_NAME_KEY);
        String tierName = column.getOption("name");
        var tier = record.getTier(tierName);
        if (tier != null && tier.hasValue()) {
            return tier.getValue().toString();
        }
        return "";
    }

    private String getDateString(LocalDate date, CSVColumn column) {
        String formatString = column.getOption("dateFormat");
        String localeString = column.getOption("locale");

        if (formatString == null || formatString.equals("DEFAULT") || formatString.equals("ISO")) {
            return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        }
        else {
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

            return formatter.format(date);
        }
    }

    public void setCurrentlyExportingLabel(JLabel label) {
        currentlyExportingLabel = label;
    }

    private void fireWritingError(IOException e) {
        for (CSVExporterListener listener : this.listenerList) {
            listener.writingError(e);
        }
    }
}
