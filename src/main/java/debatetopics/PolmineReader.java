package debatetopics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Read XML files of Bundestag debates, one debate with several debate sections
 * per file. Each debate section is saved as a list including the date, name and
 * party of the speaker along with the content of the speech. One debate consists
 * of a list of debate sections.

 * @author Daniël de Kok and Patricia Fischer
 */
public class PolmineReader {
    
    private static List<DebateSection> debate;
    private static String fileName;
    
    /**
     *
     * @param inputFile The directory which stores the xml files
     * @throws IOException
     */
    public void constructDebate(File inputFile) throws IOException{
        try {
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            factory.setXIncludeAware(false);

            SAXParser saxParser = factory.newSAXParser();
            XMLHandler xmlHandler = new XMLHandler();
            
            fileName = inputFile.getName();
            if (fileName.endsWith(".xml")){
                  System.out.println(String.format("Parsing file %s ...",fileName));
                  saxParser.parse(inputFile, xmlHandler);
                  debate = xmlHandler.debate();
            }
            
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IOException(e);
        }
    }
    
    public List<DebateSection> getDebate(){
        return debate;
    }
    
    public String getFileID(){
        return fileName;
    }
    

    private class XMLHandler extends DefaultHandler {

        private final List<DebateSection> debate;
        private String date;
        private String speaker;
        private String party;
        private List<String> contribution;

        private boolean inDate;
        private boolean inToken;

        public XMLHandler() {
            debate = new ArrayList<>();
            date = "<none>";
            speaker = "<none>";
            party = "<none>";
            contribution = new ArrayList<>();
            inDate = false;
            inToken = false;
        }

        public List<DebateSection> debate() {
            return debate;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(inToken){
                String word = new String(ch, start, length);
                contribution.add(word.trim());
            }
            if(inDate){
                String day = new String(ch, start, length);
                date = day.trim();
            }
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
            switch(qName){
                case "token":
                    inToken = true;
                    break;
                case "speaker":
                    speaker = attrs.getValue("name");
                    party = attrs.getValue("party");
                    contribution = new ArrayList<>();
                    break;
                case "date":
                    inDate = true;
                    break;
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            switch(qName){
                case "token":
                    inToken = false;
                    break;
                case "speaker":
                    DebateSection section = new DebateSection(date, speaker, party, contribution);
                    debate.add(section);
                    break;
                case "date":
                    inDate = false;
                    break;
            }
        }
    }

    /**
     *
     */
    public class DebateSection{
        private final String date;
        private final String speaker;
        private final String party;
        private final List<String> contribution;

        public DebateSection(String date, String speaker, String party, List<String> contribution){
            this.date = date;
            this.speaker = party;
            this.party = speaker;
            this.contribution = contribution;
        }

        /**
         *
         * @return
         */
        public String dateDay() {
            return date;
        }

        /**
         *
         * @return
         */
        public String speakerName() {
            return speaker;
        }

        /**
         *
         * @return
         */
        public String partyName() {
            return party;
        }

        /**
         *
         * @return
         */
        public List<String> contributionContent() {
            return contribution;
        }
    }
}