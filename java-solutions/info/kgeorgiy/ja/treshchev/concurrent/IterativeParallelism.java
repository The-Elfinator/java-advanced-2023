package info.kgeorgiy.ja.treshchev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Class for realization of parallel evaluation.
 * This class provides realization of methods in {@link ScalarIP} class
 *
 * @author artem
 */
public class IterativeParallelism implements ScalarIP {

    private final ParallelMapper mapper;

    private <T> void checkCountAndList(int count, List<? extends T> list) {
        if (count <= 0) {
            throw new IllegalArgumentException("Expected at least 1 thread to do parallelism, found " + count);
        }
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Error! Expected non-empty list as argument!");
        }
    }

    private <T> void checkArguments(int count, List<? extends T> list, Object object, String arg) {
        checkCountAndList(count, list);
        if (object == null) {
            throw new IllegalArgumentException("Error! Expected not null " + arg + "!");
        }
    }

    private <V> List<Stream<? extends V>> getSubLists(int threadsCount, List<? extends V> list) {
        final List<Stream<? extends V>> subLists = new ArrayList<>();
        final int blockSize = list.size() / threadsCount;
        final int mod = list.size() % threadsCount;
        int pos = 0;
        for (int i = 0; i < threadsCount; i++) {
            final int currentBlockSize = blockSize + (i < mod ? 1 : 0);
            if (currentBlockSize > 0) {
                subLists.add(list.subList(pos, pos + currentBlockSize).stream());
            }
            pos += currentBlockSize;
        }
        return subLists;
    }

    private List<InterruptedException> getInterruptedExceptions(List<Thread> workers) {
        final List<InterruptedException> exceptions = new ArrayList<>();
        workers.forEach(thread -> {
            try {
                while (thread.isAlive()) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                exceptions.add(e);
            }
        });
        return exceptions;
    }

    private <V, R> List<R> getResults(Function<Stream<? extends V>, R> function, List<Stream<? extends V>> subLists, List<Thread> workers) throws InterruptedException {
        final List<R> results;
        if (this.mapper != null) {
            try {
                results = this.mapper.map(function, subLists);
            } catch (InterruptedException e) {
                throw new InterruptedException("Some error occurred!");
            }
        } else {
            results = new ArrayList<>(Collections.nCopies(subLists.size(), null));
            for (int i = 0; i < subLists.size(); i++) {
                final int ind = i;
                final Thread thread = new Thread(
                    () -> results.set(ind, function.apply(subLists.get(ind)))
                );
                workers.add(thread);
                thread.start();
            }
        }
        return results;
    }

    private <V, R, T> T run(int threadsCount, List<? extends V> list, Function<Stream<? extends V>, R> function, Function<List<R>, T> update) throws InterruptedException {
        final List<Stream<? extends V>> subLists = getSubLists(threadsCount, list);
        final List<Thread> workers = new ArrayList<>();
        final List<R> results = getResults(function, subLists, workers);

        final List<InterruptedException> exceptions = getInterruptedExceptions(workers);
        if (!exceptions.isEmpty()) {
            throw new InterruptedException("Some threads were interrupted with an error!");
        }
        return update.apply(results);
    }

    /**
     * This constructor provides default realization of methods of this class.
     *
     * If object was created using this constructor,
     * then every method will create his own threads to do the task.
     */
    public IterativeParallelism() {
        this.mapper = null;
    }

    /**
     * This constructor provides realization of methods of this class
     * using {@link ParallelMapper} interface.
     *
     * If object was created using this constructor,
     * then every method will complete using {@link ParallelMapperImpl} class to
     * create threads and do the task.
     * @param mapper Object instance of {@link ParallelMapper} interface.
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * This method returns maximum of list using multiple threads.
     * This method evaluates max of list using no less than 1 thread to iterative parallel algorithm.
     *
     * @param threadsCount contains count of threads, should be no less than 1
     * @param list where to find maximum, should be not {@code null} and non-empty
     * @param comparator how to compare objects in list, should be not {@code null}
     * @param <T> type of values
     * @return maximum from list using comparator
     * @throws InterruptedException if threads were interrupted with error
     */
    @Override
    public <T> T maximum(int threadsCount, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        checkArguments(threadsCount, list, comparator, "comparator");
        return run(threadsCount, list,
                (Stream<? extends T> stream) -> stream.max(comparator).orElse(null),
                (List<T> results) -> (results.stream().max(comparator).orElse(null))
        );
    }

    /**
     * This method returns minimum of list using multiple threads.
     * This method evaluates min of list using no less than 1 thread to iterative parallel algorithm.
     * @param threadsCount contains count of threads, should be no less than 1
     * @param list where to find minimum, should be not {@code null} and non-empty
     * @param comparator how to compare objects in list, should be not {@code null}
     * @param <T> type of values
     * @return minimum from list using comparator
     * @throws InterruptedException if threads were interrupted with error
     */
    @Override
    public <T> T minimum(int threadsCount, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        checkArguments(threadsCount, list, comparator, "comparator");
        return run(threadsCount, list,
                (Stream<? extends T> stream) -> stream.min(comparator).orElse(null),
                (List<T> results) -> (results.stream().min(comparator).orElse(null))
        );
    }

    /**
     * Checks if all elements from list match with predicate.
     * This method checks if all elements match with predicate
     * using no less than 1 thread to iterative parallel algorithm.
     * @param threadsCount contains count of threads, should be no less than 1
     * @param list where to check elements, should be not {@code null} and non-empty
     * @param predicate what condition should elements match with, should be not {@code null}
     * @param <T> type of values
     * @return {@code true} if all elements from list match with predicate,
     * {@code false} if found element that doesn't match with predicate.
     * @throws InterruptedException if threads were interrupted with error
     */
    @Override
    public <T> boolean all(int threadsCount, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        checkArguments(threadsCount, list, predicate, "predicate");
        return run(threadsCount, list,
                (Stream<? extends T> stream) -> stream.allMatch(predicate),
                (List<Boolean> results) -> results.stream().allMatch(aBoolean -> aBoolean));
    }

    /**
     * Checks if any element from list match with predicate.
     * This method checks if any element match with predicate
     * using no less than 1 thread to iterative parallel algorithm.
     * @param threadsCount contains count of threads, should be no less than 1
     * @param list where to check elements, should be not {@code null} and non-empty
     * @param predicate what condition should element match with, should be not {@code null}
     * @param <T> type of values
     * @return {@code true} if found element from list that matches with predicate,
     * {@code false} if all elements don't match with predicate.
     * @throws InterruptedException if threads were interrupted with error
     */
    @Override
    public <T> boolean any(int threadsCount, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        checkArguments(threadsCount, list, predicate, "predicate");
        return run(threadsCount, list,
                (Stream<? extends T> stream) -> stream.anyMatch(predicate),
                (List<Boolean> results) -> results.stream().anyMatch(aBoolean -> aBoolean));
    }

    /**
     * Returns count of elements that match with predicate.
     * This method returns count of elements from list that match with predicate
     * using no less than 1 thread to iterative parallel algorithm.
     * @param threadsCount contains count of threads, should be no less than 1
     * @param list where to count elements, should be not {@code null} and non-empty
     * @param predicate what condition should elements match with, should be not {@code null}
     * @param <T> type of values
     * @return an {@link Integer} value that shows how many elements from the list math with predicate
     * @throws InterruptedException if threads were interrupted with error
     */
    @Override
    public <T> int count(int threadsCount, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        checkArguments(threadsCount, list, predicate, "predicate");
        return run(threadsCount, list,
                (Stream<? extends T> stream) -> stream.filter(predicate).count(),
                (List<Long> results) -> results.stream().reduce(Long::sum).orElse(0L).intValue());
    }
}
