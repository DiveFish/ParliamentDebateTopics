package compact;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Patricia Fischer
 */
public class ReaderUtils {
    
    public static List<File> getFiles(String corpus, File directory, String fileExtension) {
        List<File> files = new ArrayList();
        if (corpus.equalsIgnoreCase("taz")) {
            for (File dir : directory.listFiles()) {
                File subDir = new File(dir.getAbsolutePath());
                for (File file : subDir.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(fileExtension)) {
                        files.add(file);
                    }
                }
            }
        } else if (corpus.equalsIgnoreCase("PolMine")) {
            for (File file : directory.listFiles()) {
                if (file.isFile() && file.getName().endsWith(fileExtension)) {
                    files.add(file);
                }
            }
        } else {
            System.err.println("Provide a corpus name, choose between PolMine and taz.");
        }
        return files;
    }

    public static Reader getReader(String corpus, Layer layer) throws IOException {
        //Reader read = new ReaderPolMineXML(layer);
        Reader read = new ReaderPolMineCoNLLXML(layer);
        if (corpus.equalsIgnoreCase("taz")) {
            read = new ReaderTaz(layer);
        } else if (corpus.equalsIgnoreCase("PolMine")) {
            //read = new ReaderPolMineXML(layer);
            read = new ReaderPolMineCoNLLXML(layer);
        } else {
            System.err.println("Provide a corpus name, choose between PolMine and taz.");
        }
        return read;
    }

    public static String getExtension(String corpus) {
        String fileExtension = "";
        if (corpus.equalsIgnoreCase("taz")) {
            fileExtension = ".conll.gz";
        } else if (corpus.equalsIgnoreCase("PolMine")) {
            //fileExtension = ".xml";
            fileExtension = ".conll.gz";
        } else {
            throw new IllegalArgumentException(String.format("Unknown corpus type: %s", corpus));
        }
        return fileExtension;
    }

}
