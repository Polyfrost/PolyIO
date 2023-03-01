package cc.polyfrost.polyio.store;

import cc.polyfrost.polyio.PolyIO;
import cc.polyfrost.polyio.api.Store;
import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author xtrm
 */
public @Data class PolyStore implements Store {
    public static final Store GLOBAL_STORE = new PolyStore(
            PolyIO.getLocalStorage(),
            PolyIO.GLOBAL_STORE_NAME,
            Store.ObjectSchema.DIRECT
    );

    private final Path storeRoot;
    private final Store.ObjectSchema objectSchema;

    public PolyStore(@NotNull Path parent, @NotNull String storeDirName) {
        this(parent, storeDirName, Store.ObjectSchema.DIRECT);
    }

    @SneakyThrows
    public PolyStore(@NotNull Path parent, @NotNull String storeDirName, @NotNull Store.ObjectSchema schema) {
        this.storeRoot = parent.resolve(storeDirName);
        this.objectSchema = schema;
        Files.createDirectories(this.storeRoot);
    }

    @SneakyThrows
    @Override
    public Path getObject(String name) {
        return this.objectSchema.getObjectPath(this.storeRoot, name);
    }

    @Override
    public Store getSubStore(String name) {
        return getSubStore(name, objectSchema);
    }

    @Override
    public Store getSubStore(String name, ObjectSchema objectSchema) {
        return new PolyStore(storeRoot, name, objectSchema);
    }
}
