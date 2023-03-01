package cc.polyfrost.polyio.tests;

import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.store.PolyStore;
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
    public static PolyStore provideStore(String name) {
        return provideStore(name, Store.ObjectSchema.DIRECT);
    }

    @SneakyThrows
    public static PolyStore provideStore(String name, Store.ObjectSchema schema) {
        return new PolyStore(Helper.globalRoot, name, schema);
    }
}
