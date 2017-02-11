package io;

import eu.danieldk.nlp.conllx.Token;
import java.util.List;

/**
 * A class to represent the sections contained in an article.
 *
 * @author Patricia Fischer
 */
public class ArticleSection{
    private final String id;
    private final List<Token> content;

    public ArticleSection(String id, List<Token> content){
        this.id = id;
        this.content = content;
    }

    public String sectionID() {
        return id;
    }

    public List<Token> articleContent() {
        return content;
    }
}
