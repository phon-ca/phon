# Groups

Phonex allows defining groups by placing any subpattern between the parenthesis - '(' and ')' - metacharacters.

Some reasons to use groups:

 * Repeating subpatterns
 * Extract information for furthur processing
 * Exclude part of the pattern from the final match
 * Denote different possible subpatterns

## Capture Group

Capture groups are used to extract portions of matches for furthur processing.  In Phon this is often used
to create a new column in query result listings containing the data matched by the group subpattern.

For example, say you were searching for any CV pattern (e.g., ```\c\v```) but you wanted the consonant and vowel
in their own separate columns in a Phon query report.  You would place each phone matcher into a group using parenthesis.
(It's also common to 'name' the group in this situation, see 'Group Names' below.)

E.g.

```
(\c)(\v)
```

Capture groups can be quantified. The following expression will match a consonant followed by a vowel repeatedly:

```
(\c\v)+
```

## Lookahead and Lookbehind Groups

Lookahead and lookbehind groups allow matching subpatterns around a pattern without including
the content matched by the lookahead and lookbehind groups.  These groups are considered to be
zero-width assertions like the start-of-input ```^``` and end-of-input ```$``` boundary matchers.

Lookahead patterns are contained within parenthesis like regular groups with the special prefix ```?>```
at the beginning.  An example of using a lookahead group would be to search for all consonants ```\c``` which
are followed by a high vowel ```{v, high}```.

```
\c(?>{v, high})
```

Lookbehind patters are specified by the group prefix ```?<```.  They behave in the same manner as
lookahead groups, but look backwards in the input rather than forwards.  An example would be to search
for all vowels ```\v``` which are preceeded by a ```b```.

```
(?<b)\v
```

Lookahead and lookbehind groups can be used together in the same pattern.

## Conditional Groups

Conditional groups allow for choices within patterns. To specificy choices, subpatterns in a group
are spearated by the logical-or (or pipe) ```|``` metacharacter. The following example will match
the sequence ```bab``` as well as ```dib```.

```
(ba|di)b
```

Conditional groups can also be quantified.

## Group Numbers

Groups in a phonex pattern are numbered from left-to-right.  Each open parenthesis will increment the group index by 1 unless
the group is 'non-capturing' such as for lookbehind and lookahead groups.

## Group Names

Capturing groups can also be named. To name a group the group content should start with the group name followed by a
equals ```=``` metacharacter. The group name must start with a letter and consist of only letters, numbers, and 
underscore ```_```.  The following expression has two named groups; the first group name is 'onset'
and will match a consonant in the onset position ```\c:O```; the second group name is 'nucleus'
and will match a vowel in the nucleus position ```\v:N```.

```
(onset=\c:O)(nucleus=\v:N)
```

When used in Phon queries named groups will be added to result listings in a new column with a title
matching the phonex group name. The special group name ```X``` is reserved in Phon queries to mark
the portion of the phonex pattern to be used as the query result.

## Back References

Back references are used to match a subpattern previously matched by a capture group. Back references can be specified
using either the group number or group name. For a numbered back reference enter a backslash ```\```
metacharacter followed by the group number. The following pattern will match a consonant, store the value of the
matched consonant in group number 1, and then match the value of group 1 again (i.e., it will match repreated consonants.)

```
(\c)\1
```

To use a named group reference enter a backslash ```\``` metacharacter followed by the group name encoded in braces ```{``` and ```}```.
The following pattern will match a consonant in the onset position, store the value of the matched consonant in a group named ```C```, and then match
the value of group ```C``` again.

```
(C1=\c)\{C1}
```

Group names are case sensitive, so in the above example ```\{C1}``` would result in an error as there is no group named ```c1``` with a lower-case C.
Another caveat to point out is that the ```\{C1}``` back reference will *not* match syllable position information (e.g., ```:O```) or other supplementary matchers specified
in the capture group. Quantifier may be applied to back references but supplementary matchers are not allowed.
