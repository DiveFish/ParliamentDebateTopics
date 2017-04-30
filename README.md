# ParliamentDebateTopics
...detects topics and topic changes in German parliamentary debates and will (hopefully at some point) relate them to taz articles.

Usage: corpus fileDirectory storageDirectory

Instructions:
Depending on the input, run .jar with "PolMine" or "taz" (case INsensitive) as first argument,
the source directory of the PolMine/taz data files as second
and the directory where the serialized data shall be stored as third argument.
The taz corpus is assumed to be split into sub-directories containing the publications from one year each.
Note: You can also hardcode the corpus and file directory in main().

Output:
The program will for each cluster return the date and file id of the earliest document in the cluster
and the most relevant terms for all documents. Most relevant are those terms which have high tf-idfs
and occur in several of the cluster documents (change the ratio in main(), 1 = term has to occur in all docs, 0 = term occurs in one doc).


