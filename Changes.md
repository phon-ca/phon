# Phon 2.0 Changes

## Changes from Phon 2.0.0

### General 
 * Moved PCC-PVC to Tools -> Assessments
 * Added new assessment 'Word Match' to the Tools menu
 * Added new assessment 'PMLU' to the tools menu
 * Added Japanese (jpn) transliteration dictionary
 * Changed background color for many panels from blue to gray
 * Changed selection highlight colours (Mac OS X) from orange to blue
 * Changed keystrokes for record navigation
 * Inter-word '(.)' pauses are now considered part of the set of aligned elements in tiers
 * Splash screen will now stay visible until the Workspace window appears (Windows)

### Bugs
 * Fixed an issue where query form was not displayed when a script was loaded after the editor was open
 * Phon manual is now accessible from the Help menu
 * Added a check to the waveform exporter so that it will not overwrite the source media file
 * Added a warning to the waveform exporter if about to overwrite an existing file
 * Participant age calculation will now work if age for the participant has already been specified
 * Fixed an issue where group functions (split group, merge group, etc.) would not work properly if tier data was not committed first
 * Session editor will now move to the next record after replacing a segment using the 'Replace current segment' mode for segmentation
 * Fixed transliteration dictionary for Arabic
 * Fixed 'Formant listing' query script so that table header is printed for each session selected
 * Punctuation in Orthography will no longer export as a word in TextGrid exports
 
## Changes from Phon 1.6.2

### User Interface
 * New icon and splash screen
 * New look and feel with ability to select fonts used in UI elements.
	* Set default font for UI controls to Liberation Sans-PLAIN-12
	* Set default font for IPA tiers to Charis SIL-PLAIN-12
 * New menu item File -> Open Project (CMD+O)
 * Removed 'Recent Projects' menu
 * Added new workspace menu with actions for selecting current workspace, viewing the workspace window and opening
 a workspace project quickly.
 * Added PhonShell plug-in which provides a scripting environment for
 every window.  For more information see https://www.phon.ca/phontrac/wiki/dev
 * New Buffers window used for reporting data
	* View text output from PhonShell and query scripts
	* View CSV formatted text as a table
 
#### Project Manager
 * New menu item Project -> Anonymize Participant Information
 * Sessions can now be copied/moved to other project within the workspace using the context menu.

#### Session Editor
 * All actions are now undo-able.
 * Editor views may now be externalized into a new editor window.
 * Window position(s) and size are now saved with layout views.
 * Menus for each view can now be accessed using the View menu.
 * Added menu items for moving records.
 * Session files will be copied to a zip, 'backups.zip', inside the project folder every time the session is saved.  This
 behaviour can be controlled through an option in the Preferences dialog.
 * Toolbar search field now supports record number queries, including ranges.  E.g., the string '2,5..10' will display records 2 and 5 through
 10 in the search table.  This may be combined with other queries by separating query string using a semi-colon.
 
##### Session Information
 * Added button to delete selected participant
 * New participant editor.  Name, age and birthdays for participants are now optional and are only calculated when requested.
 * Added calendar drop-down control to date field.
  
##### Find & Replace
 * New Find & Replace system
 * Supports plain text, regular expression, and phonex queries in multiple tiers
 * Support for regex/phonex group references in replace expression
 
##### IPA Lookup
 * Improved IPA Lookup view allowing more fine-grained editing of automatic transcriptions
 * System for transliteration (Arabic already implemented)
 * Automatic transcription now works for blind transcribers
 
##### Record Data
 * Record data is now aligned by groups by default.  This can be changed using buttons in the toolbar.
 * New record number field in toolbar for setting the current record's position in the session.
 * New actions for group management avaiable from the View->Record Data menu or the Record Data view toolbar.
	* New Group (CMD/CTRL+G)
	* Split Group (CMD/CTRL+K)
	* Merge Groups (CMD/CTRL+SHIFT+G)
	* Remove Group (CMD/CTRL+SHIFT+ALT+G)

##### Syllabifiation & Alignment
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
 
##### Speech Analysis
 * The Waveform view has been renamed to Speech Analysis
 * 'Space' will now play the audio segment/selection
 * Added a new Praat button to the toolbar for Praat integration.  See '''Praat Integration''' below.
 
##### IPA Validation
 * Improved validation UI using new tier layout.

#### Queries & Reporting
 * Participant filter in Query scripts now includes an option for filtering records by participant role.
 * Reports are now genereated inside a buffer which can then be viewed as a table and/or saved to disk.

### Praat integration
 * Provided by new jpraat java library (Java Native Architecture (JNA) wrapper for Praat.)  For more information see
 https://github.com/ghedlund/jraat and https://github.com/ghedlund/libpraat
 * Generate TextGrids from Phon records.  TextGrid data for projects is stored in the __res/plugin_data/textgrid/data folder
 * Import TextGrids as Phon records
 * Visualization of Praat data inside Waveform view
	* Spectrogram
	* Formant structure
	* Pitch
	* Intensity
 * TextGrid visualization
 * TextGrids can be opened in Praat from Phon for editing.  TextGrid data is then returned to Phon using a new Praat command 'Send back to calling program...'.
 Requires Praat version 5.3.85 or later.
 * Acoustic data measurement via query and reporting system
	* Formant, Pitch, and Intensity listings
	* More to come..
 * Send information to Praat via Praat scripts and return data to Phon

### IPA Support
 * Ligatures may be used to connect diacritics as well as create compound phones
 * Multiple prefix/suffix diacritics may be added to a phone
 * Added support for sandhi and linkers
 * Added support for intra-word pauses '^'
 * Inter-word pauses '(.)' are now only allowed between words
 * Improved user feedback on transcription errors
 * New class IPATranscript replaces Phone.  This class has similar methods to the String class
 in Java.
 * For more information, see the '''IPA & Phonex Reference''' in the Help menu of Phon

### Phonex
 * New version 2.0 of Phonex with a similar API to the Java regular expression engine.
 * Support for many new constructs including:
	* Groups (capturing, non-capturing)
	* Back-references
	* Phone classes (custom sets of allowed phones)
	* Reluctant and possessive quantifiers
 * Plug-in support allowing for creation of additional phone matchers
 * For more information, see the '''IPA & Phonex Reference''' in the Help menu of Phon

### General
 * Default workspace folder is now <user home>/Documents/PhonWorkspace
 * Log location is now <user home>/Documents/Phon/phon.log.0 and <user home>/Documents/Phon/phon.log.1 (previous execution)
 * Location of user scripts, IPA dictionary, custom layouts, etc. is now <user home>/Documents/Phon
 * New streamlined API, documentation available at https://www.phon.ca/phontrac/wiki/dev/
 * Source code available on GitHub at https://github.com/ghedlund/phon
 * Phon has been re-structured as several maven/ivy artifacts which can be integrated into other
 (JVM-based) software projects very easily.  For more information https://www.phon.ca/phontrack/wiki/dev