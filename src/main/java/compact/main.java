package compact;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Patricia Fischer
 */
public class main {
    
    public static void main(String[] args) throws IOException {
        /*
        String corpus = "taz";
        File directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz_samples/");
        //File directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz/");
        
        */
        String corpus = "PolMine";
        //File directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized/");
        
        File directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized-samples/");
        //File directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/testFiles/");
        
        
        MatrixBuilder mb = new MatrixBuilder();
        mb.buildMatrix(corpus, directory);
    }
    
}
