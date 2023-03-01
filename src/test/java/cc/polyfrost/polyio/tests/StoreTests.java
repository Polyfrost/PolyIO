package cc.polyfrost.polyio.tests;

import cc.polyfrost.polyio.api.Store;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
    @SneakyThrows
    public void testStoreObject() {
        Store store = Helper.provideStore("testStoreObject");

        final String toWrite = "teststr-" + UUID.randomUUID();
        Path object = store.getObject("test");
        assertFalse(Files.exists(object));
        Files.write(object, toWrite.getBytes());
        assertTrue(Files.exists(object));
        String result = new String(Files.readAllBytes(object));
        assertEquals(toWrite, result);

        Path object2 = store.getObject("test");
        assertEquals(object, object2);
        assertTrue(Files.exists(store.getObject("test")));
        String result2 = new String(Files.readAllBytes(object2));
        assertEquals(toWrite, result2);
    }
}
