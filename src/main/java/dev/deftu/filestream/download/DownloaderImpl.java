package dev.deftu.filestream.download;

import dev.deftu.filestream.api.Downloader;
import dev.deftu.filestream.api.Rewriter;
import dev.deftu.filestream.api.Store;
import dev.deftu.filestream.util.HashingHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class DownloaderImpl implements Downloader {

    private static final boolean TRACE_BYTES = Boolean.getBoolean("filestream.debug.traceDownloadedBytes");
    private static final Logger logger = LogManager.getLogger();

    private final Store downloadStore;

    public DownloaderImpl(Store downloadStore) {
        this.downloadStore = downloadStore;
    }

    @Override
    public Download<URL> download(@NotNull URL url, @Nullable Path target, @Nullable HashProvider hashProvider, @Nullable DownloadCallback callback) {
        return new DownloadImpl(url, CompletableFuture.supplyAsync(() -> {
            logger.trace("Starting download of {}", url);

            Path downloadStoreObject = downloadStore.getObject(url.toString());
            logger.trace("Download store object is {}", downloadStoreObject);

            boolean needsLinking = target != null;

            if (!isValid(downloadStoreObject, hashProvider)) {
                logger.trace("Invalid local object, downloading {} to {}", url, downloadStoreObject);
                boolean success = downloadFile(url, downloadStoreObject, callback != null ? callback : DownloadCallback.NOOP);
                logger.trace("Download of {} to {} was {}", url, downloadStoreObject, success ? "successful" : "unsuccessful");
            }

            if (!needsLinking) {
                logger.trace("No linking required, returning {}", downloadStoreObject);
                return downloadStoreObject;
            }

            return Rewriter.DEFAULT.rewrite(downloadStoreObject, target);
        }));
    }

    @Override
    public Download<URL> download(@NotNull URL url, @NotNull Store store, @Nullable HashProvider hashProvider, @Nullable DownloadCallback callback) {
        String targetName = HashingHelper.hash(url.toString(), HashingHelper.SHA256);
        Path target = store.getStoreRoot().resolve(targetName);
        return download(url, target, hashProvider, callback);
    }

    private boolean downloadFile(URL url, Path storeObject, @NotNull DownloadCallback callback) {
        logger.trace("Opening connection to {}", url);
        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = (HttpURLConnection) Networking.createConnection(url);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while opening connection to " + url,
                    e
            );
        }

        long total = httpURLConnection.getContentLengthLong();
        logger.trace("Connection opened, total size is {}", total);

        logger.trace("Creating download store object");
        try {
            Files.createDirectories(storeObject.getParent());
            if (Files.exists(storeObject)) {
                logger.trace("Download store object already exists, deleting...");
                Files.delete(storeObject);
            }
            Files.createFile(storeObject);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while creating download store object " + storeObject,
                    e
            );
        }

        logger.trace("Downloading {} to {}", url, storeObject);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = httpURLConnection.getInputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            long totalRead = 0;
            while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                if (TRACE_BYTES) {
                    logger.trace("Read {}/{} total bytes", read, totalRead);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < read; i++) {
                        stringBuilder.append(String.format("%02X", buffer[i]));
                    }
                    logger.trace("buffer={}", stringBuilder.toString());
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
                logger.trace("Writing {} bytes to {}", bytes.length, storeObject);
                StringBuilder stringBuilder = new StringBuilder();
                for (byte b : bytes) {
                    stringBuilder.append(String.format("%02X", b));
                }

                logger.trace("buffer={}", stringBuilder.toString());
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
        logger.trace("Checking if {} is valid", target);
        if (!Files.exists(target)) {
            logger.trace("{} does not exist, invalid", target);
            return false;
        }

        if (hashProvider == null) {
            logger.trace("No hash provider, assuming invalid");
            return false;
        }

        String hash = hashProvider.getHash();
        Supplier<MessageDigest> hashingFunction = hashProvider.getHashingFunction();
        if (hash == null || hashingFunction == null) {
            logger.trace("No hash or hashing function, assuming invalid");
            return false;
        }

        logger.trace("Hash provider for {} returned {}", target, hash);

        String computedHash;
        try {
            MessageDigest messageDigest = hashingFunction.get();
            computedHash = HashingHelper.hash(target, messageDigest);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while computing hash of " + target,
                    e
            );
        }

        logger.trace("Computed hash of {} is {}", target, computedHash);
        boolean valid = hash.equals(computedHash);
        logger.trace("Hash is {}valid", valid ? "" : "in");
        return valid;
    }

}
