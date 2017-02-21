package compact;

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
    
    private static List<Map<String, Integer>> debateContent;
    
    private static List<String> debateIDs;

    private final SAXParserFactory parserFactory;

    public ReaderPolMine(Layer layer) {
        this.layer = layer;
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
        debateIDs = new ArrayList();
        try {
            SAXParser saxParser = parserFactory.newSAXParser();
            XMLHandler xmlHandler = new XMLHandler();
            
            String debateID = xmlFile.getName();
            if (debateID.endsWith(".xml")){
                  System.out.println(String.format("Parsing file %s ...", debateID));
                  saxParser.parse(xmlFile, xmlHandler);
                  debateIDs.add(debateID);
                  debateContent.add(xmlHandler.debate());
            }
            
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public List<Map<String, Integer>> getContent(){
        return debateContent;
    }
    
    @Override
    public List<String> getSectionIDs(){
        return debateIDs;
    }
    

    private class XMLHandler extends DefaultHandler {
    
        private Map<String, Integer> content;

        private boolean inToken;
        
        private final Set<String> stopwords;

        public XMLHandler() throws IOException {
            this.stopwords = Stopwords.stopwords();
            content = new HashMap();
            inToken = false;
        }

        public Map<String, Integer> debate() {
            return content;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inToken) {
                String word = new String(ch, start, length);
                if (!stopwords.contains(word.trim())){
                    if (!content.containsKey(word)) {
                        content.putIfAbsent(word, 1);
                    }
                    else {
                        content.put(word, content.get(word)+1);
                    }
                }
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
            }
        }
    }
}
