# Phon 2.2 Changes

## Changes from Phon 2.1.8

### General

 * The 'Workspace' window has been renamed 'Welcome' and now includes a list of recently used projects
 * Added support for additional IPA characters
 * New phonex flag '/o' for allowing overlapping queries
 * Improved media file suggestions when typing in media location field

### TextGrids

 * The first location for locating TextGrid files is now as a sibling of the media file with the same basename and a .TextGrid extension.
 * The default name for TextGrid files is now the media file basename and not the session name
 * TextGrids no longer generate '#' intervals between groups
 * TextGrid generation now includes empty intervals when gaps exist between records
 * TextGrid generation can now add tiers to existing TextGrids
 * New TextGrid tier management window for renaming, ordering, and deleting TextGrid tiers.

### Composer

 * New UI for creating Query Reports and Analyses
 * Reports are creating through a visual editor displaying operations as nodes in a graph.
 * A 'simple' composer is available for quick development of a 'macro' Query Report/Analysis using existing documents

### Queries & Reporting

 * Word List, Word March, PCC, and PMLU queries have been moved to the analysis menu
 * Query Reports have been redesigned using Composer.  The following Query Reports are available:
  * Query Information - general information about the query
  * Aggregate (IPA results) - aggregate inventory of results based on IPA Target <-> IPA Actual pairs, organized by session and sorted phonetically
  * Aggregate (non-IPA results) - aggregate inventory of results based, organized by session and sort alphabetically
  * Listing - listing of all results
  * Transcript Variability - # repeated, # correct, # one or more correct, # same error, # different errors, average Levenshtein distance for IPA Target <-> IPA Actual pairs in query results
  * Phone Dispersion
  * Acoustic Data Reports: Duration, Pitch, Intensity, and Formants

### Analyses

 * New 'Analysis' menu for generating longitudinal reports of session data, organized by participant.  The following analyses are available:
  * PCC & PVC
  * Phone Inventory
  * Phonological Processes: Coronal Backing, Deaffrication, Deletion, Devoicing Glottalization, Lateralization, Liquid Gliding & Vocalization, Stopping, Velar Fronting, Voicing
  * PMLU
  * Word List
  * Word Match