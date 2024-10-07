package dev.deftu.filestream.tests;

import dev.deftu.filestream.api.Store;
import dev.deftu.filestream.store.FileStore;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

class Helper {
    private static Path globalRoot;

    @SneakyThrows
    public static void init() {
        if (globalRoot == null) {
            globalRoot = Files.createTempDirectory("polyio-tests");
        }
    }

    @SneakyThrows
    public static FileStore provideStore(String name) {
        return provideStore(name, Store.ObjectSchema.DIRECT);
    }

    @SneakyThrows
    public static FileStore provideStore(String name, Store.ObjectSchema schema) {
        return new FileStore(Helper.globalRoot, name, schema);
    }
}
