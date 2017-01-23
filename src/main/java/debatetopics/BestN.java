package debatetopics;

import com.google.common.collect.Iterators;
import java.util.*;

/**
 * @author Daniël de Kok &lt;me@danieldk.eu&gt;
 */
public class BestN<T extends Comparable<T>> implements Collection<T> {
    private final int n;
    private final List<T> best;
    private final Comparator<T> comparator;

    public BestN(int n) {
        this(n, null);
    }

    public BestN(int n, Comparator<T> comparator) {
        this.n = n;
        this.comparator = comparator;
        this.best = new ArrayList<>();
    }

    @Override
    public boolean addAll(Collection<? extends T> coll) {
        boolean modified = false;

        for (T t : coll) {
            modified = modified ? modified : add(t);
        }

        return modified;
    }

    @Override
    public boolean add(T elem) {
        int insPoint = comparator == null ? Collections.binarySearch(best, elem) :
                Collections.binarySearch(best, elem, comparator);
        if (insPoint < 0) {
            insPoint = -(insPoint + 1);
        }

        if (best.size() < n) {
            best.add(insPoint, elem);
        } else {
            if (insPoint < n) {
                Collections.rotate(best.subList(insPoint, best.size()), 1);
                best.set(insPoint, elem);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        return best.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return best.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return best.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator(best.iterator());
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return best.size();
    }

    @Override
    public Object[] toArray() {
        return best.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return best.toArray(a);
    }


}

