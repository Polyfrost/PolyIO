package cc.polyfrost.polyio.tests;

import cc.polyfrost.polyio.api.Downloader;
import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.download.PolyDownloader;
import cc.polyfrost.polyio.store.FastHashSchema;
import cc.polyfrost.polyio.util.PolyHashing;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.net.URL;
import java.nio.file.Path;

@Testable
public class BestTest {
    @BeforeAll
    public static void setup() {
        Helper.init();
    }

    @SneakyThrows
    @Test
    public void test() {
        Store downloadStore = Helper.provideStore("download-cache", new FastHashSchema(PolyHashing.MD5));
        Downloader downloader = new PolyDownloader(downloadStore);

        Downloader.Download<URL> dl = downloader.download(new URL("https://wallpaperaccess.com//full/621802.jpg"), (p, tp) -> {
            System.out.println("DL Progress: " + p + "/" + tp + " (" + ((float) p / (float) tp) * 100 + "%)");
        });
        //noinspection StatementWithEmptyBody
        while (!dl.isDone()) {
        }
        Path path = dl.get();
        System.out.println("Downloaded at " + path);
    }

    @SneakyThrows
    @Test
    public void test2() {
    }
}
