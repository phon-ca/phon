# Introduction

Phonex is a pattern matching language for IPA transcriptions. *Phonex* is used to query IPA transcriptions for sequences of phones based on both segmental and prosodic criteria.

Features include:

 * Query based on features. E.g., ```{fricative}```
 * Custom phone classes. E.g., ```\c```, ```\v```, ```\w``` (consonants, vowels, consonant or vowel respectively.)
 * Query based on constituent type.  E.g., ```{fricative}:C``` (fricative codas)
 * Query based on stress. E.g., ```\c!1``` (stressed consonants)

> Note: *regular expressions* are a powerful text searching tool that are available in many applications.  An understanding of *regular expressions* (or *regex*) is useful for understanding *phonex*. *Regular expression* guides can be found [here](http://www.google.com/search?q=regular+expressions+for+beginners).
