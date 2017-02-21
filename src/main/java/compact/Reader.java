package compact;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Process input file consisting of sections. Save section IDs and section
 * contents (words and their respective frequency).
 *
 * @author Patricia Fischer
 */
public interface Reader {
    
    public void processFile(File file) throws IOException;
    
    public List<Map<String, Integer>> getContent();
    
    public List<String> getSectionIDs();
}
