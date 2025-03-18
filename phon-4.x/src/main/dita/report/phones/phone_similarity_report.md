# Phone Similarity

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
 * #Fs Place/Manner/...
 * #Sim Place/Manner/...
 * Total Fs
 * Similarity

| Session | Speaker | Age | Record # | IPA Target | IPA Actual | #Fs Place | Sim Place | #Fs Manner | Sim Manner | #Fs Voicing | Sim Voicing | #Fs Height | Sim Height | #Fs Backness | Sim Backness | #Fs Tenseness | Sim Tenseness | #Fs Rounding | Sim Rounding | #Fs | Sim |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Catootje.1_11_09 | Catootje | 01;11.09 | 1 | ə | ∅ | 0 | 0 | 1 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 4 | 0 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 1 | n | nː | 3 | 100 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 8 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 1 | ɦ | ɦ | 2 | 100 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 1 | œ | æ | 0 | 0 | 1 | 100 | 0 | 0 | 1 | 0 | 1 | 100 | 1 | 100 | 1 | 0 | 5 | 60 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 1 | y | i | 0 | 0 | 1 | 100 | 0 | 0 | 1 | 100 | 1 | 100 | 1 | 100 | 1 | 0 | 5 | 80 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 1 | s | s | 3 | 100 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 8 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 2 | ə | ∅ | 0 | 0 | 1 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 4 | 0 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 2 | n | mː | 3 | 0 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 8 | 62.5 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 2 | b | b | 2 | 100 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 2 | e | eː | 0 | 0 | 1 | 100 | 0 | 0 | 1 | 100 | 1 | 100 | 1 | 100 | 0 | 0 | 4 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 2 | ʃ̟ | s̪ | 3 | 33.333 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 8 | 75 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 2 | ə | a | 0 | 0 | 1 | 100 | 0 | 0 | 1 | 0 | 1 | 100 | 1 | 100 | 0 | 0 | 4 | 75 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 3 | b | b | 2 | 100 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 3 | e | eː | 0 | 0 | 1 | 100 | 0 | 0 | 1 | 100 | 1 | 100 | 1 | 100 | 0 | 0 | 4 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 3 | ʃ̟ | s̪ | 3 | 33.333 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 8 | 75 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 3 | ə | ə | 0 | 0 | 1 | 100 | 0 | 0 | 1 | 100 | 1 | 100 | 1 | 100 | 0 | 0 | 4 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 3 | b | b | 2 | 100 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 3 | u | u | 0 | 0 | 1 | 100 | 0 | 0 | 1 | 100 | 1 | 100 | 1 | 100 | 1 | 100 | 5 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 3 | k | k | 2 | 100 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 4 | b | p | 2 | 100 | 4 | 100 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 85.714 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 4 | u | u | 0 | 0 | 1 | 100 | 0 | 0 | 1 | 100 | 1 | 100 | 1 | 100 | 1 | 100 | 5 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 4 | k | k | 2 | 100 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 5 | p | p | 2 | 100 | 4 | 100 | 1 | 100 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 100 |
| Catootje.1_11_09 | Catootje | 01;11.09 | 5 | u | u | 0 | 0 | 1 | 100 | 0 | 0 | 1 | 100 | 1 | 100 | 1 | 100 | 1 | 100 | 5 | 100 |