package cc.polyfrost.polyio.download;

import cc.polyfrost.polyio.api.Downloader;
import cc.polyfrost.polyio.api.Rewriter;
import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.util.PolyHashing;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A lightweight and simple download management implementation.
 *
 * @author xtrm
 */
@Log4j2
@RequiredArgsConstructor
public class PolyDownloader implements Downloader {
    private static final boolean TRACE_BYTES =
            Boolean.getBoolean("polyio.debug.traceDownloadedBytes");
    private final Store downloadStore;

    @Override
    public Download<URL> download(@NotNull URL url, @Nullable Path target, @Nullable HashProvider hashProvider, @Nullable DownloadCallback callback) {
        return new PolyDownload(url, CompletableFuture.supplyAsync(() -> {
            log.info("Starting download of {}", url);

            Path downloadStoreObject = downloadStore.getObject(url.toString());
            log.trace("Download store object is {}", downloadStoreObject);

            boolean needsLinking = target != null;

            if (!isValid(downloadStoreObject, hashProvider)) {
                log.trace("Invalid local object, downloading {} to {}", url, downloadStoreObject);
                boolean success = downloadFile(url, downloadStoreObject, callback != null ? callback : DownloadCallback.NOOP);
                log.trace("Finished downloading.");
            }

            if (!needsLinking) {
                log.trace("No linking required, returning {}", downloadStoreObject);
                return downloadStoreObject;
            }

            return Rewriter.DEFAULT.rewrite(downloadStoreObject, target);
        }));
    }

    @Override
    public Download<URL> download(@NotNull URL url, @NotNull Store store, @Nullable HashProvider hashProvider, @Nullable DownloadCallback callback) {
        String targetName = PolyHashing.hash(url.toString(), PolyHashing.SHA256);
        Path target = store.getStoreRoot().resolve(targetName);
        return download(url, target, hashProvider, callback);
    }

    private boolean downloadFile(URL url, Path storeObject, @NotNull DownloadCallback callback) {
        log.trace("Opening connection to {}", url);
        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = (HttpURLConnection) PolyNetwork.createConnection(url);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while opening connection to " + url,
                    e
            );
        }

        long total = httpURLConnection.getContentLengthLong();
        log.trace("Connection opened, total size is {}", total);

        log.trace("Creating download store object");
        try {
            Files.createDirectories(storeObject.getParent());
            if (Files.exists(storeObject)) {
                log.trace("Download store object already exists, deleting...");
                Files.delete(storeObject);
            }
            Files.createFile(storeObject);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while creating download store object " + storeObject,
                    e
            );
        }

        log.trace("Downloading {} to {}", url, storeObject);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = httpURLConnection.getInputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            long totalRead = 0;
            while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                if (TRACE_BYTES) {
                    log.trace("Read {}/{} total bytes", read, totalRead);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < read; i++) {
                        stringBuilder.append(String.format("%02X", buffer[i]));
                    }
                    log.trace("buffer={}", stringBuilder.toString());
                }
                byteArrayOutputStream.write(buffer, 0, read);
                totalRead += read;
                callback.updateProgress(totalRead, total);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while downloading " + url + " to " + storeObject,
                    e
            );
        } finally {
            httpURLConnection.disconnect();
        }

        try {
            byte[] bytes = byteArrayOutputStream.toByteArray();
            if (TRACE_BYTES) {
                log.trace("Writing {} bytes to {}", bytes.length, storeObject);
                StringBuilder stringBuilder = new StringBuilder();
                for (byte b : bytes) {
                    stringBuilder.append(String.format("%02X", b));
                }
                log.trace("buffer={}", stringBuilder.toString());
            }
            Files.write(storeObject, bytes);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while writing to " + storeObject,
                    e
            );
        }

        return true;
    }

    private boolean isValid(@NotNull Path target, @Nullable HashProvider hashProvider) {
        log.trace("Checking if {} is valid", target);
        if (!Files.exists(target)) {
            log.trace("{} does not exist, invalid", target);
            return false;
        }
        if (hashProvider == null) {
            log.trace("No hash provider, assuming invalid");
            return false;
        }
        String hash = hashProvider.getHash();
        Supplier<MessageDigest> hashingFunction = hashProvider.getHashingFunction();
        if (hash == null || hashingFunction == null) {
            log.trace("No hash or hashing function, assuming invalid");
            return false;
        }
        String computedHash;
        try {
            MessageDigest messageDigest = hashProvider.getHashingFunction().get();
            computedHash = PolyHashing.hash(target, messageDigest);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while computing hash of " + target,
                    e
            );
        }
        log.trace("Computed hash of {} is {}", target, computedHash);
        boolean valid = hash.equals(computedHash);
        log.trace("Hash is {}valid", valid ? "" : "in");
        return valid;
    }
}
