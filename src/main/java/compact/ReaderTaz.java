package compact;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    
    private static List<Map<String, Integer>> newsContent;
    
    private static List<String> sectionIDs;
    
    private static final Pattern P = Pattern.compile("nr:([0-9]+)");
    
    private final Set<String> stopwords;
    
    
    public ReaderTaz(Layer layer) throws IOException {
        this.layer = layer;
        this.stopwords = Stopwords.stopwords();
    }
    
    /**
     * 
     * @param conllFile The article file in CONLL format
     * @throws IOException 
     */
    @Override
    public void processFile(File conllFile) throws IOException {
        newsContent = new ArrayList();
        sectionIDs = new ArrayList();
        String newsID = conllFile.getName();
        
        System.out.println(String.format("Processing file %s", newsID));
        
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {
                List<Token> sent = sentence.getTokens();
                
                String feats = sent.get(0).getFeatures().or("_");
                Matcher m = P.matcher(feats);
                int tokenID = 0;
                if (m.find())
                    tokenID = Integer.parseInt(m.group(0).substring(3));   //Find regex "nr:[0-9]+" in features
                
                String sectionID = newsID + "_" + tokenID;
                if (!sectionIDs.contains(sectionID)) {
                    sectionIDs.add(sectionID);
                    newsContent.add(new HashMap());
                }
                
                Map<String, Integer> wordFrequencies = newsContent.get(sectionIDs.indexOf(sectionID));
                
                for (Token token : sent) {
                    String value = layer == Layer.LEMMA ?
                        token.getLemma().or("_") :
                        token.getForm().or("_");
                    if (stopwords.contains(value)) {
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
        return newsContent;
    }
    
    @Override
    public List<String> getSectionIDs() {
        return sectionIDs;
    }    
   
}