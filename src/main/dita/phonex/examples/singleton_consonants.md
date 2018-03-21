# Example: Singleton Consonants

The phonex expression for singleton consonants has four parts:

 1) Standalone consonants
```
(?<^\s?)(\c)$
```
 2) Initial singleton consonants
```
(?<^\s?)(\c)(?>\v)
```
 3) Medial singleton consonants
```
(?<\v\s?)(\c)(?>\s?\v)
```
 4) Final singleton consonants
```
(?<\v)(\c)$
```
  
Use the double pipe (i.e., ```||```) operator to combine the expressions:

```
(?<^\s?)(\c)$ || (?<^\s?)(\c)(?>\v) || (?<\v\s?)(\c)(?>\s?\v) || (?<\v)(\c)$
```
