package compact;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Read XML files of parliamentary debates, one debate with several debate
 * sections per file. Save debate IDs and debate content (words and their
 * respective frequency). Do not include stopwords.

 * @author Daniël de Kok and Patricia Fischer
 */
public class ReaderPolMineXML implements Reader {
    
    private final Layer layer;

    private final Map<String, List<String>> debateMetadata;  // file ID <-> date - speaker - party
    
    private final SAXParserFactory parserFactory;
    
    private static List<Map<String, Integer>> debateContent;
    
    private static List<String> debateIds;
    
    private static String debateId;

    private static List<String> wordCount;
    private static int words;
    private static List<String> sentCount;
    private static int sents;

    private int processed;
    private int allProcessed = 984;
    
    public ReaderPolMineXML(Layer layer) {
        this.layer = layer;
        debateMetadata = new HashMap();
        parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(false);
        parserFactory.setValidating(false);
        parserFactory.setXIncludeAware(false);

        processed = 0;
        wordCount = new ArrayList<>();
        sentCount = new ArrayList<>();
    }

    /**
     *
     * @param xmlFile The directory which stores the xml files
     * @throws IOException
     */
    @Override
    public void processFile(File xmlFile) throws IOException{
        debateContent = new ArrayList();
        debateIds = new ArrayList();
        try {
            SAXParser saxParser = parserFactory.newSAXParser();
            XMLHandler xmlHandler = new XMLHandler();
            
            debateId = xmlFile.getName();
            if (debateId.endsWith(".xml")){
                  //System.err.println(String.format("Parsing file %s ...", debateId));
                  saxParser.parse(xmlFile, xmlHandler);
                  processed++;
            }
            // Save statistics to files only after all files have been processed
            if (processed == allProcessed) {
                System.err.println("-- SAVING COUNTS --");
                File polMineSecWordCount = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_wordsPerSection.txt");
                FileWriter fwWordsPerSection = new FileWriter(polMineSecWordCount, true);
                Path wordsPerSection = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_wordsPerSection.txt");
                Files.write(wordsPerSection, wordCount, Charset.forName("UTF-8"));
                fwWordsPerSection.close();

                File polMineSecSentCount = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_sentencesPerSection.txt");
                FileWriter fwWSentPerSection = new FileWriter(polMineSecSentCount, true);
                Path sentsPerSection = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_sentencesPerSection.txt");
                Files.write(sentsPerSection, sentCount, Charset.forName("UTF-8"));
                fwWSentPerSection.close();
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public List<Map<String, Integer>> getContent() {
        return debateContent;
    }
    
    @Override
    public List<String> getSectionIDs() {
        return debateIds;
    }
    
    @Override
    public Map<String, List<String>> getMetadata() {
        return debateMetadata;
    }
    
    private class XMLHandler extends DefaultHandler {
    
        private Map<String, Integer> content;
        private final List<String> metadata;
        private String date;
        private int sectionId;

        private boolean inDate;
        private boolean inSentence;
        private boolean inToken;
        
        private final Set<String> stopwords;

        public XMLHandler() throws IOException {
            this.stopwords = Stopwords.stopwords();
            content = new HashMap();
            metadata = new ArrayList<String>() {{add(""); add(""); add("");}};
            date = "";
            sectionId = 1;
            inDate = false;
            inSentence = false;
            inToken = false;
        }

        public Map<String, Integer> debate() {
            return content;
        }
        
        public List<String> metadata() {
            return metadata;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inToken) {
                // Remove all leading and trailing punctuation?
                String word = new String(ch, start, length).trim();//.replaceFirst("^[^a-zA-Z]+", "").replaceAll("[^a-zA-Z]+$", "");
               /*
                StringBuilder, iterate over array character by character and check if its a character
                function which tells whether sth is considered unicode <-> punctuation
                */
                
                StringBuilder sb = new StringBuilder();
                for(char c : word.toCharArray()) { 
                    int cInt = (int)c;
                    // numbers; characters and Umlaute (upper +  lower case); "ß"; "/" (as in "CDU/CSU); "-" (hyphen)
                    if (cInt == (38) || cInt == 45 || cInt == 128 || cInt == 196 || cInt == 214 || cInt == 220 || cInt == 223 || cInt == 228 || cInt == 246 || cInt == 252 || (cInt < 58 && cInt > 46) || (cInt < 91 && cInt > 64) || (cInt < 123 && cInt > 96)) {
                        sb.append(c);
                    }                    
                }
                
                word = sb.toString();
                
                if (!stopwords.contains(word.toLowerCase()) && (word.length() > 0)) {
                    if (!content.containsKey(word)) {
                        content.putIfAbsent(word, 1);
                    }
                    else {
                        content.put(word, content.get(word)+1);
                    }
                }
            }
            if (inDate) {
                date = new String(ch, start, length).trim(); //1996-02-08
                // Convert to same format as taz article dates: 08.02.1996
                date = date.substring(8)+"."+date.substring(5, 7)+"."+date.substring(0,4);
            }
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
            switch (qName) {
                case "sentence":
                    inSentence = true;
                case "token":
                    inToken = true;
                    break;
                case "body":
                    break;
                case "speaker":
                    words = 0;
                    sents = 0;
                    content = new HashMap();
                    metadata.set(0, date);
                    metadata.set(1, attrs.getValue("name"));
                    metadata.set(2, attrs.getValue("party"));
                    break;
                case "date":
                    inDate = true;
                    break;
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            switch (qName) {
                case "sentence":
                    sents++;
                    inSentence = false;
                case "token":
                    words++;
                    inToken = false;
                    break;
                case "body":
                    break;
                case "speaker":
                    wordCount.add(words+"");
                    sentCount.add(sents+"");
                    debateIds.add(debateId+"_"+sectionId);
                    debateContent.add(content);
                    debateMetadata.putIfAbsent(debateId+"_"+sectionId, metadata);
                    sectionId++;
                    break;
                case "date":
                    inDate = false;
                    break;
            }
        }
    }
}
