# Example Phones Queries

The following examples are available in Phon from the *Phones* query window as *named queries*.

## Standalone Consonants
```
// Look-behind '(?<' and match beginning of input '^'
// followed by an optional stress marker '\s?'
(?<^\s?)
// Match a consonant '(\c)' followed by end of input '$'
(\c)$
```

## Initial Singleton Consonants
```
// Look-behind '(?<' and match beginning of input '^'
// followed by an optional stress marker '\s?'
(?<^\s?)
// Match a consonant (\c)
(\c)
// Look-ahead '(?>' and match a vowel
(?>\v)
```

## Medial Singleton Consonants
```
// Look-behind '(?<' and match a vowel '\v'
// followed by an optional stress marker '\s?'
(?<\v\s?)
// Match a consonant
(\c)
// Look-ahead '(?>' and match an optional stress maker and vowel
(?>\s?\v)
```

## Final Singleton Consonants
```
// Look-behind '(?<' and match a vowel '\v'
(?<\v)
// Match a consonant '(\c)' followed by end of input '$'
(\c)$
```

## Singleton Consonants
```
// The double pipe operator will combine results of independent expressions
// Standalone Consonants
(?<^\s?)(\c)$ ||
// Initial Consonants
(?<^\s?)(\c)(?>\v) ||
// Medial Consonants
(?<\v\s?)(\c)(?>\s?\v) ||
// Final Consonants
(?<\v)(\c)$
```

## Singleton Onsets
```
// The double pipe operator will combine results of independent expressions
// Standalone Onsets
(?<^\s?)(\c:O:E)$ || 
// All other Singleton Onsets
(?<\S)(\c:O)(?>\v)
```

## Singleton Codas
```
// The double pipe operator will combine results of independent expressions
// Medial Singleton Codas
(?<\v)(\c:C)(?>\s? (\c:O | \v)) ||
// Final Singleton Codas
(?<\v)(\c:C)$
```

## Initial Consonant Clusters
```
// Look-behind '(?<' and match beginning of input '^'
// followed by an optional stress marker '\s?'
(?<^\s?)
// Match a 2 or more consonants '(\c<2,>)'
(\c<2,>)
// Look-ahead '(?>' and match an optional stress marker '\s?' and vowel '\v'
(?>\s?\v)
```

## Medial Consonant Clusters
```
// Look-behind '(?<' and match a vowel '\v'
// followed by an optional stress marker '\s?'
(?<\v\s?)
// Match a 2 or more consonants '(\c<2,>)'
(\c<2,>)
// Look-ahead '(?>' and match an optional stress marker '\s?' and vowel '\v'
(?>\s?\v)
```

## Final Consonant Clusters
```
// Look-behind '(?<' and match a vowel '\v'
// followed by an optional stress marker '\s?'
(?<\v\s?)
// Match a 2 or more consonants '(\c<2,>)'
// followed by end of input '$'
(\c<2,>)$
```

## Heterosyllabic Consonant Clusters
```
// match one or more consonants '\c+'
\c+
// followed by a syllable boundary '\S' (includes implicit boundaries)
\S
// followed by one or more consonants '\c+'
\c+
```

## Consonant Clusters
```
// The double pipe operator will combine results of independent expressions
// Initial/medial Consonant Clusters
(\c<2,>)(?>\s?\v) ||
// Final Consonant Clusters
(\c<2,>)$ ||
// Heterosyllabic Clusters
(\c+[\s\.]\c+)
```

## Syllable-initial Clusters
```
// Includes left appendices ':L', onsets ':O' and OEHS ':E'
(?<^\s?)(\c:L:O:E<2,>) ||
(?<\v\s?)(\c:L:O:E<2,>)
```

## Syllable-final Clusters
```
// Includes right appendices ':R' and codas ':C'
(\c:C:R<2,>)$ ||
(\c:C:R<2,>)(?>\s?\c:O)
```

## Tautosyllabic Consonant Clusters
```
^(\c:L:O:E<2,>) ||
(?<\v\s?)(\c:L:O:E<2,>) ||
(\c:C:R<2,>)$ ||
(\c:C:R<2,>)(?>\s?\c:O)
```

## Syllables (short)
```
// Query all syllables, store each section in a named group
// Stress
(S=\s)?
// Onset
(O=\c:L:O<,5>)
// Rhyme
(R=
	// Nucleus
	(N=.:D<2> | .:N)
	// Coda
	(C=\c:C:R<,5>)
)
```

## Syllables (expanded)
```
// Query all syllables, store each constituent in a named group
// Stress
(S=\s)?
// Onset
(O=
	(O1=\c:L:O)(O2=\c:L:O)(O3=\c:L:O)(O4=\c:O)(O5=\c:O) 
	| (\c:L:O)(\c:L:O)(\c:O)(\c:O)
	| (\c:L:O)(\c:O)(\c:O)
	| (\c:O)(\c:O) 
	| (\c:O:E)
)?
// Rhyme
(R=
	// Nucleus
	(N=
		(N1=.:D)(N2=.:D) | (.:N)
	)
	// Coda
	(C=
		(C1=\c:C)(C2=\c:C)(C3=\c:C:R)(C4=\c:C:R)(C5=\c:C:R)
		| (\c:C)(\c:C)(\c:C:R)(\c:C:R)
		| (\c:C)(\c:C)(\c:C:R)
		| (\c:C)(\c:C) 
		| (\c:C)
	)?
)
```