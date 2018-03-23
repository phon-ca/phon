# Singleton Consonants

## All Singleton Consonants

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

## Singleton Onsets

 1) Standalone onsets including onsets of empty headed syllables.
```
(?<^\s?)(\c:sctype("Onset|OEHS"))$
```

 2) All other singleton onsets
```
(?<\S)(\c:O)(?>\v)
```

Combined:

```
(?<^\s?)(\c:sctype("Onset|OEHS"))$ || (?<\S)(\c:O)(?>\v)
```

## Singleton Codas

 1) Standalone codas
```
(?<^\s?)(\c:C)$
```

 2) All other singleton codas
```
(?<\v)(\c:C)(?>\S)
```

Combined:

```
(?<^\s?)(\c:C)$ || (?<\v)(\c:C)(?>\S)
```