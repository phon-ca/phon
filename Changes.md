# Phon 1.7.0 Changes

## API Changes
 * New streamlined API, documentation available at https://www.phon.ca/phontrac/wiki/dev/
 * Source code available on GitHub at https://github.com/ghedlund/phon
 * Phon has been re-structured as several maven/ivy artifacts which can be integrated into other
 (JVM-based) software projects very easily.
 * Improved plug-in support

### IPA Support
 * New IPA parser and transcription reference.
 * Improved integration with phonex

### Phonex
 * New version 2.0 of Phonex
 * Support for many new constructs including:
	* Groups (capturing, non-capturing)
	* Back-references
	* Phone classes
	* Reluctant and possessive quantifiers
	* Plug-ins (custom matching methods)

## Session Editor
 * All views now have a context menu with actions available in that view
 * Improved undo support
 * Record Data
	* New group-aligned layout
 	* New buttons for group management
 * IPA Lookup
	* Easier handling of alternative IPA transcriptions
	* Improved Automatic Alignment
 	* System for transliteration (Arabic already implemented)
 * Syllabification & Alignment
 	* New settings for selecting syllabifier based on tier
	* New buttons for re-setting syllabifiation and/or alignment
 	* Added syllabification algorithms
		* Arabic
		* Berber
		* Swedish
		* Polish
	* Improvements to existing algorithms
		* English
		* Dutch
		* French
	* Support for geminates
 * Find & Replace
	* New Find & Replace system
 	* Supports plain text, regular expression, and phonex queries in multiple tiers
	* Support for regex/phonex group references in replace expression

## Praat integration
 * Provided by new jpraat java library (Java Native Architecture (JNA) wrapper for praat)
 * Generate TextGrids from Phon records
 * Import TextGrids as Phon records
 * Visualization of Praat data inside Waveform view
	* Spectrogram
	* Formant structure
	* Pitch
	* Intensity
 * TextGrid visualization
 * Acoustic data measurement via query and reporting system
	* Formant, Pitch, and Intensity listings
	* More to come..
 * Send information to Praat via Praat scripts and return data to Phon
 
## User Interface
 * Uniform theme across all operating systems
 * Text edit context menus for all text fields
 * Added PhonShell plug-in which provides a scripting environment for
 every window.
 * New Buffers window
	* View text output from PhonShell and query scripts
	* View CSV formatted text as a table
