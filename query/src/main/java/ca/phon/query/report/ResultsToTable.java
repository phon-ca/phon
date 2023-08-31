package ca.phon.query.report;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.query.db.ReportHelper;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultValue;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;

import java.io.IOException;
import java.text.ParseException;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Methods for turning query results into tabular data.
 *
 */
public class ResultsToTable {

    /**
     * Turn multiple result sets for multiple sessions into a table
     *
     * @param project
     * @param results
     * @param includeSessionInfo
     * @param includeSpeakerInfo
     * @param includeTierInfo
     * @param includeMetadata
     * @param ignoreDiacritics
     * @param onlyOrExcept
     * @param selectedDiacritics
     * @return table of results
     */
    public static DefaultTableDataSource createResultTable(Project project, ResultSet[] results, boolean includeSessionInfo,
                boolean includeSpeakerInfo, boolean includeTierInfo, boolean includeMetadata,
                boolean ignoreDiacritics, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
        final DefaultTableDataSource retVal = setupTable(results, includeSessionInfo, includeSpeakerInfo, includeTierInfo, includeMetadata);
        final Set<String> tierNames = collectTierNames(results);
        final Set<String> metadataKeys = collectMetadataKeys(results);

        for(ResultSet rs:results) {
            try {
                final Session session = project.openSession(rs.getCorpus(), rs.getSession());
                for(Result result:rs) {
                    List<Object> rowData = new ArrayList<>();
                    final Record record = session.getRecord(result.getRecordIndex());

                    if(includeSessionInfo) {
                        rowData.add(new SessionPath(rs.getCorpus(), rs.getSession()));
                        rowData.add(session.getDate());
                    }

                    if(includeSpeakerInfo) {
                        final Participant speaker = record.getSpeaker();
                        if(speaker != null) {
                            rowData.add(speaker);

                            final Period age = speaker.getAge(session.getDate());
                            if(age != null) {
                                rowData.add(age);
                            } else {
                                rowData.add("");
                            }
                        } else {
                            rowData.add("");
                            rowData.add("");
                        }

                    }

                    if(includeTierInfo) {
                        rowData.add(result.getRecordIndex()+1);
                        rowData.add(ReportHelper.createReportString(tierNames.toArray(new String[0]), result.getSchema()));
                        final String[] rvVals = new String[result.getNumberOfResultValues()];
                        for(int i = 0; i < result.getNumberOfResultValues(); i++) {
                            final ResultValue rv = result.getResultValue(i);
                            rvVals[i] = rv.getRange().toString();
                        }
                        rowData.add(ReportHelper.createReportString(rvVals, result.getSchema()));
                    }

                    rowData.add(result);

                    // add result objects from record
                    for(String tierName:tierNames) {
                        final List<ResultValue> resultValues = StreamSupport.stream(result.spliterator(), false)
                                .filter( (rv) -> rv.getName().equals(tierName) )
                                .collect(Collectors.toList());

                        List<Object> resultVals = new ArrayList<>();
                        ca.phon.formatter.Formatter<Object> formatter = null;
                        StringBuffer buffer = new StringBuffer();
                        for(ResultValue rv:resultValues) {
                            buffer.setLength(0);
                            Object tierValue = record.getTier(rv.getTierName()).getValue();
                            if(tierValue == null) tierValue = "";

                            // attempt to find a formatter
                            if(formatter == null) {
                                formatter = (Formatter<Object>) FormatterFactory.createFormatter(tierValue.getClass());
                            }

                            boolean defaultOutput = true;
                            // attempt to carry over as much data as possible from
                            // the IPA transcript.
                            if(tierValue instanceof IPATranscript) {
                                IPATranscript origIPA = (IPATranscript)tierValue;

                                // attempt to use result value to find phone indices
                                IPATranscriptBuilder builder = new IPATranscriptBuilder();
                                int startidx = -1;
                                int endidx = -1;
                                for(int pidx = 0; pidx < origIPA.length(); pidx++) {
                                    IPAElement ele = origIPA.elementAt(pidx);
                                    int stringIdx = origIPA.stringIndexOfElement(pidx);
                                    int endEleIdx = stringIdx + ele.toString().length();

                                    if(rv.getRange().getStart() >= stringIdx) {
                                        startidx = pidx;
                                    }
                                    if(rv.getRange().getEnd() == endEleIdx) {
                                        endidx = pidx;
                                    }
                                }

                                // take only whole elements
                                if(startidx >= 0 && endidx >= startidx) {
                                    IPATranscript subVal = origIPA.subsection(startidx, endidx+1);

                                    if(ignoreDiacritics) {
                                        if(onlyOrExcept)
                                            subVal = subVal.stripDiacritics(selectedDiacritics);
                                        else
                                            subVal = subVal.stripDiacriticsExcept(selectedDiacritics);
                                    }

                                    buffer.append(subVal.toString(true));
                                    defaultOutput = false;
                                }
                            }

                            if(defaultOutput) {
                                final String tierTxt =
                                        (formatter != null ? formatter.format(tierValue) : tierValue.toString());

                                String resultTxt =
                                        (rv.getRange().getStart() >= 0 && rv.getRange().getEnd() >= rv.getRange().getFirst() ?
                                                tierTxt.substring( rv.getRange().getStart(), rv.getRange().getEnd() ) : "");

                                if(result.getSchema().equals("DETECTOR") && resultTxt.length() == 0) {
                                    resultTxt = "∅";
                                }

                                if(ignoreDiacritics) {
                                    resultTxt = stripDiacriticsFromText(resultTxt, onlyOrExcept, selectedDiacritics);
                                }

                                if(buffer.length() > 0) buffer.append("..");
                                buffer.append(resultTxt);
                            }
                            Object resultVal = buffer.toString();
                            if(formatter != null) {
                                try {
                                    resultVal = formatter.parse(buffer.toString());
                                } catch (ParseException e) {}
                            }
                            resultVals.add(resultVal);
                        }
                        if(resultVals.size() == 1)
                            rowData.add(resultVals.get(0));
                        else if(resultVals.size() > 1) {
                            String[] txt = resultVals.stream().map(Object::toString).toArray(String[]::new);
                            rowData.add(ReportHelper.createReportString(txt, result.getSchema()));
                        } else {
                            rowData.add(new String());
                        }
                    }

                    if(includeMetadata) {
                        for(String metakey:metadataKeys) {
                            String metaValue = result.getMetadata().get(metakey);
                            if(ignoreDiacritics) {
                                metaValue = stripDiacriticsFromText(metaValue, onlyOrExcept, selectedDiacritics);
                            }
                            rowData.add(metaValue);
                        }
                    }

                    retVal.addRow(rowData.toArray());
                }
            } catch (IOException e) {
                throw new ProcessingException(null, e);
            }
        }

        return retVal;
    }

    private static Set<String> collectTierNames(ResultSet[] results) {
        final Set<String> tierNames = new LinkedHashSet<>();
        // assuming all results come from the same query, the tiers should be the
        // same in every result value
        Arrays.asList(results).stream()
                .filter((rs) -> rs.numberOfResults(true) > 0)
                .findFirst()
                .ifPresent( firstNonEmptyResultSet -> {
                    final Result firstResult = firstNonEmptyResultSet.getResult(0);
                    for(ResultValue rv:firstResult) {
                        tierNames.add(rv.getName());
                    }
                });
        return tierNames;
    }

    private static Set<String> collectMetadataKeys(ResultSet[] results) {
        Set<String> metadataKeys = new LinkedHashSet<>();
        for(ResultSet rs:results) {
            metadataKeys.addAll(Arrays.asList(rs.getMetadataKeys()));
        }
        return metadataKeys;
    }

    /**
     * Setup result table with header based on parameters
     *
     * @param results
     * @param includeSessionInfo
     * @param includeSpeakerInfo
     * @param includeTierInfo
     * @param includeMetadata
     * @return result table with header but no data
     */
    public static DefaultTableDataSource setupTable(ResultSet[] results, boolean includeSessionInfo,
                                              boolean includeSpeakerInfo, boolean includeTierInfo, boolean includeMetadata) {
        final DefaultTableDataSource retVal = new DefaultTableDataSource();

        List<String> columnNames = new ArrayList<>();
        if(includeSessionInfo) {
            columnNames.add("Session");
            columnNames.add("Date");
        }

        if(includeSpeakerInfo) {
            columnNames.add("Speaker");
            columnNames.add("Age");
        }

        if(includeTierInfo) {
            columnNames.add("Record #");
            columnNames.add("Tier");
            columnNames.add("Range");
        }
        columnNames.add("Result");

        // collect all result value tier names
        final Set<String> tierNames = collectTierNames(results);
        columnNames.addAll(tierNames);

        Set<String> metadataKeys = collectMetadataKeys(results);
        if(includeMetadata) {
            columnNames.addAll(metadataKeys);
        }

        for(int i = 0; i < columnNames.size(); i++) {
            retVal.setColumnTitle(i, columnNames.get(i));
        }

        return retVal;
    }

    private static String stripDiacriticsFromText(String text, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if(keepCharacter(ch, onlyOrExcept, selectedDiacritics)) {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    private static boolean keepCharacter(char ch, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
        FeatureMatrix fm = FeatureMatrix.getInstance();
        Collection<Character> dias = fm.getCharactersWithFeature("diacritic");

        // don't strip ligatures
        dias.remove(Character.valueOf('\u035c'));
        dias.remove(Character.valueOf('\u0361'));
        dias.remove(Character.valueOf('\u0362'));

        if(dias.contains(ch)) {
            boolean inSet = selectedDiacritics.stream().filter( d -> d.getCharacter() == ch ).findFirst().isPresent();

            if(onlyOrExcept) {
                return !inSet;
            } else {
                return inSet;
            }
        } else {
            return true;
        }
    }

}
