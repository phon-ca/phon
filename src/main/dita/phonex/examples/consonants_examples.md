# Basic Phone Matching

## Characters Matchers

Query for consonants using the base glyph.

Examples:

| Phonex | Meaning |
|--|--|
| ```b``` | Any consonant with 'b' as the base glyph |
| ```d``` | Any consonant with 'd' as the base glyph |

## Feature Set Matchers

Query using feature names. Features names are placed between braces (i.e., '{', "}').

Examples:

| Phonex | Meaning |
|--|--|
| ```{consonant}``` | Any consonant |
| ```{fricative}``` | Any fricative |

Multiple feature name may be provided, separated by commas.  If a paticular feature is to be excluded from the query it should be prefixed by a minus sign.

Examples:

| Phonex | Meaning | 
|--|--|
| ```{fricative, voiced}``` | Voiced fricatives |
| ```{consonant, -stop}``` | All consonants which are not stops |

Many feature names also have shorthand equivalents.  A listing of all feature names supported by phone along with their synomyms can be found 
[here.](../../generated/features.xml)

Examples:

| Phonex | Meaning |
|--|--|
| ```{c}``` | Any consonant |
| ```{fri}``` | Any fricative |

