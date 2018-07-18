# Comments

Comments may be inserted in phonex expression using c-style comment syntax. There are two types of comments:

 1. General Comment ```/* ... */```
 2. End of Line Comment ```// ...```

## General Comments

General comments start with the ```/*``` token and end with ```*/```.  They can be found anywhere in the expression and may span multiple lines.

E.g.,

```
/*
 * This is a comment
 */
\c\v 
```

```
\c /* This is also a comment */ \v
```

## End of Line Comment

End of line comments begin with the ```//``` token and include the remainder of the current line.

E.g.,

```
// This is a comment
\c\v
```

```
\c // Everything after the first '//' is a comment
\v
```
