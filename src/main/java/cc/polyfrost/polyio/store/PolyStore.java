package cc.polyfrost.polyio.store;

import cc.polyfrost.polyio.api.Store;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author xtrm
 */
public @Data class PolyStore implements Store  {
    private final Path storeRoot;
    private final Store.ObjectSchema objectSchema;

    public PolyStore(@NotNull Path parent, @NotNull String storeDirName) throws IOException {
        this(parent, storeDirName, Store.ObjectSchema.DIRECT);
    }

    public PolyStore(@NotNull Path parent, @NotNull String storeDirName, @NotNull Store.ObjectSchema schema) throws IOException {
        this.storeRoot = parent.resolve(storeDirName);
        this.objectSchema = schema;
        Files.createDirectories(this.storeRoot);
    }

    @Override
    public Path getObject(String name) {
        return this.objectSchema.getObjectPath(this.storeRoot, name);
    }
}
