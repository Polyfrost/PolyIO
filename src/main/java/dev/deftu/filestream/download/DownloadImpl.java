package dev.deftu.filestream.download;

import dev.deftu.filestream.api.Downloader;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author xtrm
 */
class DownloadImpl implements Downloader.Download<URL> {

    private final URL source;
    private final Future<Path> future;

    DownloadImpl(URL source, Future<Path> future) {
        this.source = source;
        this.future = future;
    }

    @Override
    public @NotNull URL getSource() {
        return source;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public Path get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public Path get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

}
