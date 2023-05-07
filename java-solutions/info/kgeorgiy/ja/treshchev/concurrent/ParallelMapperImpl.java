package info.kgeorgiy.ja.treshchev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Class containing realization of {@link ParallelMapper} interface.
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final Queue<Runnable> tasks;
    private final List<Thread> consumers;

    private static final int QUEUE_SIZE = 4096 * 4096;


    /**
     * Constructor of ParallelMapperImpl class.
     * Creates {@code threadsCount} threads to do the tasks.
     *
     * @param threadsCount how many threads should be created.
     */
    public ParallelMapperImpl(final int threadsCount) {
        if (threadsCount <= 0) {
            throw new IllegalArgumentException("Expected at least 1 thread to work, found: " + threadsCount);
        }
        this.tasks = new ArrayDeque<>();
        this.consumers = new ArrayList<>(Collections.nCopies(threadsCount, null));
        for (int i = 0; i < threadsCount; i++) {
            this.consumers.set(i, new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        completeTask();
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        this.consumers.forEach(Thread::start);
    }

    private void completeTask() throws InterruptedException {
        final Runnable task;
        synchronized (this.tasks) {
            while (this.tasks.isEmpty()) {
                this.tasks.wait();
            }
            task = this.tasks.poll();
            this.tasks.notify();
        }
        task.run();
    }

    /**
     * Applies function {@code function} to every element of list.
     *
     * @param function what function should be applied.
     * @param list     to which elements function should be applied.
     * @param <T>      type of list elements.
     * @param <R>      type of result list elements.
     * @return list of elements containing results of function applied to every element.
     * @throws InterruptedException if some errors during working were occurred.
     */
    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> function, final List<? extends T> list) throws InterruptedException {
        final HiddenList<R> results = new HiddenList<>(list.size());
        final List<RuntimeException> exceptions = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            final Runnable task = () -> {
                try {
                    results.setResult(index, function.apply(list.get(index)));
                } catch (RuntimeException e) {
                    results.setResult(index, null);
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
            };

            synchronized (this.tasks) {
                while (this.tasks.size() == QUEUE_SIZE) {
                    this.tasks.wait();
                }
                this.tasks.add(task);
                this.tasks.notify();
            }
        }
        final List<R> resultsList = results.getResults();
        getException(exceptions);
        return resultsList;
    }

    private void getException(final List<RuntimeException> exceptions) {
        RuntimeException thrown = null;
        for (RuntimeException exception : exceptions) {
            if (exception != null) {
                if (thrown == null) {
                    thrown = exception;
                } else {
                    thrown.addSuppressed(exception);
                }
            }
        }
        if (thrown != null) {
            throw thrown;
        }
    }

    /**
     * Method that finishing work of every thread.
     * This method is closing each of {@code threadsCount} thread and cancelling all their tasks.
     */
    @Override
    public void close() {
        for (Thread consumer : this.consumers) {
            consumer.interrupt();
        }
        for (int i = 0; i < this.consumers.size(); i++) {
            final Thread consumer = this.consumers.get(i);
            try {
                while (consumer.isAlive()) {
                    consumer.join();
                }
            } catch (InterruptedException e) {
                i--;
            }
        }
    }
}
