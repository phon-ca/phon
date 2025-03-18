# Multisyllabic Nonlinear Analysis

This document describes the Multisyllabic Nonlinear Analysis (MNA) report in Phon.

## Information

TODO - Information about analysis and citations which will be included at the top of the report

## Organization

The report will consist of two types of tables:

 * MNA Production - breakdown of each word sampled
 * MNA Summary - summary of all words sampled
 
An example tables of contents is displayed below. Bold level elements are
section headers, italic items are one of the tables listed above.  Other 
headings are standard Phon report elements or text blocks.

 * **Multisyllabic Nonlinear Analysis**
    * Information
    * Parameters
    * **Summary**
        - *MNA Summary*
    * **Breakdown**
        - **Word /IPA Target/** (Word 1)
            - *MNA Production* (Production 1)
            - *MNA Production* (Production 2)
            - ...
        - **Word /IPA Target/** (Word 2)
        - ...

## Data Preparation

Session data is queried by word and a WAP Table is produced for each Orthography, IPA Target, and IPA Actual triad. Proper syllabification and phone alignment is required for accurate results.  All participants should have unique names/identification numbers and a speaker should be assigned to all records.

## Parameters

Parameters for the initial *Data Tiers* query will be available.  The user may modify the query parameters to adjust the set of words sampled.

## MNA Production Table

The *MNA Production* tables provide details for each word sampled.  The table has four main categories: *Stress*, *Syllable*, *Phone/Timing Unit*, and *Feature*.  Caption for the table will be "/IPA Actual/".

Example

**/ˌkʰæːʃ̟ˈwɛʒ̙əʃ̙̟ʊː/**

| Category | Description | Value | Total |
| --- | --- | --- | --- |
| Stress | PrimaryStress mis-match | 1 |  |
|  | SecondaryStress mis-match | 1 |  |
|  | Word stress mis-match: 12UU ↔ 21UU | 1 |  |
|  | Stress subtotal |  | 3 |
| Syllable | Syllable subtotal |  | 0 |
| Phone/Timing Unit | Phone 9 deleted: t ↔ ∅ | 1 |  |
|  | Phone/Timing Unit subtotal |  | 1 |
| Feature | Place substitution {alveolar} ↔ {labial, -dental}: ɹ ↔ w | 1 |  |
|  | Manner substitution {rhotic} ↔ {glide}: ɹ ↔ w | 1 |  |
|  | Manner substitution {affricate} ↔ {fricative}: ʤ ↔ ʒ̙ | 1 |  |
|  | Height substitution {high} ↔ {mid}: ɪ ↔ ə | 1 |  |
|  | Backness substitution {front} ↔ {central}: ɪ ↔ ə | 1 |  |
|  | Place substitution {alveolar} ↔ {alveopalatal}: s ↔ ʃ̙̟ | 1 |  |
|  | Height substitution {mid} ↔ {high}: ɚ ↔ ʊː | 1 |  |
|  | Backness substitution {central} ↔ {back}: ɚ ↔ ʊː | 1 |  |
|  | …ʃ…ʤ… → …ʃ̟…ʒ̙… Progressive Harmony Manner | 1 |  |
|  | Feature subtotal |  | 9 |
| Total |  |  | 13 |

### Stress

The *Stress* category includes three checks, 1 point is added for each check that fails.

 1. Primary Stress match
 1. Secondary Stress match
 1. Stress pattern match (full-word)
 
### Syllable

The *Syllable* category will add a point for each deleted or inserted syllable.

### Phone/Timing Unit

The *Phone/Timing Unit* category will add a point for each delete or inserted phone.  
### Feature

The *Feature* category will displays substitutions found in the phone alignment. A point is added for substituted dimension found.  Consonant dimensions include: place, manner, voicing.  Vowel dimensions included: height, backness, tenseness (rounding is included for schwa.)  Deleted/inserted phones are not included.

Any *Segmental Relations* (e.g., haromny/metathesis) detected will be also be displayed in this category.  The value added for any segmental relation will be the number of dimensions (i.e., place/manner/voicing) involved in the relation.

## MNA Summary Table

The *MNA Summary* table displays totals for each word sampled.

Example

| Word | Productions | Stress Subtotal | Syllable Subtotal | Phone/Timing Unit Subtotal | Feature Subtotal | Total |
| --- | --- | --- | --- | --- | --- | --- |
| alligator | 2 | 0.0 | 0.0 | 0.5 | 3.0 | 3.5 |
| animal | 2 | 0.5 | 0.5 | 1.5 | 4.5 | 7.0 |
| balloons | 2 | 1.0 | 0.5 | 0.5 | 3.0 | 5.0 |
| cashregister | 4 | 1.0 | 0.25 | 1.25 | 7.0 | 9.5 |
| computer | 2 | 0.0 | 0.0 | 0.5 | 3.0 | 3.5 |
| electric | 4 | 0.5 | 0.0 | 0.5 | 4.75 | 5.75 |
| explodes | 3 | 0.0 | 0.0 | 1.67 | 4.33 | 6.0 |
| giraffe | 2 | 2.0 | 1.0 | 1.5 | 2.0 | 6.5 |
| gorilla | 2 | 1.5 | 0.5 | 0.5 | 3.5 | 6.0 |
| guitar | 2 | 1.0 | 0.0 | 0.5 | 5.0 | 6.5 |
| hippopotamus | 2 | 1.0 | 0.0 | 0.5 | 3.5 | 5.0 |
| hospital | 2 | 0.0 | 0.0 | 0.5 | 7.0 | 7.5 |
| invitation | 3 | 1.0 | 0.33 | 1.67 | 5.0 | 8.0 |
| magician | 2 | 1.0 | 0.0 | 0.5 | 8.0 | 9.5 |
| mosquito | 2 | 0.0 | 0.0 | 1.0 | 4.0 | 5.0 |
| skeleton | 2 | 0.5 | 0.5 | 0.5 | 4.0 | 5.5 |
| thermometer | 3 | 0.33 | 0.33 | 1.33 | 4.67 | 6.67 |
| umbrella | 2 | 0.0 | 0.0 | 0.0 | 1.5 | 1.5 |
| vegetable | 3 | 0.0 | 0.0 | 1.0 | 6.33 | 7.33 |
| watermelon | 3 | 0.67 | 0.67 | 1.0 | 4.33 | 6.67 |
