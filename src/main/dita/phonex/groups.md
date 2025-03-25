# Groups

Phonex allows defining groups by placing any subpattern between the parenthesis - ```(``` and ```)``` - metacharacters.

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
(It's also required to 'name' the group in this situation, see 'Group Names' below.)

E.g.

```
(\c)(\v)
```

Capture groups may be quantified. The following expression will match a consonant followed by a vowel repeatedly:

```
(\c\v)+
```

## Lookahead and Lookbehind Groups

Lookahead and lookbehind groups allow matching subpatterns around a pattern without including
the content matched by the lookahead or lookbehind group.  These groups are considered to be
zero-width assertions (i.e., the length of matched content is zero) like the start-of-input ```^``` 
and end-of-input ```$``` boundary matchers.

Lookahead patterns are contained within parenthesis like regular groups with the special prefix ```?>```.
An example of using a lookahead group would be to search for all consonants ```\c``` which
are followed by a high vowel ```{v, high}```.

```
\c(?>{v, high})
```

Lookbehind patterns are specified by the group prefix ```?<```.  They behave in the same manner as
lookahead groups, but look backwards in the input rather than forwards.  An example would be to search
for all vowels ```\v``` which are preceeded by a ```b```.

```
(?<b)\v
```

Lookahead and lookbehind groups can be used together in the same pattern.

## Conditional Groups

Conditional groups allow for choices within patterns. To specify choices, subpatterns in a group
are spearated by the logical-or (or pipe) ```|``` metacharacter. The following example will match
the sequence ```bab``` as well as ```dib```.

```
(ba|di)b
```

Conditional groups may be quantified.

## Group Numbers

Groups in a phonex pattern are numbered left to right.  Each open parenthesis ```(``` metacharacter will increment the group index by 1 unless
the group is 'non-capturing' such as for lookbehind and lookahead groups. The following example pattern has two groups,
the first group includes both the consonant ```\c``` and vowel ```\v``` matchers; the second group includes only the 
vowel ```\v``` matcher:

```
(\c(\v))
```

The next example also has two groups as the lookbehind group is not included in group indexing:

```
(?<^\s)(\c(\v))
```

Phonex includes syntax to exclude a group from indexing (the group's content will not be stored.)  These groups are
called non-capturing or organizational groups. To exclude a group from indexing the group content must start with ```?=```. 
The following phonex pattern has two capturing groups: group 1 includes a syllable boundary ```\S```, consonant ```\c```, and 
vowel ```\v``` matcher; group 2 includes just the vowel ```\v``` matcher. There is one non-capturing group
containing the consonant ```\c``` matcher.

```
(\S(?=\c)(\v))
```

Note that while the consonant is considered part of a non-capturing group it will still be included in the enclosing group's
matched data.

## Group Names

Capturing groups may also be named. To name a group the group content should start with the desired group name followed by an
equals ```=``` metacharacter. The group name must start with a letter and consist of only letters, numbers, and 
underscore ```_```.  The following expression has two named groups; the first group name is 'onset'
and will match a consonant in the onset position ```\c:O```; the second group name is 'nucleus'
and will match a vowel in the nucleus position ```\v:N```.

```
(onset=\c:O)(nucleus=\v:N)
```

When used in Phon queries named groups will be added to result listings in a new column with a title
matching the phonex group name. The group name ```X``` is reserved in Phon queries to mark
the portion of the phonex pattern to be used as the query result.

## Back References

Back references are used to match a subpattern previously matched by a capture group. Back references can be specified
using either the group number or group name. For a numbered back reference enter a backslash ```\```
metacharacter followed by the group number. The following pattern will match a consonant, store the value of the
matched consonant in group number 1, and then match the value of group 1 again (i.e., it will match repeated consonants.)

```
(\c)\1
```

To use a named group reference enter a backslash ```\``` metacharacter followed by the group name enclosed in braces - ```{``` and ```}```.
The following pattern will match a consonant, store the value of the matched consonant in a group named ```C1```, and then match
the sequence stored in group ```C1```.

```
(C1=\c)\{C1}
```

Group names are case sensitive, so in the above example ```\{c1}``` would result in an error as there is no group named ```c1``` with a lower-case C.
Another caveat is that the ```\{C1}``` back reference will *not* match syllable position information (e.g., ```:O```) or other supplementary matchers specified
in the capture group. Quantifiers may be applied to back references but supplementary matchers are not allowed.
