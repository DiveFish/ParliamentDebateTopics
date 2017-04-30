package compact;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricia on 29/04/17.
 */
public class SentenceLength {

    public static void main(String[] args) throws IOException {
        File taz_sentsPerFile = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/taz_sentencesPerFile.txt");
        File taz_wordsPerFile = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/taz_wordsPerFile.txt");

        File taz_sentsPerSection = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/taz_sentencesPerSection.txt");
        File taz_wordsPerSection = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/taz_wordsPerSection.txt");

        File polMine_sentsPerFile = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_sentencesPerFile.txt");
        File polMine_wordsPerFile = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_wordsPerFile.txt");

        File polMine_sentsPerSection = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_sentencesPerSection.txt");
        File polMine_wordsPerSection = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_wordsPerSection.txt");

        InputStreamReader isr1 = new InputStreamReader(new FileInputStream(taz_sentsPerFile));
        BufferedReader br_taz_sentFile = new BufferedReader(isr1);

        InputStreamReader isr2 = new InputStreamReader(new FileInputStream(taz_wordsPerFile));
        BufferedReader br_taz_wordFile = new BufferedReader(isr2);

        InputStreamReader isr3 = new InputStreamReader(new FileInputStream(taz_sentsPerSection));
        BufferedReader br_taz_sentSection = new BufferedReader(isr3);

        InputStreamReader isr4 = new InputStreamReader(new FileInputStream(taz_wordsPerSection));
        BufferedReader br_taz_wordSection = new BufferedReader(isr4);

        InputStreamReader isr5 = new InputStreamReader(new FileInputStream(polMine_sentsPerFile));
        BufferedReader br_polMine_sentFile = new BufferedReader(isr5);

        InputStreamReader isr6 = new InputStreamReader(new FileInputStream(polMine_wordsPerFile));
        BufferedReader br_polMine_wordFile = new BufferedReader(isr6);

        InputStreamReader isr7 = new InputStreamReader(new FileInputStream(polMine_sentsPerSection));
        BufferedReader br_polMine_sentSection = new BufferedReader(isr7);

        InputStreamReader isr8 = new InputStreamReader(new FileInputStream(polMine_wordsPerSection));
        BufferedReader br_polMine_wordSection = new BufferedReader(isr8);

        List<String> taz_file_avgSentLength = new ArrayList<>();
        List<String> taz_section_avgSentLength = new ArrayList<>();
        List<String> polMine_file_avgSentLength = new ArrayList<>();
        List<String> polMine_section_avgSentLength = new ArrayList<>();

        String sentCount = "";
        String wordCount = "";

        while ((sentCount = br_taz_sentFile.readLine()) != null && (wordCount = br_taz_wordFile.readLine())!=null) {
            float avgSentLength = Float.parseFloat(wordCount) / Float.parseFloat(sentCount);
            taz_file_avgSentLength.add((avgSentLength)+"");
        }

        while ((sentCount = br_taz_sentSection.readLine()) != null && (wordCount = br_taz_wordSection.readLine())!=null) {
            float avgSentLength = Float.parseFloat(wordCount) / Float.parseFloat(sentCount);
            taz_section_avgSentLength.add((avgSentLength)+"");
        }

        while ((sentCount = br_polMine_sentFile.readLine()) != null && (wordCount = br_polMine_wordFile.readLine())!=null) {
            float avgSentLength = Float.parseFloat(wordCount) / Float.parseFloat(sentCount);
            polMine_file_avgSentLength.add((avgSentLength)+"");
        }

        while ((sentCount = br_polMine_sentSection.readLine()) != null && (wordCount = br_polMine_wordSection.readLine())!=null) {
            float avgSentLength = Float.parseFloat(wordCount) / Float.parseFloat(sentCount);
            polMine_section_avgSentLength.add((avgSentLength)+"");
        }

        Path taz_file_avg = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/taz_file_averageSentenceLength.txt");
        FileWriter fwTazWordsPerFile = new FileWriter(new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/taz_file_averageSentenceLength.txt"),true);
        Files.write(taz_file_avg, taz_file_avgSentLength, Charset.forName("UTF-8"));
        fwTazWordsPerFile.close();

        Path taz_section_avg = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/taz_section_averageSentenceLength.txt");
        FileWriter fwTazWordsPerSection = new FileWriter(new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/taz_section_averageSentenceLength.txt"),true);
        Files.write(taz_section_avg, taz_section_avgSentLength, Charset.forName("UTF-8"));
        fwTazWordsPerSection.close();

        Path polMine_file_avg = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_file_averageSentenceLength.txt");
        FileWriter fwPolWordsPerFile = new FileWriter(new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_file_averageSentenceLength.txt"),true);
        Files.write(polMine_file_avg, polMine_file_avgSentLength, Charset.forName("UTF-8"));
        fwPolWordsPerFile.close();

        Path polMine_section_avg = Paths.get("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_section_averageSentenceLength.txt");
        FileWriter fwPolWordsPerSection = new FileWriter(new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/counts/PolMine_section_averageSentenceLength.txt"),true);
        Files.write(polMine_section_avg, polMine_section_avgSentLength, Charset.forName("UTF-8"));
        fwPolWordsPerSection.close();
    }
}
