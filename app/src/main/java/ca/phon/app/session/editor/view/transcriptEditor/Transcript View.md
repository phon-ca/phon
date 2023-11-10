# Transcript View

## Insertion hooks

 - Add following hook points.  These should be functions which are called at relevant points
in TranscriptDocument and have matching methods in TranscriptDocumentInsertionHook:
    - start session
    - start header
    - start header line
    - end header line
    - end header
    - start transcript
    - start comment
    - end comment
    - start gem
    - end gem
    - start record
    - start record header
    - end record header
    - begin tier
    - end tier
    - end record
    - end transcript
    - end session
  - Create abstract implementation for TranscriptDocumentInsertionHook

## Toolbar & view menu

 - Session information
   - Show/hide headers
   - Set date -> Show date header tier, put caret there, default to todays date
   - Languages
     - Set/add language -> If it doesn't exist, show it and move the caret to the end of the insert Language header line if not present and move caret to end 
     - Remove item for each current language
   - View metadata (show editable table of metadata (i.e., string key->string value pairs) no restrictions)
   - Show session information view

 - Media
   - Browse for media
     - Assign media action
   - Show media player (embedded - todo by Greg)
   - Show timeline view
   - Show media player view
   - Show speech analysis view

 - Participants
   - Add participant
   - Menu for each participant
     - Edit participant
     - Remove participant
   - Show session information view

 - Transcript
   - Insert record above/below (current transcript element index)
   - Insert comment above/below
   - Insert gem above/below

 - Tiers
   - Add tier
     - Show all items from UserTierType except %wor, %mor, %trn, %gra, %grt (chat tier names)
     - Custom tier (show tier editor)
   - Menu for each existing tier view
     - Same as tier menu everywhere else
   - Show alignment & alignment as component
   - Show syllabification & syllabification as component
   - Show/hide blind transcriptions (i.e., enter 'validation mode')
   - Show/hide chat tier names
   - Show tier management view

 - Find/replace (toggle find/replace)

 - Font size button (not in window menu)

 - Make a button to export pdf 

 - Insert symbol (todo by Greg)


## Keystrokes

General:
 - cmd/ctrl+s - needs to be hooked to save current tier edits and then call the window's 'Save' item in the 'File' window menu
 - cmt/ctrl+shift+s - same as above but for 'Save as'

General movement:
 - up/down - switch 'tier'
 - tab/shift+tab - switch 'tier'
 - home/end - beg/end of tier
 - ctrl/cmd home / ctrl/cmd end - begin/end of doc
 - pgup/pgdn - default (no override)
 - left/right - current impl

Selection:
 - cmd/ctrl+a - select tier
 - cmd/ctrl+shift+a - select all text in document

Tier specific:

 - IPA transcript
   - cmd/ctrl+shift+t - auto transcribe

 - Syllabification
   - enter - begin syllabification edit mode where we lock the caret selection, any movement out of the tier, or enter, or esc will end mode

Element specific:

 - Media segments (including internal media)
   - enter - show media segment callout
