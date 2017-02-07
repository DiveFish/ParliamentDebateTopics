# ParliamentDebateTopics
...detects topics and topic changes in German parliamentary debates and relates them to taz articles.
For now, only the matrix and cluster representation of the PolMine debates and the taz articles has been implemented.

Usage: InputHandler.java corpus fileDirectory

-> Depending on the input, run InputHandler with "polmine" or "taz" as first and the source directory of the data files as second argument. What you will get are the clusters from this data set.

You can also hardcode the corpus and file directory in the InputHandler. 
