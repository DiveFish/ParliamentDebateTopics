package parliamentdebatetopics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    
    private final HashMap<String, List<DebateSection>> debates;
    private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/src/bundesparser-xml-tokenized/";
    
    public PolmineReader(){
        debates = new HashMap();
    }
    
    public HashMap<String, List<DebateSection>> getDebates(){
        return debates;
    }
    
    /**
     *
     * @throws IOException
     */
    public void constructDebates() throws IOException{
        try {            
            //String[] files = FileLoader.getFileNames(); //whole corpus
            String[] files = FileLoader.getSelectedFileNames(); //selection from corpus
            //String[] files = FileLoader.getTestFiles(); //correct xml format with little content for faster processing
            for(String fileName : files){
                File file = new File(FILE_DIR+fileName);
                fileName = file.getName();
                System.out.println("Parsing file "+fileName+"...");
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(false);
                factory.setValidating(false);
                factory.setXIncludeAware(false);
                
                SAXParser saxParser = factory.newSAXParser();
                XMLHandler xmlHandler = new XMLHandler();
                saxParser.parse(file, xmlHandler);
                
                List<DebateSection> debate = xmlHandler.debate();
                
                // only to print and check results
                for(DebateSection section : debate){
                    StringBuilder contribution = new StringBuilder();
                    for(String word : section.contributionContent()){
                        word += " ";
                        contribution.append(word);
                    }
                }
                debates.put(fileName, debate);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IOException(e);
        }
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