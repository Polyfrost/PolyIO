package cc.polyfrost.polyio.download;

import cc.polyfrost.polyio.api.Downloader;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.Future;

/**
 * @author xtrm
 */
@Data
class PolyDownload implements Downloader.Download<URL> {
    private final URL source;
    @Delegate
    private final Future<Path> future;
}
