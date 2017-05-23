package compact;

import com.google.common.collect.BiMap;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;
import gnu.trove.list.TIntList;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.math3.util.Pair;

/**
 * Read CONLL files from PolMine corpus. Calculate number of
 * words and sentences.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class ConllUtilsPolMine {

    private static int count = 0;

    public static void main(String[] args) throws IOException {
        new ConllUtilsPolMine();
    }

    public ConllUtilsPolMine() throws IOException {

        List<String> sentenceFileCount = new ArrayList<>();
        List<String> wordFileCount = new ArrayList<>();

        File directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/polmine-conll");
        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".conll.gz")) {
                processFile(file, sentenceFileCount, wordFileCount);

            }
        }

        File fileSentences = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_sentencesPerFile.txt");
        FileWriter fwSentencesPerFile = new FileWriter(fileSentences,true); //the true will append the new data
        Path sentsPerFile = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_sentencesPerFile.txt");
        Files.write(sentsPerFile, sentenceFileCount, Charset.forName("UTF-8"));
        fwSentencesPerFile.close();

        File fileWords = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_wordsPerFile.txt");
        FileWriter fwWordsPerFile = new FileWriter(fileWords,true);
        Path wordsPerFile = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_wordsPerFile.txt");
        Files.write(wordsPerFile, wordFileCount, Charset.forName("UTF-8"));
        fwWordsPerFile.close();

    }

    /**
     *
     * @param conllFile The PolMine debates in CONLL format
     * @throws IOException
     */
    public void processFile(File conllFile, List<String> sentencesPerFile, List<String> wordsPerFile) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {

            int sentsPerFile = 0;
            int toksPerFile = 0;
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {
                List<Token> sent = sentence.getTokens();
                sentsPerFile++;
                toksPerFile += sent.size();
            }
            if (sentsPerFile > 0) {
                sentencesPerFile.add(sentsPerFile + "");
            }
            if (toksPerFile > 0) {
                wordsPerFile.add(toksPerFile + "");
            }
            count++;
            System.out.println(count+" files done");
        }
    }
}
