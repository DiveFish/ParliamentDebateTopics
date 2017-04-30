package compact;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Read CONLL files from taz corpus. Save article IDs and article content
 * (words and their respective frequency). Do not include stopwords.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class ConllUtilsTaz {

    private static int count = 0;

    private final Layer layer;

    private static List<String> sectionIds; // IDs of all sections

    private static final Pattern P_ID = Pattern.compile("nr:([0-9]+)");

    public static void main(String[] args) throws IOException {
        ConllUtilsTaz cu = new ConllUtilsTaz(Layer.LEMMA);
    }

    public ConllUtilsTaz(Layer layer) throws IOException {
        this.layer = layer;

        List<String> sentenceFileCount = new ArrayList<>();
        List<String> sentenceSectionCount = new ArrayList<>();
        List<String> wordFileCount = new ArrayList<>();
        List<String> wordSectionCount = new ArrayList<>();

        File directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/taz");
        for (File dir : directory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".conll.gz")) {
                    processFile(file, sentenceFileCount, sentenceSectionCount, wordFileCount, wordSectionCount);
                }
            }
        }

        File fileSentences = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/sentencesPerFile.txt");
        Path sentsPerFile = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/sentencesPerFile.txt");
        FileWriter fwSentencesPerFile = new FileWriter(fileSentences,true); //the true will append the new data
        Files.write(sentsPerFile, sentenceFileCount, Charset.forName("UTF-8"));
        fwSentencesPerFile.close();

        File sectionSentences = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/sentencesPerSection.txt");
        Path sentsPerSection = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/sentencesPerSection.txt");
        FileWriter fwSentencesPerSection = new FileWriter(sectionSentences,true);
        Files.write(sentsPerSection, sentenceSectionCount, Charset.forName("UTF-8"));
        fwSentencesPerSection.close();

        File fileWords = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/wordsPerFile.txt");
        Path wordsPerFile = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/wordsPerFile.txt");
        FileWriter fwWordsPerFile = new FileWriter(fileWords,true);
        Files.write(wordsPerFile, wordFileCount, Charset.forName("UTF-8"));
        fwWordsPerFile.close();

        File sectionWords = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/wordsPerSection.txt");
        Path wordsPerSection = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/wordsPerSection.txt");
        FileWriter fwWordsPerSection = new FileWriter(sectionWords,true);
        Files.write(wordsPerSection, wordSectionCount, Charset.forName("UTF-8"));
        fwWordsPerSection.close();

    }

    /**
     *
     * @param conllFile The newspaper file in CONLL format
     * @throws IOException
     */
    public void processFile(File conllFile, List<String> sentencesPerFile, List<String> sentencesPerSection,
                            List<String> wordsPerFile, List<String> wordsPerSection) throws IOException {
        sectionIds = new ArrayList();
        String fileId = conllFile.getName();

        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {

            int sentsPerFile = 0;
            int sentsPerSection = 0;

            int toksPerFile = 0;
            int toksPerSection = 0;
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {
                List<Token> sent = sentence.getTokens();

                String feats = sent.get(0).getFeatures().or("_");

                Matcher mID = P_ID.matcher(feats);
                int tokenId = 0;
                if (mID.find()) {
                    tokenId = Integer.parseInt(mID.group(0).substring(3));   //Find regex "nr:[0-9]+" in features
                }
                else {
                    System.err.printf("No ID found in article %s", fileId);
                }
                String sectionId = fileId + "_" + tokenId;

                // Encountered new article section
                if (!sectionIds.contains(sectionId)) {
                    sectionIds.add(sectionId);

                    if (sentsPerSection > 0) {
                        sentencesPerSection.add(sentsPerSection + "");
                    }
                    sentsPerSection = 0;

                    if(toksPerSection > 0) {
                        wordsPerSection.add(toksPerSection + "");
                    }
                    toksPerSection = 0;
                }
                sentsPerSection++;
                sentsPerFile++;

                toksPerSection += sent.size();
                toksPerFile += sent.size();
            }
            if(sentsPerFile > 0) {
                sentencesPerFile.add(sentsPerFile + "");
            }
            if(toksPerFile > 0) {
                wordsPerFile.add(toksPerFile + "");
            }
            count++;
            System.out.println(count+" files done");
        }
    }
}
