package dev.deftu.filestream.tests;

import dev.deftu.filestream.api.Store;
import dev.deftu.filestream.download.DownloaderImpl;
import dev.deftu.filestream.store.FastHashSchema;
import dev.deftu.filestream.util.HashingHelper;
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
        Store downloadStore = Helper.provideStore("download-cache", new FastHashSchema(HashingHelper.MD5));
        dev.deftu.filestream.api.Downloader downloader = new DownloaderImpl(downloadStore);

        dev.deftu.filestream.api.Downloader.Download<URL> dl = downloader.download(new URL("https://wallpaperaccess.com//full/621802.jpg"), (p, tp) -> {
            System.out.println("DL Progress: " + p + "/" + tp + " (" + ((float) p / (float) tp) * 100 + "%)");
        });
        //noinspection StatementWithEmptyBody
        while (!dl.isDone()) {
        }
        Path path = dl.get();
        System.out.println("Downloaded at " + path);
    }
}
