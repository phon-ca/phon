# Query: Phones

The __Phones__ query is used to query data contained within IPA Target and IPA Actual tiers.

![Query : Phones](../images/Query_Phones.png)

## Parameters

 * Search Tier: IPA Target or IPA Actual.
 * Search by: Group, Word and optionally by syllable. This options defines the search domain for the query.
 * Expression type: Plain text, Regular Expression, Phonex, Stress Pattern, CGV Pattern
 * Expression: query expression
    * Case sensitive (not applicable for phonex expressions)
    * Exact match
    * Allow overlapping matches (phonex only)

### Aligned Phones

The phones aligned with the queried phones can be added to query results. If 'include aligned phones' is selected the aligned results may be filtered using the __IPA Target Matcher__ and __IPA Actual Matcher__ expressions.


### Other Parameters

 * [Group Options](./query_group_options.html)
 * [Word Options](./query_word_options.html)
 * [Syllable Options](./query_syllable_options.html)
 * [Participant Filter](./query_participant_filter.html)
