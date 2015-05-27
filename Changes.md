# Phon 2.1 Changes

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
