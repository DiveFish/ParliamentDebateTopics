package compact;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build a vocabulary from text input. Associate each word (lemma or token)
 * and each document with an index.
 * 
 * The document frequency keeps track of all the documents in which a term
 * occurs so that these counts do not have to be calculated separately for the
 * tf.idf matrix.
 * The token counts keep track of the overall frequency of a token in the whole
 * corpus and can be used to retrieve (and filter) the most frequent tokens
 * which are thereby considered stopwords.
 * 
 * 
 * @author Daniël de Kok and Patricia Fischer
 */
public class Vocabulary {
    
    private final BiMap<String, Integer> documentIndices;
    
    private final BiMap<String, Integer> tokenIndices;
    
    private final TIntList tokenCounts;  //for getting the most frequent tokens/ stopwords
    
    private final TIntList documentFrequencies; //stores for each term the documents in which it occurs
    
    private final Map<Integer, Date> dateIds;
    
    /**
     * Vocabulary can be created on lemmas or tokens.
     * Layer LEMMA will lemmatize the input text and process word types.
     * Layer TOKEN will skip lemmatization and process the input by tokens.
     * 
     * @param layer The layer of word extraction
     */
    
    public Vocabulary(Layer layer) {
        documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        tokenCounts = new TIntArrayList();
        documentFrequencies = new TIntArrayList();
        dateIds = new HashMap();
    }
    
    // processFile()
    // extractFileID()
    // extractVocabulary()
    // <- input: List<Map<String, Integer>>; List<String> = wordFrequencies and file IDs
    
    /**
     * Process content of each debate section and save file ID.
     * 
     * @param sectionID The file name of the debate
     * @param wordFrequencies
     * @throws IOException
     */
    public void processSection(String sectionID, Map<String, Integer> wordFrequencies) throws IOException {
        extractSectionID(sectionID);
        extractVocabulary(wordFrequencies);
    }
    
    /**
     * Extract tokens from text and assign them an index.
     * Process them either as lemmas or tokens.
     * 
     * @param wordFrequencies The content represented as words and their respective frequency
     */
    private void extractVocabulary(Map<String, Integer> wordFrequencies) throws IOException {
        for (Map.Entry<String, Integer> entry : wordFrequencies.entrySet()) {
            String token = entry.getKey();
            Integer frequency = entry.getValue();
            Integer index = tokenIndices.get(token);
            if (index == null) {
                index = tokenIndices.size();
                tokenIndices.put(token, index);
                tokenCounts.add(frequency);
            }
            else {
                tokenCounts.set(index, tokenCounts.get(index) + frequency);
            }

            if (documentFrequencies.size() == index) {
                documentFrequencies.add(1);
            } else {
                documentFrequencies.set(index, documentFrequencies.get(index) + 1);
            }
        }
    }
    
    /**
     * Add section ID to documentIndices, assigning each ID an index.
     * 
     * @param sectionID The file ID of the debate
     */
    private void extractSectionID(String sectionID) {
        if (documentIndices == null) {
            return;
        }
        documentIndices.putIfAbsent(sectionID, documentIndices.size());
    }

    /**
     *
     * @return The file IDs and their associated indices
     */
    public BiMap<String, Integer> documentIndices() {
        return documentIndices;
    }

    /**
     *
     * @return The tokens and their associated indices
     */
    public BiMap<String, Integer> tokenIndices() {
        return tokenIndices;
    }
    
    /**
     *
     * @return The list of token counts
     */
    public TIntList tokenCounts() {
        return tokenCounts;
    }

    /**
     *
     * @return A list of document index lists
     */
    public TIntList documentFrequencies() {
        return documentFrequencies;
    }   
    
    /**
     *
     * @return The dates by document indices
     */
    public Map<Integer, Date> documentDates() {
        return dateIds;
    }
    
    
    /**
     * Map each date to the document index. Original format is "file ID - doc idx"
     * and "file ID - date string", should be "doc idx - date"
     * 
     * @param metadata The metadata containing the file's date
     */
   public void extractDocumentDates(Map<String, List<String>> metadata) {
       
       BiMap<Integer, String> docsByIdx = documentIndices.inverse();
       
       for (int i = 0; i < docsByIdx.size(); i++) {
           dateIds.putIfAbsent(i, stringToDate(metadata.get(docsByIdx.get(i)).get(0)));  // get date from metadata
       }
   }
   
   /**
    * Convert string to date.
    * 
    * @param dateString The string to be converted
    * @return The date
    */
   private Date stringToDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
        Date date = new Date();
        try {
            date = formatter.parse(dateString);
            return date;

        } catch (ParseException e) {
            System.err.println("Cannot parse date");
        }
        return date;
   }
}
