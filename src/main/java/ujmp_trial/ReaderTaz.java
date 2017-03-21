package ujmp_trial;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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
        
        System.out.println(String.format("Processing file %s", fileId));
        
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
                    //if (!token.getPosTag().or("_").equals("VVPP")) {    
                    //if (!token.getPosTag().or("_").equals("ADJA")||token.getPosTag().or("_").equals("ADV")||token.getPosTag().or("_").equals("ADJD")) {
                    //if (!token.getPosTag().or("_").equals("NE")) {
                    if (!(token.getPosTag().or("_").equals("NN")||token.getPosTag().or("_").equals("NE"))) {
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
    

}