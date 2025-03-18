# Consonant Clusters

## All consonant clusters

The simplest method of seraching for consonant clusters is the following expression:

```
\c<2,>
```

However, this method will not corretly identify heterosyllabic consonant clusters when the second syllable is prefixed by a boundary or stress marker.  To include all possible samples, the following three expressions are required.

 1) Initial/Medial clusters followed by an optional stress marker and vowel
```
(\c<2,>)(?>\s?\v)
```
 2) Final clusters
```
(\c<2,>)$
```
 3) Heterosyllabic clusters
```
(\c+[\s\.]\c+)
```

Use the double pipe (i.e., ```||```) operator to combine the expressions:

```
(\c<2,>)(?>\s?\v) || (\c<2,>)$ || (\c+[\s\.]\c+)
```

## Tautosyllabic Clusters

 1) Onset clusters including onsets of empty headed syllables
```
(\c:sctype("LeftAppendix|Onset|OEHS")<2,>)
```

 2) Coda clusters
```
(\c:sctype("Coda|RightAppendix")<2,>)
```

Combined:

```
(\c:sctype("LeftAppendix|Onset|OEHS")<2,>) || (\c:sctype("Coda|RightAppendix")<2,>)
```

## Heterosyllabic Clusters

Similar to the expression listed for all consonants but uses the syllable boundary (i.e., ```\S```) matcher instead of the ```[\s\.]``` phone class matcher to include implicit syllable boundaries.

```
(\c+\S\c+)
```
