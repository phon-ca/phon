# Session Timeline Changes (Proposal)

## Terms

 * Intervals when talking about TextGrids, segments when talking about Phon data
 * Points when talking about TextGrids, markers when talking about Phon data

## Goals

 * Add timeline information for all record data (e.g., segment times for groups/words/phones, VOT markers for consonant segments)
 * Add ability to create and modify session level timeline information (e.g., session comments, custom annotations)
 * Add options for accessing/searching timeline information in queries and reports (e.g., add word/group/phone length filter options in queries.)
 * Provide methods for importing and exporting TextGrids
 * Provide round-trip support for timeline information with TalkBank (where possible)

## Implementation

### Session changes

Timeline information will be distributed throughout session data:

 * Comments tier
   
   Data for the comments tier will be interleaved with record data (using current method)
   
 * Record data tiers
 
   Timeline information for record data tiers will be stored with record data. Record segment will be defined by the segment tier as it is currently.
   Segment values for groups/words/phones will be derived from the record segment by using normalized values (between 0.0 and 1.0) for boundaries. Initially
   all elements will be given equal distribution within their parent (e.g., 2 groups in a record will each be given 50% of record time, each word of 2 in the first group will be given 25% of record time, etc.)

 * Session level tiers
 
   Session level tiers will be stored in parallel with record data.  Session level tiers may any user-defined tier specified by the user.  A Session level tier
   may be a mix of both intervals and points. Tiers imported from TextGrids but are not mapped to record data tiers will appear as a session level tier. 

#### TalkBank Support

 * Comments
  
   Comments will be supported by CHAT/TalkBank out of the box as we are hijacking the current comment format.
   
 * Record data tiers
 
   Additional elements/properties will need to be added to the TalkBank format to accomodate the new information.
   
 * Session level tiers
 
   Unknown if this will be supported by TalkBank at any point as it represents a parallel transcript that does not comply with their transcipt requirements.
   One option may be to add a custom comment type to TalkBank which may be added to the end of the transcript.
      
### UI Changes

New timeline tiers will become available in both the Speech Analysis and Timeline views:

 * Timeline view
   * Comments tier
   * Session level tiers
 * Speech analysis view
   * Record data tiers
   * Session level tiers?
   
#### Comments tier

The comments tier will display comments as markers in the timeline. Comments will appear:

 * At the specified position in the timeline if the comment has a specifid media time specified, otherwise:
 * At the beginning of the timeline if the comment exists before any record data; or
 * At the beginning of the next record in the session if the comment is interleaved between records.
 
Overlapping markers will be merged in the UI.

Actions will be available to modify or add session comments in all positions.

#### Record data tiers

Record data tiers will display record data in a grid with segment values derived from the record's segment start/end times.  Each phon tier will have multiple sub-tiers,
visiblity and order controllable by the user.  Sub-tiers will be created for each query domain:

 * Group/word/morpheme? for all tiers
 * Group/word/morpheme?/syllable/phone for all IPA Transcript tiers

> Note: syllable tier segments will be derived from phone segments

Record data segments may have associated markers which may be used to fine tune analyses.  These makers will be visible when clicking on
the segment, or by turning on marker visiblity in the UI settings.  Exmaples of these markers are:

 * analysis window adjustment (start/end time offsets)
 * VOT markers (for consonants)
 
#### Session level tiers

Session level tiers are user-defined timeline tiers which span the entire session.  They may consist of a mixture of segments and markers as defined by the user.
Tiers imported from TextGrid files but are not associated with Phon tier data will be added as session level timeline tiers.

### Query/Analysis Changes

 * Add new query filters
 * Add new accessors for timeline information in reports
 * All current Praat based query/analyses will need to be updated to use the new API.
   
### TextGrid Support

#### TextGrid differenences/incompatibilities

 * TextGrid files do not support overlapping segments. Timeline tiers in Phon will not have this restriction.
 * Timeline segments will support additional point annotations (e.g., analysis start/end offsets, VOT markers, etc.)
 
These incompatibilites will need to be reconciled in TextGrid imports/exports.

#### Importing TextGrids

Importing TextGrids data will work in a similar manner as our current method of importing data with a few changes:

 * Some point tiers, such as VOT, will be imported as annotations to timeline segments instead of as session level tiers. Users will be able to choose
   point tiers for specific annotations.
 * A TextGrid import may generate multiple sessions using an interval tier from the TextGrid (this can be the same as the record segment tier, creating one session per record)

Issues

 * Incompatibilites between Phon Timeline information and Praat TextGrids will make it impossible to re-import TextGrid data over an existing Phon session.  
   TextGrid import will only create new records. Round-tripping information will not be possible.
 * The current method of importing modified TextGrid information for a record may not be possible.  More investigation is required.

#### Exporting TextGrids

Any timeline information will be exportable to TextGrid data.

 * Mixed timeline tiers (tiers with both segments and markers) will be split into two TextGrid tiers.  (e.g., A VOT point tier will be exported if necessary.)
 * Segments will be clipped if an overalp occurs, this information is unrecoverable.
 * Record data intervals will be clipped if they overlap, this information is unrecoverable.
 * Multiple session may be exported to the same TextGrid, using the rules above, if they share the same media.

