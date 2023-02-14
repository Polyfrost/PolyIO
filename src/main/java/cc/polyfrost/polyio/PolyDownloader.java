package cc.polyfrost.polyio;

import cc.polyfrost.polyio.api.Downloader;
import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.util.PolyHasher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Schedules and manages downloads.
 *
 * @author xtrm
 */
public class PolyDownloader implements Downloader {
    private final Store downloadStore;

    public PolyDownloader(Store downloadStore) {
        this.downloadStore = downloadStore;
    }

    @Override
    public Future<Path> download(@NotNull URL url, @NotNull Path target, @Nullable HashProvider hashProvider, @Nullable DownloadCallback callback) {
        return CompletableFuture.supplyAsync(() -> {
            Path downloadStoreObject = downloadStore.getObject(url.toString());
            if (!isValid(downloadStoreObject, hashProvider)) {
                //TODO: Download
            }

            // Link downloadStoreObject to target
            try {
                Files.createDirectories(target.getParent());
                Files.createSymbolicLink(target, downloadStoreObject);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return target;
        });
    }

    @Override
    public Future<Path> download(@NotNull URL url, @NotNull Store store, @Nullable HashProvider hashProvider, @Nullable DownloadCallback callback) {
        // target = storeRoot/hash(url)
        String targetName = PolyHasher.hash(url.toString(), PolyHasher.SHA256);
        Path target = store.getStoreRoot().resolve(targetName);
        return download(url, target, hashProvider, callback);
    }

    private boolean isValid(@NotNull Path target, @Nullable HashProvider hashProvider) {
        if (!Files.exists(target)) {
            return false;
        }
        if (hashProvider == null) {
            return false;
        }
        String hash = hashProvider.getHash();
        Supplier<MessageDigest> hashingFunction = hashProvider.getHashingFunction();
        if (hash == null || hashingFunction == null) {
            return false;
        }
        String computedHash;
        try {
            MessageDigest messageDigest = hashProvider.getHashingFunction().get();
            computedHash = PolyHasher.hash(target, messageDigest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return hash.equals(computedHash);
    }
}
