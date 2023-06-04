package info.kgeorgiy.ja.treshchev.hello;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class to shut down fixed thread pool.
 * This final class contains only 1 method {@link ClosingTool#close(ExecutorService)}
 * that closes fixed thread pool.
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub account</a>)
 */
public final class ClosingTool {

    /**
     * Method allows you shutting down thread pool.
     * @param service what thread pool to shut down
     */
    public static void close(final ExecutorService service) {
        if (!service.isTerminated()) {
            service.shutdown();
            boolean wasShutDownNow = false;
            boolean flag = false;
            while (!flag) {
                try {
                    flag = service.awaitTermination(1L, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    if (!wasShutDownNow) {
                        service.shutdownNow();
                        wasShutDownNow = true;
                    }
                }
            }
            if (wasShutDownNow) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
