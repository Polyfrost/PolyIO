package cc.polyfrost.polyio.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Schedules and manages downloads.
 *
 * @author xtrm
 */
public interface Downloader {
    /**
     * <p>Downloads a file from the given URL to the given target path.</p>
     * <p>Will try to rely on a cache mechanism if the file already exists and
     * {@code hashProvider} is provided.</p>
     *
     * @param url          the {@link URL} to download from
     * @param target       the target file {@link Path}
     * @param hashProvider a {@link HashProvider}, nullable
     * @param callback     a {@link DownloadCallback} to be called on any change in
     * @return a {@link Download} of the {@link Path} to the downloaded file
     */
    Download<URL> download(@NotNull URL url, @Nullable Path target, @Nullable HashProvider hashProvider, @Nullable DownloadCallback callback);

    default Download<URL> download(URL url, Path target, @Nullable HashProvider hashProvider) {
        return download(url, target, hashProvider, DownloadCallback.NOOP);
    }

    default Download<URL> download(URL url, Path target, DownloadCallback callback) {
        return download(url, target, null, callback);
    }

    default Download<URL> download(URL url, Path target) {
        return download(url, target, null, DownloadCallback.NOOP);
    }

    Download<URL> download(@NotNull URL url, @NotNull Store store, @Nullable HashProvider hashProvider, @Nullable DownloadCallback callback);

    default Download<URL> download(URL url, Store store, @Nullable HashProvider hashProvider) {
        return download(url, store, hashProvider, DownloadCallback.NOOP);
    }

    default Download<URL> download(URL url, Store store, DownloadCallback callback) {
        return download(url, store, null, callback);
    }

    default Download<URL> download(URL url, Store store) {
        return download(url, store, null, DownloadCallback.NOOP);
    }

    default Download<URL> download(URL url, HashProvider hashProvider, DownloadCallback callback) {
        return download(url, (Path) null, hashProvider, callback);
    }

    default Download<URL> download(URL url, DownloadCallback callback) {
        return download(url, (Path) null, null, callback);
    }

    default Download<URL> download(URL url) {
        return download(url, (Path) null, null, DownloadCallback.NOOP);
    }

    interface Download<S> extends Future<Path> {
        @NotNull S getSource();
    }

    interface HashProvider {
        @Nullable String getHash();

        @Nullable Supplier<@NotNull MessageDigest> getHashingFunction();

        default boolean isHashPresent() {
            return getHash() == null || getHash().isEmpty();
        }

        static HashProvider of(String hash, String hashingFunction) {
            return new HashProvider() {
                private MessageDigest messageDigest;

                @Override
                public String getHash() {
                    return hash;
                }

                @Override
                public Supplier<@NotNull MessageDigest> getHashingFunction() {
                    return () -> {
                        if (messageDigest == null) {
                            try {
                                messageDigest = MessageDigest.getInstance(hashingFunction);
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return messageDigest;
                    };
                }
            };
        }
    }

    @FunctionalInterface
    interface DownloadCallback {
        DownloadCallback NOOP = (downloaded, total) -> {
        };

        /**
         * <p>Callback function to be called on any change in the download
         * process.</p>
         *
         * <p><b>Implementation Notice</b>: Please not that this function, in
         * event of a cached file, might not be called.</p>
         *
         * @param downloaded the amount of bytes downloaded
         * @param total      the total amount of bytes to download
         */
        void updateProgress(long downloaded, long total);
    }
}
