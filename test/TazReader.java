package io;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Read CONLL files from taz corpus. Save each article and its id. Delete
 * stopwords before further processing.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class TazReader {
    
    private static List<List<Token>> articleContent;
    
    private static List<String> sectionIDs;
    
    private static String articleID;
    
    private static final Pattern p = Pattern.compile("nr:([0-9]+)");
    
    /**
     * 
     * @param conllFile The article file in CONLL format
     * @throws IOException 
     */
    public void processFile(File conllFile) throws IOException {
        articleContent = new ArrayList();
        sectionIDs = new ArrayList();
        articleID = conllFile.getName();
        
        System.out.println(String.format("Processing file %s", articleID));
        
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {
                List<Token> sent = sentence.getTokens();
                
                String feats = sent.get(0).getFeatures().toString();
                /*
                String startNum = feats.substring(feats.indexOf("nr:"));
                String feat = startNum.substring(3, startNum.indexOf("|"));
                System.out.println("feat "+feat);
                */
                Matcher m = p.matcher(feats);
                int artIdent = 0;
                if (m.find())
                    artIdent = Integer.parseInt(m.group(0).substring(3));   //Find regex "nr:[0-9]+" in features
                
                String sectionID = articleID + "_" + artIdent;
                int idx = sectionIDs.indexOf(sectionID);
                if (idx < 0) {
                    sectionIDs.add(sectionID);
                    articleContent.add(new ArrayList());
                }
                articleContent.get(sectionIDs.indexOf(sectionID)).addAll(sent);
            }
        }
    }
    
    public List<List<Token>> getArticleContent() {
        return articleContent;
    }
    
    public List<String> getSectionIDs() {
        return sectionIDs;
    }
    
    public String getArticleID() {
        return articleID;
    }
    
}