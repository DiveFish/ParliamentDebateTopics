package compact;

/**
 * @author Daniël de Kok and Patricia Fischer
 */
public class DocumentDistance implements Comparable<DocumentDistance> {
    private final String document;
    private final double distance;

    public DocumentDistance(String document, double distance) {
        this.document = document;
        this.distance = distance;
    }

    public String document() {
        return document;
    }

    public double distance() {
        return distance;
    }

    @Override
    public String toString() {
        return "DocumentDistance{" +
                "document='" + document + '\'' +
                ", distance=" + distance +
                '}';
    }

    @Override
    public int compareTo(DocumentDistance o) {
        int cmp = Double.compare(distance, o.distance);
        if (cmp == 0) {
            return document.compareTo(o.document);
        }

        return cmp;
    }
}
