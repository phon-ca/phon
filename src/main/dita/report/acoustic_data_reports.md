# Acoustic Data Reports

Acoustic data reports print acoustic information for each query result.

All acoustic data reports require identification of one or more intervals in the audio for analysis. Intervals are selected using one of the following methods:

 * The full segment for the record of the query result
 * Intervals from a specific TextGrid tier
    * When using this option, a result line will be printed for each interval of the specified TextGrid tier which is included in the full record segement
    * TextGrid intervals may be filtered by their label using a regular expression
 * Intervals from a result column in the query result (typically a tier name)
    * TextGrid mappings must exist between the specified tier and a TextGrid tier
    * This is the default option with the tier name set as ```IPA Actual```
 
![../images/IntervalSelection.png](../images/IntervalSelection.png)

If an interval cannot be selected for a query result, no information will be printed in the report.
