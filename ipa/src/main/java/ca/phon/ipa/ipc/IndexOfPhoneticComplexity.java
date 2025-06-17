package ca.phon.ipa.ipc;

import ca.phon.ipa.IPATranscript;

/**
 * Index of phonetic complexity components.
 * Jakielski, Kathy J.. "The Index of Phonetic Complexity: At-a-Glance Scoring System, Terminology,
 * Instructions, & Data Forms" (2022). Building Speech and Quantifying Complexity.
 * https://digitalcommons.augustana.edu/csdbuildingspeech/10
 */
public record IndexOfPhoneticComplexity(
        /** Place (D) - dorsals */
        int D,

        /** Manner (M) - fricatives, affricates, and liquids */
        int M,

        /** Vowels (V) - Rhotic vowels */
        int V,

        /** Word shape (S) - ends with a consonant */
        int S,

        /** Word length (L) - 3 or more syllables */
        int L,

        /** Place variegation (P) - singleton consonants that are place variegated */
        int P,

        /** Contiguous clusters (C) */
        int C,

        /** Cluster type (T) - Each cluster that is comprised of segments that vary in place
         (CCV) gets 1 point for being heterorganic. */
        int T
) {
    /**
     * Calculate the Index of Phonetic Complexity from an IPATranscript.
     *
     * @param ipa the IPATranscript to calculate the index from
     * @return an IndexOfPhoneticComplexity object representing the calculated index
     */
    public static IndexOfPhoneticComplexity FromTranscript(IPATranscript ipa) {
        return IPCCalculator.calculate(ipa);
    }

    /**
     * Default constructor for the Index of Phonetic Complexity. Zero scores for all components.
     */
    public IndexOfPhoneticComplexity() {
        this(0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Constructor for the Index of Phonetic Complexity with specified scores for each component.
     *
     * @param D Place score (dorsals)
     * @param M Manner score (fricatives, affricates, and liquids)
     * @param V Vowel score (rhotic vowels)
     * @param S Word shape score (ends with a consonant)
     * @param L Word length score (3 or more syllables)
     * @param P Place variegation score (singleton consonants that are place variegated)
     * @param C Contiguous clusters score
     * @param T Cluster type score (heterorganic clusters)
     */
    public IndexOfPhoneticComplexity(int D, int M, int V, int S, int L, int P, int C, int T) {
        this.D = D;
        this.M = M;
        this.V = V;
        this.S = S;
        this.L = L;
        this.P = P;
        this.C = C;
        this.T = T;
    }

    /**
     * Calculate the total score for the index of phonetic complexity.
     *
     * @return the total score
     */
    public int totalScore() {
        return D + M + V + S + L + P + C + T;
    }

    /**
     * Difference between two IndexOfPhoneticComplexity objects.
     *
     * @param other the other IndexOfPhoneticComplexity object
     *
     * @return a new IndexOfPhoneticComplexity object representing the difference
     */
    public IndexOfPhoneticComplexity difference(IndexOfPhoneticComplexity other) {
        return new IndexOfPhoneticComplexity(
                this.D - other.D,
                this.M - other.M,
                this.V - other.V,
                this.S - other.S,
                this.L - other.L,
                this.P - other.P,
                this.C - other.C,
                this.T - other.T
        );
    }

}
