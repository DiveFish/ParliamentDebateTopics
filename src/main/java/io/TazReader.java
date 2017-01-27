package io;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class TazReader {
    
    private static List<Token> articleContent;
    
    private static String articleID;
    
    /**
     * 
     * @param conllFile The article file in CONLL format
     * @throws IOException 
     */
    public void processFile(File conllFile) throws IOException {
        articleContent = new ArrayList<>();
        articleID = conllFile.getName();
        System.out.println(String.format("Processing file %s", articleID));
        
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {
                articleContent.addAll(sentence.getTokens());
            }
        }
    }
    
    public List<Token> getArticleContent() {
        return articleContent;
    }
    
    
    public String getArticleID() {
        return articleID;
    }
    
}