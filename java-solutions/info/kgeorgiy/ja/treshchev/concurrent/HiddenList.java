package info.kgeorgiy.ja.treshchev.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class HiddenList<R> {

    private final List<R> results;
    private int realSize;

    public HiddenList(int size) {
        this.results = new ArrayList<>(Collections.nCopies(size, null));
        this.realSize = 0;
    }

    public synchronized void setResult(int pos, R obj) {
        this.results.set(pos, obj);
        this.realSize++;
        if (this.realSize == this.results.size()) {
            this.notifyAll();
        }
    }

    public synchronized List<R> getResults() throws InterruptedException {
        while (this.realSize < this.results.size()) {
            this.wait();
        }
        return this.results;
    }
}
