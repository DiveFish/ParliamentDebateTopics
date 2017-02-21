package compact;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Load stopword lists from files.
 *
 * @author Patricia Fischer
 */
public class Stopwords {
    
    public static Set<String> stopwords() throws FileNotFoundException, IOException {
        
        Set<String> stopwordList = new HashSet();
        try(BufferedReader br = new BufferedReader(new FileReader("./src/main/resources/stopwords.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                stopwordList.add(line.trim());
            }
        }
        try(BufferedReader br = new BufferedReader(new FileReader("./src/main/resources/stopwords_capitalized.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                stopwordList.add(line.trim());
            }
        }
        Set<String> stopwords = ImmutableSet.<String>builder().addAll(stopwordList).build();
        
        return stopwords;
    }
}
