package io;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import eu.danieldk.nlp.conllx.Token;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * See interface for description.
 * 
 * @author Daniël de Kok and Patricia Fischer
 */
public class VocabularyTaz implements Vocabulary {
    
    private final Layer layer;
    
    private final Set<String> STOPWORDS = ImmutableSet.of("ab", "aber", "ähnlich", "alle", "allein", "allem", "allen", "aller", "allerdings", "allerlei", "alles", "allmählich", "allzu", "als", "alsbald", "also", "am", "an", "and", "ander", "andere", "anderem", "anderen", "anderer", "andererseits", "anderes", "anderm", "andern", "andernfalls", "anders", "anstatt", "auch", "auf", "aus", "ausgenommen", "ausser", "außer", "ausserdem", "außerdem", "außerhalb", "bald", "bei", "beide", "beiden", "beiderlei", "beides", "beim", "beinahe", "bereits", "besonders", "besser", "beträchtlich", "bevor", "bezüglich", "bin", "bis", "bisher", "bislang", "bist", "bloß", "bsp.", "bzw", "ca", "ca.", "content", "da", "dabei", "dadurch", "dafür", "dagegen", "daher", "dahin", "damals", "damit", "danach", "daneben", "dann", "daran", "darauf", "daraus", "darin", "darüber", "darüberhinaus", "darum", "darunter", "das", "daß", "dass", "dasselbe", "davon", "davor", "dazu", "dein", "deine", "deinem", "deinen", "deiner", "deines", "dem", "demnach", "demselben", "den", "denen", "denn", "dennoch", "denselben", "der", "derart", "derartig", "derem", "deren", "derer", "derjenige", "derjenigen", "derselbe", "derselben", "derzeit", "des", "deshalb", "desselben", "dessen", "desto", "deswegen", "dich", "die", "diejenige", "dies", "diese", "dieselbe", "dieselben", "diesem", "diesen", "dieser", "dieses", "diesseits", "dir", "direkt", "direkte", "direkten", "direkter", "doch", "dort", "dorther", "dorthin", "drauf", "drin", "drüber", "drunter", "du", "dunklen", "durch", "durchaus", "eben", "ebenfalls", "ebenso", "eher", "eigenen", "eigenes", "eigentlich", "ein", "eine", "einem", "einen", "einer", "einerseits", "eines", "einfach", "einführen", "einführte", "einführten", "eingesetzt", "einig", "einige", "einigem", "einigen", "einiger", "einigermaßen", "einiges", "einmal", "eins", "einseitig", "einseitige", "einseitigen", "einseitiger", "einst", "einstmals", "einzig", "entsprechend", "entweder", "er", "erst", "es", "etc", "etliche", "etwa", "etwas", "euch", "euer", "eure", "eurem", "euren", "eurer", "eures", "falls", "fast", "ferner", "folgende", "folgenden", "folgender", "folgendes", "folglich", "fuer", "für", "gab", "ganze", "ganzem", "ganzen", "ganzer", "ganzes", "gänzlich", "gar", "gegen", "gemäss", "ggf", "gleich", "gleichwohl", "gleichzeitig", "glücklicherweise", "hab", "habe", "haben", "haette", "hast", "hat", "hätt", "hatte", "hätte", "hatten", "hätten", "hattest", "hattet", "heraus", "herein", "hier", "hiermit", "hiesige", "hin", "hinein", "hinten", "hinter", "hinterher", "höchstens", "http", "ich", "igitt", "ihm", "ihn", "ihnen", "ihr", "ihre", "ihrem", "ihren", "ihrer", "ihres", "im", "immer", "immerhin", "in", "indem", "indessen", "infolge", "innen", "innerhalb", "ins", "insofern", "inzwischen", "irgend", "irgendeine", "irgendwas", "irgendwen", "irgendwer", "irgendwie", "irgendwo", "ist", "ja", "jährig", "jährige", "jährigen", "jähriges", "je", "jed", "jede", "jedem", "jeden", "jedenfalls", "jeder", "jederlei", "jedes", "jedoch", "jemand", "jene", "jenem", "jenen", "jener", "jenes", "jenseits", "jetzt", "kam", "kann", "kannst", "kaum", "kein", "keine", "keinem", "keinen", "keiner", "keinerlei", "keines", "keineswegs", "klar", "klare", "klaren", "klares", "klein", "kleinen", "kleiner", "kleines", "koennen", "koennt", "koennte", "koennten", "komme", "kommen", "kommt", "konkret", "konkrete", "konkreten", "konkreter", "konkretes", "können", "künftig", "leider", "man", "manche", "manchem", "manchen", "mancher", "mancherorts", "manches", "manchmal", "mehr", "mehrere", "mein", "meine", "meinem", "meinen", "meiner", "meines", "mich", "mir", "mit", "mithin", "muessen", "muesst", "muesste", "muss", "muß", "müssen", "musst", "mußt", "müßt", "musste", "müsste", "müßte", "mussten", "müssten", "nach", "nachdem", "nachher", "nachhinein", "nächste", "nahm", "nämlich", "natürlich", "neben", "nebenan", "nehmen", "nicht", "nichts", "nie", "niemals", "niemand", "nirgends", "nirgendwo", "noch", "nötigenfalls", "nun", "nur", "ob", "oben", "oberhalb", "obgleich", "obschon", "obwohl", "oder", "oft", "per", "plötzlich", "schließlich", "schon", "sehr", "sehrwohl", "sein", "seine", "seinem", "seinen", "seiner", "seines", "seit", "seitdem", "seither", "selber", "selbst", "sich", "sicher", "sicherlich", "sie", "sind", "so", "sobald", "sodass", "sodaß", "soeben", "sofern", "sofort", "sogar", "solange", "solch", "solche", "solchem", "solchen", "solcher", "solches", "soll", "sollen", "sollst", "sollt", "sollte", "sollten", "solltest", "somit", "sondern", "sonst", "sonstwo", "sooft", "soviel", "soweit", "sowie", "sowohl", "tatsächlich", "tatsächlichen", "tatsächlicher", "tatsächliches", "trotzdem", "übel", "über", "überall", "überallhin", "überdies", "übermorgen", "übrig", "übrigens", "ueber", "um", "umso", "unbedingt", "und", "unmöglich", "unmögliche", "unmöglichen", "unmöglicher", "uns", "unser", "unsere", "unserem", "unseren", "unserer", "unseres", "unter", "usw", "viel", "viele", "vielen", "vieler", "vieles", "vielleicht", "vielmals", "völlig", "vom", "von", "vor", "voran", "vorher", "vorüber", "während", "währenddessen", "wann", "war", "wär", "wäre", "waren", "wären", "warst", "warum", "was", "weder", "weil", "weiß", "weiter", "weitere", "weiterem", "weiteren", "weiterer", "weiteres", "weiterhin", "welche", "welchem", "welchen", "welcher", "welches", "wem", "wen", "wenig", "wenige", "weniger", "wenigstens", "wenn", "wenngleich", "wer", "werde", "werden", "werdet", "weshalb", "wessen", "wichtig", "wie", "wieder", "wieso", "wieviel", "wiewohl", "will", "willst", "wir", "wird", "wirklich", "wirst", "wo", "wodurch", "wogegen", "woher", "wohin", "wohingegen", "wohl", "wohlweislich", "womit", "woraufhin", "woraus", "worin", "wurde", "würde", "wurden", "würden", "zB", "z.B.", "zahlreich", "zeitweise", "zu", "zudem", "zuerst", "zufolge", "zugleich", "zuletzt", "zum", "zumal", "zur", "zurück", "zusammen", "zuviel", "zwar", "zwischen");

    private final BiMap<String, Integer> documentIndices;

    private final BiMap<String, Integer> tokenIndices;

    private final TIntList tokenCounts;

    private final List<TIntList> documentFrequencies; //stores for each term the documents in which it occurs
    
    public VocabularyTaz(Layer layer) throws IOException {
        this.layer = layer;
        this.documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        tokenCounts = new TIntArrayList();
        documentFrequencies = new ArrayList<>();
    }

    public void processFile(List<String> sectionIDs, List<List<Token>> articleContent) {
        System.out.println("Processing article vocabulary...");
        extractVocabulary(sectionIDs, articleContent);
    }

    private void extractVocabulary(List<String> sectionIDs, List<List<Token>> articleContent) {
        for (List<Token> articleSection : articleContent) {
            
            String sectionID = sectionIDs.get(articleContent.indexOf(articleSection));
            extractArticleID(sectionID);
            
            for (Token token : articleSection) {
                String value = layer == Layer.LEMMA ?
                        token.getLemma().or("_") :
                        token.getForm().or("_");
                Integer index = tokenIndices.get(value);
                if (index == null) {
                    index = tokenIndices.size();
                    tokenIndices.put(value, index);
                    tokenCounts.add(1);
                } else {
                    tokenCounts.set(index, tokenCounts.get(index) + 1);
                }

                int articleIndex = documentIndices.get(sectionID);

                //Add all document indices to document index list of respective
                //token to obtain token's document frequency
                if (documentFrequencies.size() == index) {
                    documentFrequencies.add(new TIntArrayList());
                }

                TIntList documents = documentFrequencies.get(index);

                // Use binary search for O(log N) search.
                int idx = documents.binarySearch(articleIndex);
                if (idx < 0) { // fileIDIndex not yet in document list of this token
                    idx = ~idx;
                    documents.insert(idx, articleIndex);
                }
            }
        }
    }

    private void extractArticleID(String articleID) {
        if (documentIndices == null) {
            return;
        }

        documentIndices.putIfAbsent(articleID, documentIndices.size());
    }

    @Override
    public BiMap<String, Integer> documentIndices() {
        return documentIndices;
    }

    @Override
    public BiMap<String, Integer> tokenIndices() {
        return tokenIndices;
    }
    
    @Override
    public TIntList tokenCounts() {
        return tokenCounts;
    }
    
    @Override
    public List<TIntList> documentFrequencies() {
        return documentFrequencies;
    }
    
}
