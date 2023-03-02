package cc.polyfrost.polyio.download;

import cc.polyfrost.polyio.api.Downloader;
import cc.polyfrost.polyio.api.Rewriter;
import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.util.PolyHashing;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
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
        // target = storeRoot/hash(url)
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
        try (InputStream inputStream = httpURLConnection.getInputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
             FileOutputStream fileOutputStream = new FileOutputStream(storeObject.toFile())) {
            byte[] buffer = new byte[1024];
            int read;
            long totalRead = 0;
            while ((read = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, read);
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
