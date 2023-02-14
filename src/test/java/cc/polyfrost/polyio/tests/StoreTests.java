package cc.polyfrost.polyio.tests;

import cc.polyfrost.polyio.api.Store;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testable
public class StoreTests {
    @BeforeAll
    public static void setup() {
        Helper.init();
    }

    @Test
    public void testStore() {
        Store store = Helper.provideStore("testStore");
        assertTrue(Files.exists(store.getStoreRoot()));
    }

    @Test
    public void testStoreObject() {
        Store store = Helper.provideStore("testStoreObject");
        Path object = store.getObject("test");
        assertFalse(Files.exists(object));
    }
}
