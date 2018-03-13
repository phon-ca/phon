# Report: Phone Similarity

Phone similarity measures how similar two phones or strings of phones are within a target-actual aligned pair based on the number of descriptive phonological matchings divided by the maximal number of potential matches.

## Calculation

Given a pair of aligned phones, simlarity is calculated as:

```
 # of matched features
 -----------------------------------------  * 100
 max(# target features, # actual features)
```

## Parameters

The number of features considered in the calculation depends on the dimensions selected during execution of the analysis.  The dimensions available for consonants are:

 * Place
 * Manner
 * Voicing
 
and for vowels:

 * Height
 * Backness
 * Tenseness
 * Rounding
 
## Example

The table produced by this report will have the following columns:

 * Session
 * Speaker
 * Record #
 * IPA Target
 * IPA Actual
 * Alignment
 * # Features Compared
 * Similarity

| Session | Speaker | Record # | IPA Target | IPA Actual | Alignment | # Features Compared | Similarity |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Anne.Session | Anne | 2 | s | s | s↔s | 8 | 1.0 |
| Anne.Session | Anne | 2 | ɪ | ɪː | ɪ↔ɪː | 4 | 1.0 |
| Anne.Session | Anne | 2 | ŋ | ∅ | ŋ↔∅ | 7 | 0.0 |
| Anne.Session | Anne | 2 | k | k | k↔k | 7 | 1.0 |
| Anne.Session | Anne | 2 | o | o | o↔o | 5 | 1.0 |
| Anne.Session | Anne | 2 | ʊ | ʊ | ʊ↔ʊ | 5 | 1.0 |
| Anne.Session | Anne | 2 | l | l | l↔l | 9 | 1.0 |
| Anne.Session | Anne | 2 | d | d | d↔d | 8 | 1.0 |
| Anne.Session | Anne | 2 | n | n | n↔n | 8 | 1.0 |
| Anne.Session | Anne | 2 | ɛ | ɛ | ɛ↔ɛ | 4 | 1.0 |
| Anne.Session | Anne | 2 | v | v | v↔v | 7 | 1.0 |
| Anne.Session | Anne | 2 | ə | ə | ə↔ə | 4 | 1.0 |
| Anne.Session | Anne | 2 | ɹ | ɹ | ɹ↔ɹ | 9 | 1.0 |
| Anne.Session | Anne | 2 | b | b | b↔b | 7 | 1.0 |
| Anne.Session | Anne | 2 | ɑ | ɑ | ɑ↔ɑ | 4 | 1.0 |
| Anne.Session | Anne | 2 | ð | ð | ð↔ð | 8 | 1.0 |
| Anne.Session | Anne | 2 | ə | ə | ə↔ə | 4 | 1.0 |
| Anne.Session | Anne | 2 | ɹ | ɹ | ɹ↔ɹ | 9 | 1.0 |
| Anne.Session | Anne | 2 | d | d | d↔d | 8 | 1.0 |
| Anne.Session | Anne | 2 | m | m | m↔m | 7 | 1.0 |
| Anne.Session | Anne | 2 | iː | iː | iː↔iː | 4 | 1.0 |
| Anne.Session | Anne | 2 | t | t | t↔t | 8 | 1.0 |
| Anne.Session | Anne | 2 | uː | uː | uː↔uː | 5 | 1.0 |
| Anne.Session | Anne | 2 | j | j | j↔j | 9 | 1.0 |
| Anne.Session | Anne | 2 | uː | uː | uː↔uː | 5 | 1.0 |
| Anne.Session | Anne | 2 | h | ∅ | h↔∅ | 7 | 0.0 |
| Anne.Session | Anne | 2 | ɔ | ɔː | ɔ↔ɔː | 5 | 1.0 |
| Anne.Session | Anne | 2 | ɹ | z | ɹ↔z | 9 | 0.67 |
| Anne.Session | Anne | 2 | s | ∅ | s↔∅ | 8 | 0.0 |
| Anne.Session | Anne | 2 | iː | iː | iː↔iː | 4 | 1.0 |
| Anne.Session | Anne | 4 | j | j | j↔j | 9 | 1.0 |
| Anne.Session | Anne | 4 | æ | ɛ | æ↔ɛ | 4 | 0.75 |
| Anne.Session | Anne | 7 | tʰ | tʰ | tʰ↔tʰ | 9 | 1.0 |
| Anne.Session | Anne | 7 | uː | uː | uː↔uː | 5 | 1.0 |
| Anne.Session | Anne | 9 | j | j | j↔j | 9 | 1.0 |
| Anne.Session | Anne | 9 | æ | ɛ | æ↔ɛ | 4 | 0.75 |
| Anne.Session | Anne | 9 | o | ∅ | o↔∅ | 5 | 0.0 |
| Anne.Session | Anne | 9 | ʊ | ʊː | ʊ↔ʊː | 5 | 1.0 |
| Anne.Session | Anne | 9 | n | n | n↔n | 8 | 1.0 |
| Anne.Session | Anne | 9 | l | ∅ | l↔∅ | 9 | 0.0 |
| Anne.Session | Anne | 9 | iː | iː | iː↔iː | 4 | 1.0 |
| Anne.Session | Anne | 9 | ʃ | ∅ | ʃ↔∅ | 8 | 0.0 |
| Anne.Session | Anne | 9 | iː | ɪː | iː↔ɪː | 4 | 0.75 |
| Anne.Session | Anne | 9 | z | ∅ | z↔∅ | 8 | 0.0 |
| Anne.Session | Anne | 9 | b | b | b↔b | 7 | 1.0 |
| Anne.Session | Anne | 9 | ɪ | ɪ | ɪ↔ɪ | 4 | 1.0 |
| Anne.Session | Anne | 9 | ɡ | ɡ | ɡ↔ɡ | 7 | 1.0 |
| Anne.Session | Anne | 9 | ɡ | ɡ | ɡ↔ɡ | 7 | 1.0 |
| Anne.Session | Anne | 9 | ʌ | ʌ | ʌ↔ʌ | 4 | 1.0 |
| Anne.Session | Anne | 9 | ɹ | ɹ | ɹ↔ɹ | 9 | 1.0 |
| Anne.Session | Anne | 9 | l | l | l↔l | 9 | 1.0 |
| Anne.Session | Anne | 11 | j | j | j↔j | 9 | 1.0 |
| Anne.Session | Anne | 11 | æ | æ | æ↔æ | 4 | 1.0 |
| Anne.Session | Anne | 14 | j | j | j↔j | 9 | 1.0 |
| Anne.Session | Anne | 14 | æ | æ | æ↔æ | 4 | 1.0 |
| Anne.Session | Anne | 17 | ð | d | ð↔d | 8 | 0.75 |
| Anne.Session | Anne | 17 | ə | ə | ə↔ə | 4 | 1.0 |
| Anne.Session | Anne | 17 | b | b | b↔b | 7 | 1.0 |
| Anne.Session | Anne | 17 | ʌ | ʌ | ʌ↔ʌ | 4 | 1.0 |
| Anne.Session | Anne | 17 | ɹ | ∅ | ɹ↔∅ | 9 | 0.0 |
| Anne.Session | Anne | 17 | θ | t | θ↔t | 8 | 0.75 |
| Anne.Session | Anne | 17 | d | ∅ | d↔∅ | 8 | 0.0 |
| Anne.Session | Anne | 17 | e | e | e↔e | 4 | 1.0 |
| Anne.Session | Anne | 17 | ɪ | ∅ | ɪ↔∅ | 4 | 0.0 |
| Anne.Session | Anne | 17 | k | k | k↔k | 7 | 1.0 |
| Anne.Session | Anne | 17 | e | e | e↔e | 4 | 1.0 |
| Anne.Session | Anne | 17 | ɪ | ɪ | ɪ↔ɪ | 4 | 1.0 |
| Anne.Session | Anne | 17 | k | k˺ | k↔k˺ | 7 | 1.0 |
| Anne.Session | Anne | 19 | j | j | j↔j | 9 | 1.0 |
| Anne.Session | Anne | 19 | æ | æ | æ↔æ | 4 | 1.0 |
| Anne.Session | Anne | 19 | s | s | s↔s | 8 | 1.0 |
| Anne.Session | Anne | 19 | ʌ | ɵ | ʌ↔ɵ | 5 | 0.4 |
| Anne.Session | Anne | 19 | m | m | m↔m | 7 | 1.0 |
| Anne.Session | Anne | 19 | ɡ | ∅ | ɡ↔∅ | 7 | 0.0 |
| Anne.Session | Anne | 19 | ɹ | d | ɹ↔d | 9 | 0.67 |
| Anne.Session | Anne | 19 | æ | ɜ | æ↔ɜ | 4 | 0.5 |
| Anne.Session | Anne | 19 | s | s | s↔s | 8 | 1.0 |
