# Phon 2.1 Changes

## Changes from Phon 2.1.3

### General
* Added new command 'Check Session' to the Session Editor which will check the IPA
transcriptions in the current session.
* Check Session will automatically run when the Session Editor is open. A message will
be displayed in the status bar if any warnings are found.
* Added new :tone() phonex plug-in for matching numbered tone diacritics for all syllable
elements.

### Bug Fixes
* Fixed an issue with opening the Rename Corpus/Session dialogs in the project manager.
* Fixed a problem with the boolean check for ignoreDiaciritcs in the PCC javascript library.
* Fixed an issue with stress detection across all syllable elements when using the :stress()
phonex plug-in.
* Fixed an issue with the PMLU script which caused it to fail randomly.
* Fixed an issue with French IPA contraction rules.

## Changes from Phon 2.1.2

### General
* Revert to Java 8u31 to fix issues with rendering on Windows 10.
* Fixed header size in Exit dialog on mac.
* Improved behaviour of word alignment filter in queries.
* Fixed an issue with opening TextGrid files in Praat.
* Changed icons for Save/Save As actions.
* Fixed an issue with record selection when selecting values in a sorted buffer table.
* Updated French and Spanish IPA dictionaries.
* Syllabification and alignment is now updated when using Find & Replace.
* Fixed an issue with phone focus in Syllabification & Alignment when moving to a record with a shorter transcription.

## Changes from Phon 2.1.1

### General
* Update to Java 8u60.
* Fixed an issue with flat tier sizing when data spans multiple lines in Record Data view.
* Improved performance of updating query result highlights in session editor when using the Result Set Editor.
* New status bar for Session Editor which includes:
  * Modification status
  * Session corpus/name with link to corpus folder
  * Progress indicator
* Fixed memory leak when using the Result Set editor with an attached Session editor.
* Result set editor now opens with consistent size regardless of result set schema.
* Fixed an issue with keyboard focus on macosx when using the Result Set editor with an attached Session editor.
* PCC-PVC query now properly ignores diacritics.
* Improved performance of highlighting query results when using the Result Set editor.
* Added new feature 'cover' to symbols 'C', 'V', 'G', and '*'
* Fixed issues when opening older Phon session files with invalid alignments.

### Project Window
* Deleted corpora/sessions are now moved to Recycle Bin/Trash when deleted (if available.)
* The following Drag & Drop operations are now supported in the Project Window:
  * Copy corpora to another project
  * Drag corpus folders to another application (i.e., Explorer/Finder, mail applications, etc.)
  * Copy folders (corpora) into project by dropping onto the corpus list.
  * Copy sessions to another corpus/project
  * Drag session files to another application (i.e., Explorer/Finder, mail applications, etc.)
  * Copy xml files (sessions) into project by dropping onto the session list.
* Delete/Duplicate corpus/session operations may now apply to more than one selected item.
* Updated context menus for corpus/session lists in Project Window.

### Media
* Removed ffmpeg dependency.
* Updated to VLC 2.2.1.
* Now using VLC instead of ffmpeg for media exports.
* Removed deprecated 'Export media' function from Media Player view menu.
* Improved user feedback when exporting wav data for session media.
* Speech analysis start time will now scroll as the horizontal scrollbar is dragged.
* Improved look and feel of tier separators in Speech Analysis view.
 
### Praat Plug-in
* Update to jpraat 0.12 (Praat version 5.4.15)
* Improved form and behaviour of Generating Records from TextGrid function.
* There are now two methods for opening TextGrids in Praat
  * Open TextGrid in Praat - full audio: This will load the entire audio file in memory, not recommended for large sound files.
  * Open TextGrid in Praat - segment only: This will load the segment for the current record in memory, recommended unless you need to work with more than one record at a time.

### Api
* Added new functions to ca.phon.project.Project allowing for easier access to underlying paths to corpora/sessions.
* Fixed an issue with ca.phon.session.impl.WordImpl when requesting ipa transcripts in tiers with no data.

## Changes from Phon 2.1.0

### General Changes
 * Added basic git commands for Phon projects under the Project -> Team menu in the Project Manager window.
 * Set default wav display height to 100px.
 
### Praat Plug-in
 * Modified Formant Listing query so that all formants for an interval are printed in a single row.
 * Default value for Max formants in Formant Listing query is now 4.
 * Entire sound file is now avilable in Praat TextGrid editor when using the 'Edit TextGrid in Praat...' command.

### Bugs
 * Fixed an issue where keystrokes for segmentation would not work for newly added participants.
 * Fixed a bug where data for blind-transcribers would not be saved if the data was not committed using the enter key or focus change.
 * Ensure that horizontal scrollbar for Speech Analysis view is visible.
 * Wav display will now revalidate on x or y size changes.
 * Fixed participant assignment for records during Merge Session operations.
 * Fixed an bug when querying grouped, user-defined tiers which have been left empty for a record.
 * Fixed sorting order when sorting lists of ipa strings using ternary tries.
 * Fixed keystrokes for record navigation while focused in a tier field.
 * Keystrokes for next/previous record has been changed back to PgDown/PgUp.
 * Wav display is now repainted correctly when session media changes.
 * Record numbers are now printed without a decimal place in the result set navigator.
  
## Changes from Phon 2.0.8

### Project Window

 * Moved status label and 'blind mode' checkbox to bottom of window.
 * New multi-action buttons for creating new corpora and sessions. These buttons work the same way as the Create Project button in the Workspace dialog.
 * New layout using a split-pane for dividing the display horizontally.
 * New behaviours:
   * When opening an empty project, the create corpus button will be displayed and focused.  If the project has a corpus, the first corpus is selected.
   * New corpora are selected after creation.
   * When selecting an empty corpus, the create session button will be displayed and focused.
 * Fixed initial selection of corpus in new session dialog.
 * Changed icon for Corpus list header and selected corpus item
 * Icon for adding corpus is now always visible in corpus button
 * Added rename and delete actions to corpus button
 * Changed icon for Session list header and selected session item
 * Added rename, delete, and open actions session button

### Session Editor

 * Orthography wordnets may now be chained.  Examples: one+two+three, a~b~c, yo~ma+ma
 * Fixed an issue where CHAT tags in comments - particularly those inside events - would be 'invisible' in the Orthography tier.
 * Improved error reporting for Orthography.  Events and comments will no longer auto-complete, instead an error will be generated.
 * Undo/redo actions will now propagate correctly to the editor if no more edits are available for the current typing group
 * Save actions will now propagate correctly to the editor from within group fields.  Allowing the use of CMD/CTRL-S to save data without any commit action.
 * Default height of wav display has been reduced.  Height selected by the user is now saved as a preference.
 * Added short message which will display under the save button when save is completed - may move to a status bar?
 * Group Field editing will now trigger modification events in session editor

### Praat Plug-in

 * Added support for full-length TextGrids.
 * Added support for multiple TextGrids to be assigned to a session.  Only one TextGrid a time is visible in the Speech Analysis view.  TextGrids may be switched using the Praat plug-in menu in the Speech Analysis toolbar.
 * TextGrid folder for a session has been changed to '<project>/__res/textgrids/<corpus>/<session>/'
 * Added action to 'Add exisiting TextGrid..' which will copy a TextGrid selected by the user to the TextGrid folder for the session (and create that folder if necessary.)
 * Action 'Show TextGrid folder...' will now create the folder if it does not exist
 * Spectrogram default height has been reduced.  Height selected by the user is now saved as a preference. 
 * New wizard for creating records from TextGrid data
 * When generating TextGrids, group marker (i.e., '#') length is now 5% of the total interval for the record up to a max of 0.1s.  This fixes issues with generating TextGrids for smaller intervals.
 * Re-organized Praat TextGrid menu

### Queries

 * Phones.js has been modified so that any Phonex group named 'X' will become the result for the expression.  E.g., to isolate onsets of
 lax vowels, the expression would be: (X=\c:o+){lax,v}
 * Phones.js has been modified so that any named Phonex group will become a new field in the result.  E.g., to identifiy and separate complex
 onsets in result columns named 'O1' and 'O2', the expression would be: (O1=\c:o)(O2=\c:o)
 * Result sets from starred queries now have a star in record filter options.
 * Any query result set that is open in a result set editor is now available in record filter options.

### Other

 * Added IPA transcript conversion for SILDoulosIPA93
 * Added IPA transcript conversion for Germain-SAMPA
 * Added new window for displaying logs for the current and previous execution of Phon.  Window is available from the Help menu.
 * Changed name of application logs to phon0.log and phon1.log for current and previous logs respectively.

### Bugs

 * Fixed an issue with creating new sessions in corpora which have been renamed after a template has been setup.
 * Fixed detection and prompt to load autosaves when opening original session fails in Project Window.
 * Fixed an issue with CSV import where the IPA converters were not available for IPA tiers.
 * Fixed an issue that would cause the Find & Replace method in the Session editor to enter an infinite loop.
 * Speech Analysis view will no properly paint all channels in the audio file.
 * Case sensitive option in editor search field now works as expected
