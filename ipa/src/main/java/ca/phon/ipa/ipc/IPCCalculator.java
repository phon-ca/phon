package ca.phon.ipa.ipc;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.PhoneDimension;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;

/**
 * {@link IndexOfPhoneticComplexity} calculator.
 * <p>
 * This class provides methods to calculate the Index of Phonetic Complexity (IPC)
 * defined by Jakielski (2022).
 * <p>
 * The IPC is a measure of phonetic complexity based on various components such as
 * place, manner, vowels, word shape, word length, place variegation, contiguous clusters,
 * and cluster type.
 */
public class IPCCalculator {

    /**
     * Calculate the {@link IndexOfPhoneticComplexity} score based on the provided transcript.
     *
     * @param ipa the transcript to analyze
     * @return index of phonetic complexity
     */
    public static IndexOfPhoneticComplexity calculate(IPATranscript ipa) {
        if (ipa.length() == 0) return new IndexOfPhoneticComplexity();

        final StringBuffer explanation = new StringBuffer("Index of Phonetic Complexity: ");

        // Place (D)
        int D = 0;
        explanation.append("\n\tD=");
        for (IPAElement t : ipa) {
            if (t.featureSet().hasFeature("dorsal")) {
                if (D > 0) {
                    explanation.append(" +");
                }
                explanation.append(" " + t.toString());
                D++;
            }
        }
        explanation.append(" = " + D);

        // Manner (M)
        int M = 0;
        explanation.append("\n\tM=");
        for (IPAElement t : ipa) {
            if (t.featureSet().hasFeature("fricative") || t.featureSet().hasFeature("affricate") || t.featureSet().hasFeature("liquid")) {
                if (M > 0) {
                    explanation.append(" +");
                }
                explanation.append(" " + t.toString());
                M++;
            }
        }
        explanation.append(" = " + M);

        // Vowels (V)
        int V = 0;
        explanation.append("\n\tV=");
        for (IPAElement t : ipa) {
            if (t.featureSet().hasFeature("v") && t.featureSet().hasFeature("rhotic")) {
                if (V > 0) {
                    explanation.append(" +");
                }
                explanation.append(" " + t.toString());
                V++;
            }
        }
        explanation.append(" = " + V);

        // Word shape (S)
        int S = ipa.elementAt(ipa.length() - 1).featureSet().hasFeature("consonant") ? 1 : 0;
        explanation.append("\n\tS=");
        if (S > 0) {
            explanation.append(" ends with consonant");
        } else {
            explanation.append(" does not end with consonant");
        }
        explanation.append(" = " + S);

        // Word length (L)
        int numRealSylls = 0;
        explanation.append("\n\tL=");
        for (IPATranscript syll : ipa.syllables()) {
            if (syll.contains("\\w:N")) {
                if (numRealSylls > 0) {
                    explanation.append(" +");
                }
                explanation.append(" " + syll.toString());
                numRealSylls++;
            }
        }
        int L = numRealSylls >= 3 ? 1 : 0;
        explanation.append(" = " + L);

        // Place variegation (P)
        FeatureSet prevPlace = null;
        boolean lastWasConsonant = false;
        int P = 0;
        explanation.append("\n\tP=");
        StringBuilder placeVariegationExplanation = new StringBuilder();
        for (int i = 0; i < ipa.audiblePhones().length(); i++) {
            IPAElement t = ipa.audiblePhones().elementAt(i);
            if (t.featureSet().hasFeature("consonant")) {
                if (lastWasConsonant) {
                    // cluster reset our place
                    prevPlace = null;
                } else {
                    if (i < ipa.audiblePhones().length() - 1) {
                        IPAElement next = ipa.audiblePhones().elementAt(i + 1);
                        if (next.featureSet().hasFeature("consonant")) {
                            prevPlace = null;
                            lastWasConsonant = false;
                            ++i; // move past next non-consonant
                        } else {
                            FeatureSet currentPlace = FeatureSet.intersect(PhoneDimension.PLACE.getPrimaryFeatures(), t.featureSet());
                            if (prevPlace != null) {
                                if (!prevPlace.equals(currentPlace)) {
                                    placeVariegationExplanation.append(" -> " + t.toString());
                                    placeVariegationExplanation.append(currentPlace.toString());

                                    if (P > 0) {
                                        explanation.append(" +");
                                    }
                                    explanation.append(placeVariegationExplanation.toString());
                                    P++;
                                }
                            }
                            prevPlace = currentPlace;
                            placeVariegationExplanation.setLength(0);
                            placeVariegationExplanation.append(" " + t.toString());
                            placeVariegationExplanation.append(prevPlace.toString());
                        }
                    }
                }
                lastWasConsonant = true;
            } else {
                lastWasConsonant = false;
            }
        }
        if (P > 0) {
            explanation.append(" = " + P);
        } else {
            explanation.append(" no place variegation");
        }


        // Contiguous consonants (C)
        int C = 0;
        StringBuilder contiguousExplanation = new StringBuilder();
        contiguousExplanation.append("\n\tC=");
        int T = 0;
        StringBuilder clusterTypeExplanation = new StringBuilder();
        clusterTypeExplanation.append("\n\tT=");
        String phonex = "\\c[\\s\\c]+";
        PhonexPattern pattern = PhonexPattern.compile(phonex);
        PhonexMatcher matcher = pattern.matcher(ipa);
        while (matcher.find()) {
            if (C > 0) {
                contiguousExplanation.append(" +");
            }
            contiguousExplanation.append(" " + (new IPATranscript(matcher.group())).toString());
            C++;

            // if place variegation is present, increment T
            prevPlace = null;
            for (var ele : matcher.group()) {
                if (ele.featureSet().hasFeature("consonant")) {
                    FeatureSet currentPlace = FeatureSet.intersect(PhoneDimension.PLACE.getPrimaryFeatures(), ele.featureSet());
                    if (prevPlace != null) {
                        if (!prevPlace.equals(currentPlace)) {
                            if (T > 0) {
                                clusterTypeExplanation.append(" +");
                            }
                            clusterTypeExplanation.append(" " + (new IPATranscript(matcher.group())).toString() + " (place variegation)");
                            T++;
                            break;
                        }
                    }
                    prevPlace = currentPlace;
                }
            }

            // if cluster is heterosyllabic, increment T
            int lastSyllableIdx = -1;
            for (var ele : matcher.group()) {
                if (ele.featureSet().hasFeature("consonant")) {
                    int currentSyllableIdx = ipa.syllableIndexOf(ele);
                    if (lastSyllableIdx == -1) {
                        lastSyllableIdx = currentSyllableIdx;
                    } else if (lastSyllableIdx != currentSyllableIdx) {
                        if (T > 0) {
                            clusterTypeExplanation.append(" +");
                        }
                        clusterTypeExplanation.append(" " + (new IPATranscript(matcher.group())).toString() + " (heterosyllabic)");
                        T++;
                        break;
                    }
                }
            }
        }
        if (C > 0) {
            explanation.append(contiguousExplanation.toString());
            explanation.append(" = " + C);
        } else {
            explanation.append("\n\tC= no contiguous consonants");
        }
        if (T > 0) {
            explanation.append(clusterTypeExplanation.toString());
            explanation.append(" = " + T);
        } else {
            explanation.append("\n\tT= no cluster type variegation");
        }

        explanation.append("\n\t" + "Total IPC = " + (D + M + V + S + L + P + C + T));

        return new IndexOfPhoneticComplexity(D, M, V, S, L, P, C, T, explanation.toString());
    }

}
