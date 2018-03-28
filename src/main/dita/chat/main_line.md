# CHAT Main Line

The *CHAT* main line codes the basic transcription of what a speaker said.  The main line includes both pronounced forms and markers. In *CHAT* the main line starts with an asterisk followed by the speaker ID, a colon and a tab; the remainder of the line is the orthographic transcription. More information about the *CHAT* main line can be found at https://talkbank.org/manuals/CHAT.html#_Toc486414395.

E.g.,

```
*CHI:   hello world!
```

In *Phon*, the main line is mapped to the *Orthography* tier.  Each main line in a *CHAT* document cooresponds to a single record in a *Phon* session. Phon supports many of the same coding standards as *CHAT*.  This document will outline many of the common codings as well as any differences from *CHAT*.  See https://talkbank.org/manuals/CHAT.html#_Toc486414395 for complete documentation.

## Special Form Markers

The following special form markers are available in *Phon*.  For more information see https://talkbank.org/manuals/CHAT.html#_Toc486414397.

| Letters | Category | Example | Meaning |
|--|--|--|--|
| ```@a``` | addition | ```xxx@a``` | unintelligible |
| ```@b``` | babbling | ```abame@b``` |  |
| ```@c``` | child-invented form | ```gumma@c``` | sticky |
| ```@d``` | dialect form | ```younz@d``` | you |
| ```@e``` | echolalia, repetition | ```want@e more@e``` | want more |
| ```@f``` | family-specific form | ```bunko@f``` | broken |
| ```@fs``` | filled-syllable | ```uh@fs``` |  |
| ```@fp``` | filled-pause | ```xxx@fp``` |  |
| ```@g``` | general special form | ```gonga@g``` |  |
| ```@i``` | interjection, interaction | ```uhhuh@i``` |  |
| ```@k``` | multiple letters | ```ka@k``` | Japanese “ka” | 
| ```@l``` | letter | ```b@l``` | letter 'b' |
| ```@n``` | neologism | ```broked@b``` | broken |
| ```@si``` | singing | ```lalala@si``` | singing |
| ```@sl``` | signed language | ```apple@sl``` | apple |
| ```@sas``` | sign & speech | ```apple@sas``` | apple |
| ```@t``` | test word | ```wut@t``` | what |
| ```@u``` | unibet transcription | ```binga@u``` |  |
| ```@wp``` | word play | ```goobarumba@wp``` |  |
| ```@x``` | excluded words | ```stuff@x``` | excluded |
| ```@z:*``` | user-defined code | ```word@z:rtfd``` | any user code |
