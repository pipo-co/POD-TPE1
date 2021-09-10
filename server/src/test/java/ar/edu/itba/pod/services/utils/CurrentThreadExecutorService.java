package ar.edu.itba.pod.services.utils;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Executor service that runs all tasks on current thread. Intended for testing purposes. */
public class CurrentThreadExecutorService extends AbstractExecutorService {

    // atomic because can be viewed by other threads
    private final AtomicBoolean terminated;

    public CurrentThreadExecutorService() {
        this.terminated = new AtomicBoolean(false);
    }

    @Override
    public void shutdown() {
        terminated.set(false);
    }

    @Override
    public boolean isShutdown() {
        return terminated.get();
    }

    @Override
    public boolean isTerminated() {
        return isShutdown();
    }

    @Override
    public boolean awaitTermination(final long theTimeout, final TimeUnit theUnit) throws InterruptedException {
        if(isShutdown()) {
            return true;
        } else {
            theUnit.sleep(theTimeout);
            return false;
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return List.of();
    }

    @Override
    public void execute(final Runnable theCommand) {
        theCommand.run();
    }
}
