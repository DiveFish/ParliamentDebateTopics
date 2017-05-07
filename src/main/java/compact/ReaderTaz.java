package compact;

import com.google.common.collect.BiMap;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;
import gnu.trove.list.TIntList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.math3.util.Pair;

/**
 * Read CONLL files from taz corpus. Save article IDs and article content
 * (words and their respective frequency). Do not include stopwords.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class ReaderTaz implements Reader {
    
    private final Layer layer;
    
    private final Map<String, List<String>> newsMetadata;  // section ID <-> date
    
    private final Set<String> stopwords;
    
    private static List<Map<String, Integer>> fileContent;  // content of all sections, each section one HashMap
    
    private static List<String> sectionIds; // IDs of all sections
    
    private static final Pattern P_ID = Pattern.compile("nr:([0-9]+)");
    
    private static final Pattern P_DATE = Pattern.compile("dat:([0-9]){2}\\.([0-9]){2}\\.([0-9]){2}");
    
    
    public ReaderTaz(Layer layer) throws IOException {
        this.layer = layer;
        newsMetadata = new HashMap();
        this.stopwords = Stopwords.stopwords();
    }
    
    /**
     * 
     * @param conllFile The article file in CONLL format
     * @throws IOException 
     */
    @Override
    public void processFile(File conllFile) throws IOException {
        fileContent = new ArrayList();
        sectionIds = new ArrayList();
        String fileId = conllFile.getName();
        
        //System.err.println(String.format("Processing file %s", fileId));
        
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {
                List<Token> sent = sentence.getTokens();
                
                String feats = sent.get(0).getFeatures().or("_");
                
                String newsDate = "";
                Matcher md = P_DATE.matcher(feats);
                if (md.find()) {
                    newsDate =  md.group().substring(4);   //Find regex "dat: num{2}.num{2}.num{2}" in features
                }
                else {
                    System.err.printf("No date found in article %s", fileId);
                }
                
                Matcher mID = P_ID.matcher(feats);
                int tokenId = 0;
                if (mID.find()) {
                    tokenId = Integer.parseInt(mID.group(0).substring(3));   //Find regex "nr:[0-9]+" in features
                }
                else {
                    System.err.printf("No ID found in article %s", fileId);
                }
                String sectionId = fileId + "_" + tokenId;
                
                // Encountered new article section
                if (!sectionIds.contains(sectionId)) {
                    sectionIds.add(sectionId);
                    newsMetadata.putIfAbsent(sectionId, Arrays.asList(newsDate));
                    fileContent.add(new HashMap());
                }
                
                Map<String, Integer> wordFrequencies = fileContent.get(sectionIds.indexOf(sectionId));
                
                for (Token token : sent) {
                    String value = layer == Layer.LEMMA ?
                        token.getLemma().or("_") :
                        token.getForm().or("_");
                    if (stopwords.contains(value.toLowerCase())) {
                        continue;
                    }
                    
                    // Exclude all words except proper nouns or proper and common nouns
                    //if (!token.getPosTag().or("_").equals("CARD")) {
                    //if (!token.getPosTag().or("_").equals("TRUNC")) {    
                    //if (!token.getPosTag().or("_").equals("VVFIN")) {    
                    if (!token.getPosTag().or("_").equals("VVPP")) {    
                    //if (!token.getPosTag().or("_").equals("ADJA")||token.getPosTag().or("_").equals("ADV")||token.getPosTag().or("_").equals("ADJD")) {
                    //if (!token.getPosTag().or("_").equals("NE")) {
                    //if (!(token.getPosTag().or("_").equals("NN")||token.getPosTag().or("_").equals("NE"))) {
                        continue;
                    }
                    
                    if (!wordFrequencies.containsKey(value)) {
                        wordFrequencies.putIfAbsent(value, 1);
                    }
                    else {
                        wordFrequencies.put(value, wordFrequencies.get(value)+1);
                    }
                }
            }
            
        }
    }
    
    @Override
    public List<Map<String, Integer>> getContent() {
        return fileContent;
    }
    
    @Override
    public List<String> getSectionIDs() {
        return sectionIds;
    }
    
    @Override
    public Map<String, List<String>> getMetadata() {
        return newsMetadata;
    }
    
    /**
     * Find the earliest document in the cluster. Sort documents by date and
     * return doc id with earliest date.
     *
     * @param cluster The cluster of documents
     * @param documentIndices The document indices
     * @return The doc id of earliest date in cluster
     */
    public int earliestDoc(TIntList cluster, BiMap<String, Integer> documentIndices) {
        SortedSet<Pair<Integer, Date>> sortedDates = new TreeSet<>((o1, o2) -> {
            int cmp = o1.getValue().compareTo(o2.getValue());
            if (cmp == 0) {
                return o1.getKey().compareTo(o2.getKey());
            }
            return -cmp;
        });
        
        Map<Integer, Date> dateIds = dateByID(documentIndices);
        
        for (int i = 0; i < dateIds.size(); i++) {
            sortedDates.add(new Pair<>(i, dateIds.get(i)));
        }
        System.out.printf("Date: %s, doc %s", sortedDates.first().getValue(), sortedDates.first().getKey());
       return sortedDates.first().getKey();
   }
   
    /**
     * Map each date to the document index. Original format is "file ID - doc idx"
     * and "file ID - date string", should be "doc idx - date"
     * 
     * @param documentIndices The document indices
     * @return The dates by document indices
     */
   private Map<Integer, Date> dateByID(BiMap<String, Integer> documentIndices) {
       
       BiMap<Integer, String> docsByIdx = documentIndices.inverse();
       Map<Integer, Date> dateIDs = new HashMap();
       
       for (int i = 0; i < docsByIdx.size(); i++) {
           dateIDs.putIfAbsent(i, stringToDate(newsMetadata.get(docsByIdx.get(i)).get(0)));  // get date from metadata
       }
       return dateIDs;
   }
   
   /**
    * Convert string to date.
    * 
    * @param dateString The string to be converted
    * @return The date
    */
   private Date stringToDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
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
