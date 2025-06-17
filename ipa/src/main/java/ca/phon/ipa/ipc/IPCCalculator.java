package ca.phon.ipa.ipc;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.PhoneDimension;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;

/**
 * {@link IndexOfPhoneticComplexity} calculator.
 *
 * This class provides methods to calculate the Index of Phonetic Complexity (IPC)
 * defined by Jakielski (2022).
 *
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
    public IndexOfPhoneticComplexity calculate(IPATranscript ipa) {
        if (ipa.length() == 0) return new IndexOfPhoneticComplexity();

        // Place (D)
        int D = 0;
        for (IPAElement t : ipa) {
            if (t.getFeatureSet().hasFeature("dorsal")) {
                D++;
            }
        }

        // Manner (M)
        int M = 0;
        for (IPAElement t : ipa) {
            if (t.getFeatureSet().hasFeature("fricative") || t.getFeatureSet().hasFeature("affricate") || t.getFeatureSet().hasFeature("liquid")) {
                M++;
            }
        }

        // Vowels (V)
        int V = 0;
        for (IPAElement t : ipa) {
            if (t.getFeatureSet().hasFeature("v") && t.getFeatureSet().hasFeature("rhotic")) {
                V++;
            }
        }

        // Word shape (S)
        int S = ipa.elementAt(ipa.length() - 1).getFeatureSet().hasFeature("consonant") ? 1 : 0;

        // Word length (L)
        int numRealSylls = 0;
        for (IPATranscript syll : ipa.syllables()) {
            if (syll.contains("\\w:N"))
                numRealSylls++;
        }
        int L = numRealSylls >= 3 ? 1 : 0;

        // Place variegation (P)
        FeatureSet prevPlace = null;
        boolean lastWasConsonant = false;
        int P = 0;
        for (int i = 0; i < ipa.audiblePhones().length(); i++) {
            IPAElement t = ipa.audiblePhones().elementAt(i);
            if (t.getFeatureSet().hasFeature("consonant")) {
                if (lastWasConsonant) {
                    // cluster reset our place
                    prevPlace = null;
                } else {
                    if (i < ipa.audiblePhones().length() - 1) {
                        IPAElement next = ipa.audiblePhones().elementAt(i + 1);
                        if (next.getFeatureSet().hasFeature("consonant")) {
                            prevPlace = null;
                            lastWasConsonant = false;
                            ++i; // move past next non-consonant
                        } else {
                            FeatureSet currentPlace = FeatureSet.intersect(PhoneDimension.PLACE.getFeatures(), t.getFeatureSet());
                            if (prevPlace != null) {
                                if (!prevPlace.equals(currentPlace)) {
                                    P++;
                                }
                            }
                            prevPlace = currentPlace;
                        }
                    }
                }
                lastWasConsonant = true;
            } else {
                lastWasConsonant = false;
            }
        }


        // Contiguous consonants (C)
        int C = 0;
        int T = 0;
        String phonex = "\\c[\\s\\c]+";
        PhonexPattern pattern = PhonexPattern.compile(phonex);
        PhonexMatcher matcher = pattern.matcher(ipa);
        while (matcher.find()) {
            C++;
        }

        // Cluster type (T)
        for (IPATranscript syll : ipa.syllables()) {
            if (syll.length() > 2) {
                FeatureSet lastPlace = null;
                for (IPAElement t : syll.audiblePhones()) {
                    if (t.getFeatureSet().hasFeature("consonant")) {
                        FeatureSet currentPlace = FeatureSet.intersect(PhoneDimension.PLACE.getFeatures(), t.getFeatureSet());
                        if (lastPlace != null) {
                            if (!lastPlace.equals(currentPlace)) {
                                T++;
                                break;
                            }
                        }
                        lastPlace = currentPlace;
                    }
                }
            }
        }

        return new IndexOfPhoneticComplexity(D, M, V, S, L, P, C, T);
    }

}
