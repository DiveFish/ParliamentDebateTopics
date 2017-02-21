package compact;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Patricia Fischer
 */
public class main {
    
    public static void main(String[] args) throws IOException {
        
        String corpus;
        File directory;
        /*
        String corpus = "taz";
        File directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz_samples/");
        //File directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz/");
        
        */
        corpus = "PolMine";
        //File directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized/");
        
        directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized-samples/");
        //File directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/testFiles/");
        
        
        
        if (args.length != 2) {
            System.out.println("Wrong number of arguments.Usage: 2, provide name of dataset (Polmine/Taz) path to data files");
        }
        corpus = args[0].toLowerCase();
        directory = new File(args[1]);
        
        MatrixBuilder mb = new MatrixBuilder();
        mb.buildMatrix(corpus, directory);
    }
    
}
