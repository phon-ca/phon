# Phon 2.0 Changes

## Changes from Phon 2.0.4

### General
 * Updated to Java 1.8.0_31 (JRE provided with installers.)
 * Improved support for HiDPI (e.g., Apple Retina) displays.
 * Added transliteration dictionary for Slovak.
 * Logging behaviour can now be modified using a property file.  The location of the file is specified using the property 'phonlog.properties' and must follow the specification outlined at http://docs.oracle.com/cd/E19717-01/819-7753/gcblo/index.html.

### IPA Map
 * Added search button to IPA Map.
 * The 'Always on Top' behaviour can now be modified.

### Session Editor
 * Added a new command to the Record Data view for merging all groups in the current record.
 * A dialog allowing selection of optional fields to anonymize is now displayed when using the Anonymize function in the Participant editor.
 * Calculated age is now retained if Age is not selected to be cleared when using the Anonymize participant function.
 * New lightweight video player replaces VLC opengl renderer in Media Player view.
 * Changed UI for position slider in Media Player view.
 * Default value for segmentation window is now 0.
 * Speech analysis view initially displays more time on each side of the current segment.
 * Zoom-in/out buttons in Speech Analysis view will keep current mid-point 'centered' in the view.
 * Navigation buttons in Session Editor no long keep focus when clicked.
 * Added confirmation dialog when deleting records in the Session Editor.
 * Improved error reporting when using the IPA lookup console in IPA Lookup view.

### Queries
 * Added new filters for syllable type to the Phones.js query.
 * Improved context menu items for 'Toggle Hiatus' with adjacent nuclei in Syllabication & Alignment view.
 * Reversed order of add/edit participant/tier buttons in Session Information and Tier Management views.
 * Added debugger for Phon queries (only available when property 'phon.debug' is 'true'.)

### UI
 * Changed some UI colours to match system theme colours.
 * Double-clicking a result in Check Transcriptions will navigate to the relevant record.

### Bug Fixes
 * Mac OS X dialogs now display as sheets where appropriate and will no longer compete for modality with Java dialogs.
 * Updated French syllabifiers.
 * Fixed modifying IPA for a group when no data has been entered for all previous groups in the same tier.
 * Fixed result highlighting for searches involving syllable patterns.
 * Fixed result highlighting for aligned searches in IPA transcripts which included combining diacritics.
 * Fixed option for including excluded records in queries.
 * Fixed case sensitive options for PatternFilter.js.
 * Fixed option to exclude aligned values in Phones.js.
 * Fixed filter for truncated syllables in queries.
 * Fixed null pointer exception when using regular expressions with PatternFilter.js in queries.
 * Fixed an issue with using the '+' (one or more) quantifier in Stress Pattern matchers.
 * Fixed alignment behaviour between grouped and non-grouped tiers in queries.
 * Fixed an issue when using the 'Find Next' button in the Find & Replace view if the language is regex and the last match was at the end of a group.
 * Fixed retention of result listing parameters in report editor.
 * Fixed column scripts for result listings in report editor.  Older reports will no longer open.
 * Blind transcriber password check now works correctly.
 * Fixed calculation of the 'chksize' header value when saving segments using Speech Analysis view.
 * Syllabifier names are now displayed correctly in combo boxes.
 * Orthography -> IPA lookups are now case insensitive.
 * Fixed save button in Speech Analysis view.
 * Segments which start at 0.0s will now be rendered correctly in the Speech Analysis view.
 * Fixed command for dropping user-defined IPA dictionary in IPA Lookup view.
 * Fixed the 'Toggle Hiatus' action in the Syllablcation & Alignment view for IPA Target transcripts.
 * Fixed font used in result set table.
 * Window/context menus will no longer disappear behind the media player.
 * Retain selection of tier when using the tier movement buttons in the Tier Management view.
 * Fixed highlighting suffix codes in Orthography tier.
 * Generating a TextGrid using the Praat menu of the Speech Analysis view will now generate a TextGrid  for the current (instead of previous) record.
 
## Changes from Phon 2.0.3

### General
 * Attempting to add a new media folder will no longer hang the application on Mac OS X
 * Reverted to system look and feel to fix an issue with UI flicker on Mac OS X
 * Fixed setup of initial syllabifier and dictionary language on new installs
 * Fixed an issue preventing VLC from loading on some Windows installs
 * Fixed an issue preventing VLC from playing media found on Windows network shares
 * Fixed a problem with resetting transcriptions using the Check Transcriptions tool
 * Fixed IPA Lookup view actions for blind transcribers
 * IPA lookup will now ignore puctuation at the beginning and end of words
 * Changed the keystrokes for Merge and Split groups to CTRL+ALT+G and CTRL+ALT+K respectively
 * Syllable boxes now redraw properly when modifying constituent information using the Syllabification & Alignment view
 * Fixed syntax highlighting for Orthography
 * Improved entry of birthday and age in participant editor
 * Improved layout of participant editor
 * Double clicking a participant in the session information view will open the participant editor
 
### Speech Analysis
 * Fixed rendering issues on Mac OS X
 * Significantly improved rendering performance of the waveform display
 * Audio output device may now be selected using the speech analysis menu
 * Audio may now be played in a loop
 * Display of channels in the waveform view can be toggled
 * When the current record has no segment, the view will display the first 3 seconds of audio data
 * Position in the audio data may be adjusted by dragging in the top portion of the waveform display
 * Window length, or amount of audio data display, may be adjusted using the zoom in/out buttons or by using the mouse wheel while holding control.

## Changes from Phon 2.0.2

### General
 * Updated Japanese transliteration dictionary
 * Added ePMLU calculations to PMLU assessment
 * Updated report for PMLU/ePMLU assessment so that results are grouped by target word
 * Updated report for PCC/PVC assessment
 * Time values for current selection in Speech Analysis are now displayed in the top bar

### Praat
 * Added DurationListing query script
 * Pitch and intensity values for current selection are now printed on right-hand side of spectrogram view
 
### Bugs
 * Fixed the 'Show More' button in the Speech Analysis view
 * Syllabification & Alignment is now updated during automatic transcription
 * Inter-word pauses are now included in automatic transcription
 * Fixed an issue where a manually entered age would not be saved for a participant.
 * Calculated age is now displayed in the Session Information view if a birthday and session date are available for a participant.

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