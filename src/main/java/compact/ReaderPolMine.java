package compact;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
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
public class ReaderPolMine implements Reader {
    
    private final Layer layer;

    private final Map<String, List<String>> debateMetadata;  // file ID <-> date - speaker - party
    
    private final SAXParserFactory parserFactory;
    
    private static List<Map<String, Integer>> debateContent;
    
    private static List<String> debateIds;
    
    public ReaderPolMine(Layer layer) {
        this.layer = layer;
        debateMetadata = new HashMap();
        parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(false);
        parserFactory.setValidating(false);
        parserFactory.setXIncludeAware(false);
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
            
            String debateID = xmlFile.getName();
            if (debateID.endsWith(".xml")){
                  System.out.println(String.format("Parsing file %s ...", debateID));
                  saxParser.parse(xmlFile, xmlHandler);
                  debateIds.add(debateID);
                  debateContent.add(xmlHandler.debate());
                  debateMetadata.putIfAbsent(debateID, xmlHandler.metadata());
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
        private List<String> metadata;

        private boolean inDate;
        private boolean inToken;
        
        private final Set<String> stopwords;

        public XMLHandler() throws IOException {
            this.stopwords = Stopwords.stopwords();
            content = new HashMap();
            metadata = new ArrayList<String>() {{add(""); add(""); add("");}};
            inDate = false;
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
                String day = new String(ch, start, length).trim(); //1996-02-08
                int l = day.length();
                // Convert to same format as taz article dates: 08.02.1996
                day = day.substring(l-2)+"."+day.substring(l-5, l-3)+"."+day.substring(0,l-6);
                metadata.set(0, day);
            }
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
            switch (qName) {
                case "token":
                    inToken = true;
                    break;
                case "body":
                    content = new HashMap();
                    break;
                case "speaker":
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
                case "token":
                    inToken = false;
                    break;
                case "body":
                    break;
                case "date":
                    inDate = false;
                    break;
            }
        }
    }
}
