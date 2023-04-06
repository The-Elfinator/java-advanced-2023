package info.kgeorgiy.ja.treshchev.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {

    private final List<T> arraySet;
    private final Comparator<T> comparator;
    private final int size;

    public ArraySet() {
        this.comparator = null;
        this.arraySet = new ArrayList<>();
        this.size = 0;
    }

    public ArraySet(Collection<T> collection) {
        this(collection, null);
    }

    private ArraySet(List<T> arrayList, Comparator<T> comparator) {
        this.arraySet = arrayList;
        this.comparator = comparator;
        this.size = arrayList.size();
    }

    public ArraySet(Collection<T> collection, Comparator<T> comparator) {
        this.comparator = comparator;
        TreeSet<T> sortedCollection = new TreeSet<>(comparator);
        sortedCollection.addAll(collection);
        this.arraySet = new ArrayList<>(sortedCollection);
        this.size = this.arraySet.size();
    }

    @Override
    public Iterator<T> iterator() {
        return this.arraySet.iterator();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Comparator<? super T> comparator() {
        return this.comparator;
    }

    private int getIndex(int index) {
        return index < 0 ? -index - 1 : index;
    }

    private int indexOf(T element) {
        return Collections.binarySearch(this.arraySet, element, this.comparator);
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Object element) {
        return indexOf((T) element) >= 0;
    }


    private SortedSet<T> subSet(int indexFrom, int indexTo) {
        return new ArraySet<>(this.arraySet.subList(indexFrom, indexTo), this.comparator);
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) throws IllegalArgumentException {
        if (this.comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return subSet(getIndex(indexOf(fromElement)), getIndex(indexOf(toElement)));
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        if (this.size == 0) {
            return new TreeSet<>(this.comparator);
        }
        return subSet(0, getIndex(indexOf(toElement)));
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        if (this.size == 0) {
            return new TreeSet<>(this.comparator);
        }
        return subSet(getIndex(indexOf(fromElement)), this.size);
    }

    @Override
    public T first() throws NoSuchElementException {
        if (this.size == 0) {
            throw new NoSuchElementException();
        }
        return this.arraySet.get(0);
    }

    @Override
    public T last() throws NoSuchElementException {
        if (this.size == 0) {
            throw new NoSuchElementException();
        }
        return this.arraySet.get(arraySet.size() - 1);
    }


}
