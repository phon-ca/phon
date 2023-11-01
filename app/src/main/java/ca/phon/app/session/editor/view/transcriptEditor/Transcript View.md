# Transcript View

## Toolbar & window menu

 * Session information
   * Show/hide headers
   * Set date
   * Languages
     * Add language -> Language entry dialog with predictive text input with check using the Language class (Greg)
     * Remove item for each current language
   * View metadata (show editable table of metadata (i.e., string key->string value pairs) no restrictions)
   * Show session information view

 * Media
   * Select media
   * Show media player (embedded - todo by Greg)
   * Show timline view
   * Show media player view
   * Show speech analysis view

 * Participants
   * Add participant
   * Menu for each participant
     * Edit participant
     * Remove participant
   * Show session information view

 * Transcript
   * Insert record above/below (current transcript element index)
   * Insert comment above/below
   * Insert gem above/below

 * Tiers
   * Add tier
     * Show all items from UserTierType except %wor, %mor, %trn, %gra, %grt (chat tier names)
     * Custom tier (show tier editor)
   * Menu for each existing tier
     * Same as tier menu everywhere else
   * Show alignment & alignment as component
   * Show syllabification & syllabification as component
   * Show/hide blind transcriptions (i.e., enter 'validation mode')
   * Show/hide chat tier names
   * Show tier management view

 * Find/replace (toggle find/replace)

 * Font size button (not in window menu)

 * Insert symbol (todo by Greg)


## Keystrokes

General:
 * cmd/ctrl+s - needs to be hooked to save current tier edits and then call the window's 'Save' item in the 'File' window menu
 * cmt/ctrl+shift+s - same as above but for 'Save as'

General movement:
 * Up/down - switch 'tier'
 * Tab/shift tab - switch 'tier'
 * Home/end - beg/end of tier

Selection:
 * cmd/ctrl+a - select tier
 * cmd/ctrl+shift+a - select all text in document

Tier specific:

 * IPA transcript
   * cmd/ctrl+shift+t - auto transcribe

 * Syllabification
   * enter - begin syllabification edit mode where we lock the caret selection, any movement out of the tier, or enter, or esc will end mode

Element specific:

 * Media segments (including internal media)
   * enter - show media segment callout
