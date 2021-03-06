package io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.collect.ImmutableSet;
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
public class PolMineReader {
    
    private static List<DebateSection> debate;
    
    private static String debateID;

    private final SAXParserFactory parserFactory;

    public PolMineReader() {
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
    public void constructDebate(File xmlFile) throws IOException{
        try {
            SAXParser saxParser = parserFactory.newSAXParser();
            XMLHandler xmlHandler = new XMLHandler();
            
            debateID = xmlFile.getName();
            if (debateID.endsWith(".xml")){
                  System.out.println(String.format("Parsing file %s ...", debateID));
                  saxParser.parse(xmlFile, xmlHandler);
                  debate = xmlHandler.debate();
            }
            
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IOException(e);
        }
    }
    
    public List<DebateSection> getDebate(){
        return debate;
    }
    
    public String getDebateID(){
        return debateID;
    }
    

    private class XMLHandler extends DefaultHandler {

        private final List<DebateSection> debate;
        private String date;
        private String speaker;
        private String party;
        private List<String> contribution;

        private boolean inDate;
        private boolean inToken;
        
        private final Set<String> STOPWORDS = ImmutableSet.of("ab", "aber", "ähnlich", "alle", "allein", "allem", "allen", "aller", "allerdings", "allerlei", "alles", "allmählich", "allzu", "als", "alsbald", "also", "am", "an", "and", "ander", "andere", "anderem", "anderen", "anderer", "andererseits", "anderes", "anderm", "andern", "andernfalls", "anders", "anstatt", "auch", "auf", "aus", "ausgenommen", "ausser", "außer", "ausserdem", "außerdem", "außerhalb", "bald", "bei", "beide", "beiden", "beiderlei", "beides", "beim", "beinahe", "bereits", "besonders", "besser", "beträchtlich", "bevor", "bezüglich", "bin", "bis", "bisher", "bislang", "bist", "bloß", "bsp.", "bzw", "ca", "ca.", "content", "da", "dabei", "dadurch", "dafür", "dagegen", "daher", "dahin", "damals", "damit", "danach", "daneben", "dann", "daran", "darauf", "daraus", "darin", "darüber", "darüberhinaus", "darum", "darunter", "das", "daß", "dass", "dasselbe", "davon", "davor", "dazu", "dein", "deine", "deinem", "deinen", "deiner", "deines", "dem", "demnach", "demselben", "den", "denen", "denn", "dennoch", "denselben", "der", "derart", "derartig", "derem", "deren", "derer", "derjenige", "derjenigen", "derselbe", "derselben", "derzeit", "des", "deshalb", "desselben", "dessen", "desto", "deswegen", "dich", "die", "diejenige", "dies", "diese", "dieselbe", "dieselben", "diesem", "diesen", "dieser", "dieses", "diesseits", "dir", "direkt", "direkte", "direkten", "direkter", "doch", "dort", "dorther", "dorthin", "drauf", "drin", "drüber", "drunter", "du", "dunklen", "durch", "durchaus", "eben", "ebenfalls", "ebenso", "eher", "eigenen", "eigenes", "eigentlich", "ein", "eine", "einem", "einen", "einer", "einerseits", "eines", "einfach", "einführen", "einführte", "einführten", "eingesetzt", "einig", "einige", "einigem", "einigen", "einiger", "einigermaßen", "einiges", "einmal", "eins", "einseitig", "einseitige", "einseitigen", "einseitiger", "einst", "einstmals", "einzig", "entsprechend", "entweder", "er", "erst", "es", "etc", "etliche", "etwa", "etwas", "euch", "euer", "eure", "eurem", "euren", "eurer", "eures", "falls", "fast", "ferner", "folgende", "folgenden", "folgender", "folgendes", "folglich", "fuer", "für", "gab", "ganze", "ganzem", "ganzen", "ganzer", "ganzes", "gänzlich", "gar", "gegen", "gemäss", "ggf", "gleich", "gleichwohl", "gleichzeitig", "glücklicherweise", "hab", "habe", "haben", "haette", "hast", "hat", "hätt", "hatte", "hätte", "hatten", "hätten", "hattest", "hattet", "heraus", "herein", "hier", "hiermit", "hiesige", "hin", "hinein", "hinten", "hinter", "hinterher", "höchstens", "http", "ich", "igitt", "ihm", "ihn", "ihnen", "ihr", "ihre", "ihrem", "ihren", "ihrer", "ihres", "im", "immer", "immerhin", "in", "indem", "indessen", "infolge", "innen", "innerhalb", "ins", "insofern", "inzwischen", "irgend", "irgendeine", "irgendwas", "irgendwen", "irgendwer", "irgendwie", "irgendwo", "ist", "ja", "jährig", "jährige", "jährigen", "jähriges", "je", "jed", "jede", "jedem", "jeden", "jedenfalls", "jeder", "jederlei", "jedes", "jedoch", "jemand", "jene", "jenem", "jenen", "jener", "jenes", "jenseits", "jetzt", "kam", "kann", "kannst", "kaum", "kein", "keine", "keinem", "keinen", "keiner", "keinerlei", "keines", "keineswegs", "klar", "klare", "klaren", "klares", "klein", "kleinen", "kleiner", "kleines", "koennen", "koennt", "koennte", "koennten", "komme", "kommen", "kommt", "konkret", "konkrete", "konkreten", "konkreter", "konkretes", "können", "künftig", "leider", "man", "manche", "manchem", "manchen", "mancher", "mancherorts", "manches", "manchmal", "mehr", "mehrere", "mein", "meine", "meinem", "meinen", "meiner", "meines", "mich", "mir", "mit", "mithin", "muessen", "muesst", "muesste", "muss", "muß", "müssen", "musst", "mußt", "müßt", "musste", "müsste", "müßte", "mussten", "müssten", "nach", "nachdem", "nachher", "nachhinein", "nächste", "nahm", "nämlich", "natürlich", "neben", "nebenan", "nehmen", "nicht", "nichts", "nie", "niemals", "niemand", "nirgends", "nirgendwo", "noch", "nötigenfalls", "nun", "nur", "ob", "oben", "oberhalb", "obgleich", "obschon", "obwohl", "oder", "oft", "per", "plötzlich", "schließlich", "schon", "sehr", "sehrwohl", "sein", "seine", "seinem", "seinen", "seiner", "seines", "seit", "seitdem", "seither", "selber", "selbst", "sich", "sicher", "sicherlich", "sie", "sind", "so", "sobald", "sodass", "sodaß", "soeben", "sofern", "sofort", "sogar", "solange", "solch", "solche", "solchem", "solchen", "solcher", "solches", "soll", "sollen", "sollst", "sollt", "sollte", "sollten", "solltest", "somit", "sondern", "sonst", "sonstwo", "sooft", "soviel", "soweit", "sowie", "sowohl", "tatsächlich", "tatsächlichen", "tatsächlicher", "tatsächliches", "trotzdem", "übel", "über", "überall", "überallhin", "überdies", "übermorgen", "übrig", "übrigens", "ueber", "um", "umso", "unbedingt", "und", "unmöglich", "unmögliche", "unmöglichen", "unmöglicher", "uns", "unser", "unsere", "unserem", "unseren", "unserer", "unseres", "unter", "usw", "viel", "viele", "vielen", "vieler", "vieles", "vielleicht", "vielmals", "völlig", "vom", "von", "vor", "voran", "vorher", "vorüber", "während", "währenddessen", "wann", "war", "wär", "wäre", "waren", "wären", "warst", "warum", "was", "weder", "weil", "weiß", "weiter", "weitere", "weiterem", "weiteren", "weiterer", "weiteres", "weiterhin", "welche", "welchem", "welchen", "welcher", "welches", "wem", "wen", "wenig", "wenige", "weniger", "wenigstens", "wenn", "wenngleich", "wer", "werde", "werden", "werdet", "weshalb", "wessen", "wichtig", "wie", "wieder", "wieso", "wieviel", "wiewohl", "will", "willst", "wir", "wird", "wirklich", "wirst", "wo", "wodurch", "wogegen", "woher", "wohin", "wohingegen", "wohl", "wohlweislich", "womit", "woraufhin", "woraus", "worin", "wurde", "würde", "wurden", "würden", "zB", "z.B.", "zahlreich", "zeitweise", "zu", "zudem", "zuerst", "zufolge", "zugleich", "zuletzt", "zum", "zumal", "zur", "zurück", "zusammen", "zuviel", "zwar", "zwischen");

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
            if (inToken) {
                String word = new String(ch, start, length);
                if (!STOPWORDS.contains(word.trim())){
                    contribution.add(word.trim());
                }
            }
            if (inDate) {
                String day = new String(ch, start, length);
                date = day.trim();
            }
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
            switch (qName) {
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
            switch (qName) {
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

        public String dateDay() {
            return date;
        }

        public String speakerName() {
            return speaker;
        }

        public String partyName() {
            return party;
        }

        public List<String> contributionContent() {
            return contribution;
        }
    }
}