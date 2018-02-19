# Report: VoT

The voice onset time (VoT) report will calculate VoT for each query result.

## Parameters

 * *VoT Tier* TextGrid tiername for the VoT point tier (default 'VoT')

## Data preparation

This report requires the use of TextGrid intervals.  A TextGrid must be assigned to the session and tier mappings setup for the Phon tier used in the query.  A point tier (default name 'VoT') must exist, with a point for each interval for which VoT calculation will be performed.  The label of the point should match that of the related interval.

## VoT Calculation

Given a TextGrid interval, T, and a point, P, VoT is calculated as:
 
 * TODO

## Example

The table produced will have the following columns:

 * Session
 * Speaker
 * Record #
 * Result/Tier name
 * Start (s)
 * End (s)
 * Duration (s)
 * VoT Point (s)
 * VoT (s)
