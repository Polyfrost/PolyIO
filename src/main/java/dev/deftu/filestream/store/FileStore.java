package dev.deftu.filestream.store;

import dev.deftu.filestream.FileStream;
import dev.deftu.filestream.api.Store;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author xtrm
 */
public class FileStore implements Store {

    public static final @NotNull Store GLOBAL_STORE = new FileStore(
            FileStream.getLocalStorage(),
            FileStream.GLOBAL_STORE_NAME,
            Store.ObjectSchema.DIRECT
    );

    private final Path storeRoot;
    private final Store.ObjectSchema objectSchema;

    public FileStore(@NotNull Path parent, @NotNull String storeDirName) {
        this(parent, storeDirName, Store.ObjectSchema.DIRECT);
    }

    public FileStore(@NotNull Path parent, @NotNull String storeDirName, @NotNull Store.ObjectSchema schema) {
        this.storeRoot = parent.resolve(storeDirName);
        this.objectSchema = schema;

        try {
            Files.createDirectories(this.storeRoot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (storeDirName.startsWith(".")) {
            try {
                Files.setAttribute(this.storeRoot, "dos:hidden", true);
            } catch (Throwable ignored) {
            }
        }
    }

    public @NotNull Path getStoreRoot() {
        return storeRoot;
    }

    @Override
    public @NotNull Path getObject(String name) {
        try {
            return this.objectSchema.getObjectPath(this.storeRoot, name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Store getSubStore(String name) {
        return getSubStore(name, objectSchema);
    }

    @Override
    public @NotNull Store getSubStore(String name, ObjectSchema objectSchema) {
        return new FileStore(storeRoot, name, objectSchema);
    }

}
