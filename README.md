# ![https://www.phon.ca](https://github.com/ghedlund/phon/raw/master/app/src/main/resources/data/icons/32x32/apps/database-phon.png) Phon  

Phon is a software program that greatly facilitates a number of tasks related 
to the analysis of phonological data. Built to support research in 
phonological development (including babbling), second language acquisition, 
and phonological disorders, Phon can also be used to investigate virtually all 
types of phonological investigations (e.g. loanword phonology, fieldwork in 
phonology). Phon supports multimedia data linkage, unit segmentation (e.g. 
utterance, word), multiple-blind transcription, automatic labeling of data 
(features, syllabification), and systematic comparisons between target (model) 
and actual (produced) phonological forms. All of these functions are accessible 
through a user-friendly graphical interface. Databases managed within Phon can 
also be queried using a powerful search system adapted for the needs of the 
phonologist. This software program works on Mac OS X, Windows and Linux 
platforms and is compliant with the CHILDES TalklBank (http://talkbank.org) 
XML data format. Phon is being made freely available to the community as 
open-source software. Phon facilitates data exchange among researchers and is 
currently used for the elaboration of the shared PhonBank (http://childes.psy.cmu.edu/phon/)
database, a new initiative within CHILDES to support empirical needs of research
in all areas of phonological development.

For more information, visit https://www.phon.ca

## Building Phon

### Requirements

The following software must be installed in order to download and compile Phon:

 * Java JDK - 1.6 or higher (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
 * maven (http://maven.apache.org/)

### Downloading Phon


```
git clone https://github.com/ghedlund/phon
```

### Compiling Phon

```
mvn package
```

### Running Phon

```
java -cp "app/target/phon-app-<version>.jar:app/target/deps/*" ca.phon.app.Main
```
