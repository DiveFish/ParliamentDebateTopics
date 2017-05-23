package compact;

import com.carrotsearch.hppc.BitSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A container of hashed documents, which can be used to
 * find documents that are similar to a particular document.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class HashedDocuments implements Serializable {
    private static final long serialVersionUID = 2L;

    private BiMap<String, Integer> documentIndices;
    private List<BitSet> documentHashes;
    private int hashLength;

    public HashedDocuments(BiMap<String, Integer> documentIndices, List<BitSet> documentHashes, int hashLength) {
        Preconditions.checkNotNull(documentIndices);
        Preconditions.checkNotNull(documentHashes);

        this.documentIndices = documentIndices;
        this.documentHashes = documentHashes;
        this.hashLength = hashLength;
    }

    public List<BitSet> getDocumentHashes() {
        return documentHashes;
    }

    public int getHashLength() {
        return hashLength;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        documentIndices = (BiMap<String, Integer>) in.readObject();
        List<long[]> rawBitsets = (List<long[]>) in.readObject();
        documentHashes = new ArrayList<>(rawBitsets.size());

        for (long[] rawBitset : rawBitsets) {
            documentHashes.add(new BitSet(rawBitset, rawBitset.length));
        }

        hashLength = in.readInt();
    }

    public Optional<List<DocumentDistance>> similar(String docId, int n) {
        Preconditions.checkNotNull(docId);

        Integer tokenIdx = documentIndices.get(docId);
        if (tokenIdx == null) {
            return Optional.empty();
        }

        BestN<DocumentDistance> bestN = new BestN<>(n);

        BitSet tokenHash = documentHashes.get(tokenIdx);
        for (int i = 0; i < documentHashes.size(); i++) {
            if (i == tokenIdx) {
                continue;
            }

            double distance = (double) BitSet.xorCount(tokenHash, documentHashes.get(i)) / hashLength;

            //DocumentSimilarity
            //number of documents are the columns here, would be number of words for PDT
            //some words will only have few documents in which they occur
            DocumentDistance documentDistance = new DocumentDistance(documentIndices.inverse().get(i), distance);
            bestN.add(documentDistance);
        }

        return Optional.of(new ArrayList(bestN));
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        List<long[]> rawBitsets = new ArrayList<>(documentHashes.size());
        for (BitSet tokenHash : documentHashes) {
            rawBitsets.add(tokenHash.bits);
        }

        out.writeObject(documentIndices);
        out.writeObject(rawBitsets);
        out.writeInt(hashLength);
    }
}