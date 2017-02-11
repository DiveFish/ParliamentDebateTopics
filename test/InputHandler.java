package io;

import java.io.File;
import java.io.IOException;
import matrix.MatrixBuilderPolMine;
import matrix.MatrixBuilderTaz;

/**
 * A wrapper class to handle PolMine and Taz input.
 *
 * @author Patricia Fischer
 */
public class InputHandler {
    
    
    private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz/";
    
    //private static final String OUTPUT_DIR = "./TazMatrices/";
    
    
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized/";
    
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized-samples/";
    
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/testFiles/";

    //private static final String OUTPUT_DIR = "./PolMineMatrices/";
    
    
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Wrong number of arguments.Usage: 2, provide name of dataset (Polmine/Taz) path to data files");
        }
        
        //String dataset = args[0].toLowerCase();
        //String dataset = "polmine";
        String dataset = "taz";
        
        //File dataPath = new File(args[1]);
        File dataPath = new File(FILE_DIR);
        if (!dataPath.isDirectory()) {
            throw new IOException("Given directory name is not a valid directory.");
        }
        
        switch (dataset) {
            case "polmine":
                MatrixBuilderPolMine pol = new MatrixBuilderPolMine();
                pol.buildMatrix(dataPath);
                break;
            case "taz":
                MatrixBuilderTaz taz = new MatrixBuilderTaz();
                taz.buildMatrix(dataPath);
                break;
            default:
                throw new IOException("Please check name of dataset, choose between PolMine and Taz");
        }
    }
    
}
