package compact;

import com.google.common.collect.BiMap;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;
import gnu.trove.list.TIntList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import org.apache.commons.math3.util.Pair;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Read CONLL files from PolMine corpus. Save debate IDs and debate content
 * (words and their respective frequency). Do not include stopwords.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class ReaderPolMineCoNLLXML implements Reader {

    private final Layer layer;

    private final Map<String, List<String>> debateMetadata;  // section/file ID <-> date

    private final SAXParserFactory parserFactory;

    private final Set<String> stopwords;

    private static List<String> debateIds;

    private static List<Map<String, Integer>> fileContent;  // content of all sections, each section one HashMap of tokens and their frequencies

    private static String date;

    public ReaderPolMineCoNLLXML(Layer layer) throws IOException {
        this.layer = layer;
        debateMetadata = new HashMap();
        this.stopwords = Stopwords.stopwords();

        parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(false);
        parserFactory.setValidating(false);
        parserFactory.setXIncludeAware(false);
    }

    /**
     *
     * @param conllFile The article file in CONLL format
     * @throws IOException
     */
    @Override
    public void processFile(File conllFile) throws IOException {
        fileContent = new ArrayList();
        fileContent.add(new HashMap<>());

        String fileId = conllFile.getName();

        debateIds = new ArrayList<>();
        debateIds.add(fileId);

        Map<String, Integer> wordFrequencies = fileContent.get(debateIds.indexOf(fileId));

        //System.err.println(String.format("Processing file %s", fileId));

        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {
                List<Token> sent = sentence.getTokens();

                for (Token token : sent) {
                    String value = layer == Layer.LEMMA ?
                            token.getLemma().or("_") :
                            token.getForm().or("_");
                    if (stopwords.contains(value.toLowerCase())) {
                        continue;
                    }

                    // Exclude all words except proper nouns or proper and common nouns
                    //if (!token.getPosTag().or("_").equals("TRUNC")) {
                    //if (!token.getPosTag().or("_").equals("VVPP")) {
                    //if (!token.getPosTag().or("_").equals("NE")) {

                    // Other
                    //if (!token.getPosTag().or("_").equals("CARD") || token.getPosTag().or("_").equals("FM")
                    //        || token.getPosTag().or("_").equals("XY")) {
                    // Verbal
                    //if (!token.getPosTag().or("_").equals("VVFIN") || token.getPosTag().or("_").equals("VVINF")
                    //        || token.getPosTag().or("_").equals("VVIZU")|| token.getPosTag().or("_").equals("VVPP")) {
                    // Adjectival/adverbial
                    //if (!token.getPosTag().or("_").equals("ADJA") || token.getPosTag().or("_").equals("ADJD")||token.getPosTag().or("_").equals("ADV")) {
                    // Nominal
                    //if (!(token.getPosTag().or("_").equals("NN") || token.getPosTag().or("_").equals("NE")||token.getPosTag().or("_").equals("TRUNC"))) {
                    //COMBINED: NN, NE, TRUNC, ADJA, ADJD, CARD
                    if (!(token.getPosTag().or("_").equals("NN") || token.getPosTag().or("_").equals("NE") || token.getPosTag().or("_").equals("TRUNC") ||
                            token.getPosTag().or("_").equals("ADJA") || token.getPosTag().or("_").equals("ADJD") || token.getPosTag().or("_").equals("CARD"))) {
                            continue;
                    }

                    if (!wordFrequencies.containsKey(value)) {
                        wordFrequencies.putIfAbsent(value, 1);
                    }
                    else {
                        wordFrequencies.put(value, wordFrequencies.get(value)+1);
                    }
                }
            }
            readDate(conllFile);
            debateMetadata.putIfAbsent(fileId, Arrays.asList(date));
            fileContent.add(wordFrequencies);
        }
    }

    private void readDate(File conllFile) throws IOException {
        try {
            SAXParser saxParser = parserFactory.newSAXParser();
            XMLHandler xmlHandler = new XMLHandler();

            File xmlFile = new File(conllFile.getPath().substring(0, conllFile.getPath().length()-8)+"xml");
            String fileId = xmlFile.getName();
            if (fileId.endsWith(".xml") && xmlFile.exists()){
                //System.err.println(String.format("Getting date from xml file %s ...", fileId));
                saxParser.parse(xmlFile, xmlHandler);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IOException(e);
        }
    }

    private class XMLHandler extends DefaultHandler {
        private boolean inDate;

        public XMLHandler() throws IOException {
            inDate = false;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inDate) {
                date = new String(ch, start, length).trim(); //1996-02-08
                // Convert to same format as taz article dates: 08.02.1996
                date = date.substring(8)+"."+date.substring(5, 7)+"."+date.substring(0,4);
            }
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
            switch (qName) {
                case "date":
                    inDate = true;
                    break;
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            switch (qName) {
                case "date":
                    inDate = false;
                    break;
            }
        }
    }

    @Override
    public List<Map<String, Integer>> getContent() {
        return fileContent;
    }

    @Override
    public List<String> getSectionIDs() {
        return debateIds;
    }

    @Override
    public Map<String, List<String>> getMetadata() {
        return debateMetadata;
    }

    /**
     * Find the earliest document in the cluster. Sort documents by date and
     * return doc id with earliest date.
     *
     * @param cluster The cluster of documents
     * @param documentIndices The document indices
     * @return The doc id of earliest date in cluster
     */
    public int earliestDoc(TIntList cluster, BiMap<String, Integer> documentIndices) {
        SortedSet<Pair<Integer, Date>> sortedDates = new TreeSet<>((o1, o2) -> {
            int cmp = o1.getValue().compareTo(o2.getValue());
            if (cmp == 0) {
                return o1.getKey().compareTo(o2.getKey());
            }
            return -cmp;
        });

        Map<Integer, Date> dateIds = dateByID(documentIndices);

        for (int i = 0; i < dateIds.size(); i++) {
            sortedDates.add(new Pair<>(i, dateIds.get(i)));
        }
        System.out.printf("Date: %s, doc %s", sortedDates.first().getValue(), sortedDates.first().getKey());
        return sortedDates.first().getKey();
    }

    /**
     * Map each date to the document index. Original format is "file ID - doc idx"
     * and "file ID - date string", should be "doc idx - date"
     *
     * @param documentIndices The document indices
     * @return The dates by document indices
     */
    private Map<Integer, Date> dateByID(BiMap<String, Integer> documentIndices) {

        BiMap<Integer, String> docsByIdx = documentIndices.inverse();
        Map<Integer, Date> dateIDs = new HashMap();

        for (int i = 0; i < docsByIdx.size(); i++) {
            dateIDs.putIfAbsent(i, stringToDate(debateMetadata.get(docsByIdx.get(i)).get(0)));  // get date from metadata
        }
        return dateIDs;
    }

    /**
     * Convert string to date.
     *
     * @param dateString The string to be converted
     * @return The date
     */
    private Date stringToDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date date = new Date();
        try {
            date = formatter.parse(dateString);
            return date;

        } catch (ParseException e) {
            System.err.println("Cannot parse date");
        }
        return date;
    }
}
