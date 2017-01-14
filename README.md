# ParliamentDebateTopics
...detects topics and topic changes in German parliamentary debates and relates them to taz articles.
For now, only the matrix representation of the debates has been implemented.

Run MatrixBuilder to have a look at the term-document, tf.idf and svd matrices.

Notes:
- To change the xml file directory, adjust the FILE_DIR variable in MatrixBuilder accordingly.
- To process lemmas instead of tokens, change the layer variable in MatrixBuilder from "form" to "lemma". Also make sure the TreeTagger ist accessible via the Other Sources folder, its directory is given in the Lemma class and the German-utf8.par exists in the TreeTagger/lib folder.

