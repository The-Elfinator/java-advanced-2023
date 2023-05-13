package info.kgeorgiy.ja.treshchev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * public class for recursive walking of sites starting with url and to the specified depth
 *
 * @author Artem Treschev (<a href="https://github.com/The-Elfinator">github.com/The-Elfinator</a>)
 */
public class WebCrawler implements Crawler {

    private Downloader downloader;
    private ExecutorService downloadersService;
    private ExecutorService extractorsService;
    private int perHost;

    private static final int QUEUE_SIZE = 4096 * 4096;
    private static final double TIME_SCALE = 1;
    private static final String DEPTH_NAME = "depth";
    private static final String DOWNLOADERS_NAME = "downloaders";
    private static final String EXTRACTORS_NAME = "extractors";
    private static final String PER_HOST_NAME = "perHost";
    private static final Map<String, Integer> DEFAULT_VALUES = Map.of(
            DEPTH_NAME, 1,
            DOWNLOADERS_NAME, 1,
            EXTRACTORS_NAME, 1,
            PER_HOST_NAME, 1
    );

    /**
     * Creates WebCrawler instance with default downloader.
     * Creates the instance of {@link WebCrawler} class using
     * {@link CachingDownloader} as default downloader
     *
     * @param downloaders max count of pages that could be downloaded simultaneously.
     * @param extractors  max count of pages from which links could be extracted simultaneously.
     * @param perHost     hom many pages could be downloaded from one host.
     * @throws IOException if creating the instance of {@link CachingDownloader} throws IOException.
     */
    public WebCrawler(final int downloaders, final int extractors, final int perHost) throws IOException {
        new WebCrawler(new CachingDownloader(TIME_SCALE), downloaders, extractors, perHost);
    }

    /**
     * Creates WebCrawler instance with default downloader.
     * Creates the instance of {@link WebCrawler} class using specified downloader of pages.
     *
     * @param downloader  what downloader of pages should be used
     * @param downloaders max count of pages that could be downloaded simultaneously.
     * @param extractors  max count of pages from which links could be extracted simultaneously.
     * @param perHost     how many pages could be downloaded from one host.
     */
    public WebCrawler(final Downloader downloader,
                      final int downloaders,
                      final int extractors,
                      final int perHost) {
        if (downloader == null) {
            throw new IllegalArgumentException("Required not null downloader!");
        }
        checkNonLessOne(downloaders, DOWNLOADERS_NAME);
        checkNonLessOne(extractors, EXTRACTORS_NAME);
        checkNonLessOne(perHost, PER_HOST_NAME);
        this.downloader = downloader;
        this.downloadersService = Executors.newFixedThreadPool(downloaders);
        this.extractorsService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    private void checkNonLessOne(final int value, final String name) {
        if (value < 1) {
            throw new IllegalArgumentException(
                    String.format("Expected at least one %s, found: %d", name, value)
            );
        }
    }

    /**
     * Method that allows you to run a crawl from the command line.
     * Uses {@link CachingDownloader} as default downloader for downloading pages and extracting links.
     *
     * @param args {@code url [depth [downloaders [extractors [perHost]]]]} - arguments of command line,
     *             as some arguments are optional there would be default values for each of them.
     */
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Error! Not null arguments expected!");
            return;
        }
        if (args.length < 1 || args.length > 5) {
            System.err.println("Expected from 1 to 5 arguments of command line:\n" +
                    "'url [depth [downloaders [extractors [perHost]]]]', but found " + args.length + " arguments");
            return;
        }
        String url = args[0];
        final int depth;
        final int downloaders;
        final int extractors;
        final int perHost;
        try {
            depth = getArgument(1, args, DEPTH_NAME);
            downloaders = getArgument(2, args, DOWNLOADERS_NAME);
            extractors = getArgument(3, args, EXTRACTORS_NAME);
            perHost = getArgument(4, args, PER_HOST_NAME);
        } catch (NumberFormatException e) {
            System.err.println("Expected only integers as arguments!" + e.getMessage());
            return;
        }

        try (WebCrawler crawler = new WebCrawler(downloaders, extractors, perHost)) {
            final Result result = crawler.download(url, depth);
            final List<String> success = result.getDownloaded();
            final Map<String, IOException> errors = result.getErrors();
            System.out.println("These pages has been downloaded successfully:");
            for (String s : success) {
                System.out.println(s);
            }
            System.out.println("These pages has been downloaded with error:");
            for (String error : errors.keySet()) {
                System.out.printf("URL=\"%s\", error=\"%s\"%n", error, errors.get(error).getMessage());
            }
        } catch (IOException e) {
            System.err.println("CachingDownloader directory wasn't found!" + e.getMessage());
        }
    }

    private static int getArgument(final int index, final String[] args, final String name)
            throws NumberFormatException {
        try {
            return Integer.parseInt(args[index]);
        } catch (IndexOutOfBoundsException e) {
            return DEFAULT_VALUES.get(name);
        }
    }

    /**
     * Method that allows you to recursive walk pages
     * starting with specified url to the specified depth
     * and returning the list of downloaded pages containing in {@link Result#getDownloaded()}.
     * If some pages has been downloaded with error
     * than {@link Result#getErrors()} will return {@link Map}
     * with key {@link String} contains the url that has been downloaded with error
     * and value {@link IOException} contains the specific error that has been occurred.
     *
     * @param url   from where to start crawling
     * @param depth to what depth should crawl has been done
     * @return {@link Result} instance contains {@link List} of successfully downloaded pages
     * and {@link Map} of pages has been downloaded with errors.
     */
    @Override
    public Result download(final String url, final int depth) {
        final Set<String> success = ConcurrentHashMap.newKeySet();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        final Set<String> used = ConcurrentHashMap.newKeySet();
        bfsDownload(url, depth, success, errors, used);
        return new Result(success.stream().toList(), errors);
    }

    private void bfsDownload(final String url,
                             final int depth,
                             final Set<String> success,
                             final Map<String, IOException> errors,
                             final Set<String> used) {
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(QUEUE_SIZE, true);
        queue.add(url);
        used.add(url);
        int currentDepth = depth;
        // :NOTE: new Phaser need here
        final Phaser phaser = new Phaser();
        phaser.register();
        while (!queue.isEmpty()) {
            final BlockingQueue<String> levelQueue = new ArrayBlockingQueue<>(queue.size(), true, queue);
            queue.clear();
            final int finalCurrentDepth = currentDepth;
            while (!levelQueue.isEmpty()) {
                final String currentUrl = levelQueue.poll();
                phaser.register();
                final Runnable downloaderTask = getDownloaderTask(success, errors, used, queue,
                        finalCurrentDepth, phaser, currentUrl);
                this.downloadersService.submit(downloaderTask);
            }
            phaser.arriveAndAwaitAdvance();
            currentDepth--;
        }
    }

    private Runnable getDownloaderTask(final Set<String> success,
                                       final Map<String, IOException> errors,
                                       final Set<String> used,
                                       final BlockingQueue<String> queue,
                                       final int finalCurrentDepth,
                                       final Phaser phaser,
                                       final String currentUrl) {
        return () -> {
            if (currentUrl != null) {
                try {
                    final Document document = this.downloader.download(currentUrl);
                    success.add(currentUrl);
                    if (finalCurrentDepth > 1) {
                        phaser.register();
                        final Runnable extractorTask = getExtractorTask(errors, used, queue,
                                phaser, currentUrl, document);
                        this.extractorsService.submit(extractorTask);
                    }
                } catch (IOException e) {
                    errors.put(currentUrl, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            }
        };
    }

    private Runnable getExtractorTask(final Map<String, IOException> errors,
                                      final Set<String> used,
                                      final BlockingQueue<String> queue,
                                      final Phaser phaser,
                                      final String currentUrl,
                                      final Document document) {
        return () -> {
            try {
                final List<String> links = document.extractLinks();
                for (String link : links) {
                    if (used.add(link)) {
                        queue.add(link);
                    }
                }
            } catch (IOException e) {
                errors.put(currentUrl, e);
            } finally {
                phaser.arriveAndDeregister();
            }
        };
    }

    /**
     * Shuts down all helping threads that has been started
     * with creating instance of {@link WebCrawler} class.
     */
    @Override
    public void close() {
        while (!this.downloadersService.isTerminated()) {
            this.downloadersService.shutdownNow();
        }
        while (!this.extractorsService.isTerminated()) {
            this.extractorsService.shutdownNow();
        }
    }
}
