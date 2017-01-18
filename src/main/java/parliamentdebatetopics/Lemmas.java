package parliamentdebatetopics;

import java.io.IOException;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

/**
 * This class produces lemmas of text input. The tree-tagger and tt4j wrapper
 * are used to create lemmas from tokens.
 *
 * @author Patricia Fischer
 */
public class Lemmas {
    
    private final List<String> lemmas;
    private final String TREE_TAGGER_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/TreeTagger";
    
    public Lemmas(){
        lemmas = new ArrayList<>();
    }
    
    /*
    public static void main(String[] args) throws IOException, TreeTaggerException{
        Lemmas lem = new Lemmas();
        String text = "Die Parlamentsdebatte des Bundestags ging bis in die späten Abendstunden ."
                    + "Aber wir gingen nicht so spät :)";
        List<String> splitText = asList(text.split("\\s+"));
        lem.lemmatize(splitText);
        
        //Check content of "lemmas" list
        List<String> lemmatizedText = lem.getLemmas();
        System.out.println("LEMMATIZED TEXT");
        for(String word : lemmatizedText)
            System.out.println(word);
    }
    */

    /**
     *
     * @param sectionContent
     * @return
     * @throws IOException
     * @throws TreeTaggerException
     */
    public List<String> lemmatizeDebateSection(List<String> sectionContent) throws IOException, TreeTaggerException{
        lemmatize(sectionContent);
        return lemmas;
    }
    
    /**
     *
     * @param tokens
     * @throws IOException
     * @throws TreeTaggerException
     */
    public void lemmatize(List<String> tokens) throws IOException, TreeTaggerException{
        System.setProperty("treetagger.home", TREE_TAGGER_DIR);
        TreeTaggerWrapper tt = new TreeTaggerWrapper<>();
        try {
            tt.setModel("TreeTagger/lib/german-utf8.par");
            tt.setHandler(new TokenHandler<String>() {
                public void token(String token, String pos, String lemma) {
                    //System.out.println(token+"\t"+pos+"\t"+lemma);
                    //System.out.println(token+"_"+lemma);
                    lemmas.add(lemma);
                }
            });
            tt.process(tokens);
        }
        finally {
            tt.destroy();
        }
    }
    
    /**
     *
     * @return
     */
    public List<String> getLemmas(){
        return lemmas;
    }
}
